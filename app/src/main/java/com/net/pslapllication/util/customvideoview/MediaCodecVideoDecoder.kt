package com.net.pslapllication.util.customvideoview


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.AudioMetadata
import android.media.MediaCodec;
import android.media.MediaFormat;
 import android.util.Log;
import android.view.Surface;

import java.io.IOException;

internal class MediaCodecVideoDecoder @Throws(IOException::class)
constructor(extractor:MediaExtractor, passive:Boolean, trackIndex:Int,
            listener:OnDecoderEventListener, videoSurface:Surface, renderModeApi21:Boolean):MediaCodecDecoder(extractor, passive, trackIndex, listener) {
    private var mVideoSurface:Surface
    private var mRenderModeApi21:Boolean = false
    val videoWidth:Int
        get() {
            val format:MediaFormat = MediaFormat()
            return if (format != null)
                ((format.getInteger(MediaFormat.KEY_HEIGHT) * format.getFloat(MediaExtractor.MEDIA_FORMAT_EXTENSION_KEY_DAR))) as Int
            else
                0
        }
    val videoHeight:Int
        get() {
            val format =MediaFormat()
            return if (format != null) format.getInteger(MediaFormat.KEY_HEIGHT) else 0
        }
    /**
     * Returns the rotation of the video in degree.
     * Only works on API21+, else it always returns 0.
     * @return the rotation of the video in degrees
     */
    // rotation-degrees is available from API21, officially supported from API23 (KEY_ROTATION)
    val videoRotation:Int
        get() {
            val format = MediaFormat()
            return if (format != null && format.containsKey("rotation-degrees"))
                format.getInteger("rotation-degrees")
            else
                0
        }
    init{
        mVideoSurface = videoSurface
        mRenderModeApi21 = renderModeApi21
        reinitCodec()
    }

    @Throws(IOException::class)
    fun updateSurface(videoSurface:Surface) {
        if (videoSurface == null)
        {
            // TODO disable video decoder when surface is null
            throw RuntimeException("surface must not be null")
        }
        mVideoSurface = videoSurface
        reinitCodec()
    }
    @SuppressLint("NewApi")
    override fun renderFrame(frameInfo:FrameInfo, offsetUs:Long) {
        //Log.d(TAG, "renderFrame: " + frameInfo);
        if (mRenderModeApi21)
        {
            releaseFrame(frameInfo, offsetUs)
        }
        else
        {
            releaseFrame(frameInfo, true)
        }
    }
    /**
     * Releases all data belonging to a frame and optionally renders it to the configured surface.
     */
    fun releaseFrame(frameInfo:FrameInfo, render:Boolean) {
        codec.releaseOutputBuffer(frameInfo.buffer, render) // render picture
        releaseFrameInfo(frameInfo)
    }
    /**
     * Releases all data belonging to a frame and optionally renders it to the configured surface.
     */
    @TargetApi(21)
    fun releaseFrame(frameInfo:FrameInfo, renderOffsetUs:Long) {
        /** In contrast to the old rendering method through {@link MediaCodec#releaseOutputBuffer(int, boolean)}
         * this method does not need a timing/throttling mechanism (e.g. {@link Thread#sleep(long)})
         * and returns instantly, but still defers rendering internally until the given
         * timestamp. It does not release the buffer until the actual rendering though,
         * and thus times/throttles the decoding loop by keeping the buffers and not returning
         * them until the picture is rendered, which means that {@link MediaCodec#dequeueOutputBuffer(MediaCodec.BufferInfo, long)}
         * fails until a frame is rendered and the associated buffer returned to the codec
         * for a new frame output.
         */
        val renderTimestampNs = System.nanoTime() + (renderOffsetUs * 1000)
        codec.releaseOutputBuffer(frameInfo.buffer, renderTimestampNs) // render picture
        releaseFrameInfo(frameInfo)
    }
    @Throws(IOException::class)
    protected override fun seekTo(seekMode:MediaPlayer.SeekMode, seekTargetTimeUs:Long,
                                  extractor:MediaExtractor, codec:MediaCodec):FrameInfo {
        /* Android API compatibility:
     * Use millisecond precision to stay compatible with VideoView API that works
     * in millisecond precision only. Else, exact seek matches are missed if frames
     * are positioned at fractions of a millisecond. */
        var presentationTimeMs:Long = -1
        var seekTargetTimeUs:Long =  seekTargetTimeUs
        var seekTargetTimeMs = seekTargetTimeUs / 1000
        var frameInfo = super.seekTo(seekMode, seekTargetTimeUs, extractor, codec)
        if ((seekMode === MediaPlayer.SeekMode.FAST
                    || seekMode === MediaPlayer.SeekMode.FAST_TO_CLOSEST_SYNC
                    || seekMode === MediaPlayer.SeekMode.FAST_TO_PREVIOUS_SYNC
                    || seekMode === MediaPlayer.SeekMode.FAST_TO_NEXT_SYNC))
        {
            Log.d(TAG, "fast seek to " + seekTargetTimeUs + " arrived at " + frameInfo!!.presentationTimeUs)
        }
        else if (seekMode === MediaPlayer.SeekMode.FAST_EXACT)
        {
            releaseFrame(frameInfo!!, false)
            fastSeek(seekTargetTimeUs, extractor, codec)
            frameInfo = decodeFrame(true, true)
            Log.d(TAG, "fast_exact seek to " + seekTargetTimeUs + " arrived at " + frameInfo!!.presentationTimeUs)
            if (frameInfo!!.presentationTimeUs < seekTargetTimeUs)
            {
                Log.d(TAG, "presentation is behind...")
            }
            return frameInfo
        }
        else if (seekMode === MediaPlayer.SeekMode.PRECISE || seekMode === MediaPlayer.SeekMode.EXACT)
        {
            /* NOTE
       * This code seeks one frame too far, except if the seek time equals the
       * frame PTS:
       * (F1.....)(F2.....)(F3.....) ... (Fn.....)
       * A frame is shown for an interval, e.g. (1/fps seconds). Now if the seek
       * target time is somewhere in frame 2's interval, we end up with frame 3
       * because we need to decode it to know if the seek target time lies in
       * frame 2's interval (because we don't know the frame rate of the video,
       * and neither if it's a fixed frame rate or a variable one - even when
       * deriving it from the PTS series we cannot be sure about it). This means
       * we always end up one frame too far, because the MediaCodec does not allow
       * to go back, except when starting at a sync frame.
       *
       * Solution for fixed frame rate could be to subtract the frame interval
       * time (1/fps secs) from the seek target time.
       *
       * Solution for variable frame rate and unknown frame rate: go back to sync
       * frame and re-seek to the now known exact PTS of the desired frame.
       * See EXACT mode handling below.
       */
            var frameSkipCount = 0
            var lastPTS:Long = -1
            presentationTimeMs = frameInfo!!.presentationTimeUs / 1000
            while (presentationTimeMs < seekTargetTimeMs)
            {
                if (frameSkipCount == 0)
                {
                    Log.d(TAG, "skipping frames...")
                }
                frameSkipCount++
                if (isOutputEos)
                {
                    /* When the end of stream is reached while seeking, the seek target
           * time is set to the last frame's PTS, else the seek skips the last
           * frame which then does not get rendered, and it might end up in a
           * loop trying to reach the unreachable target time. */
                    seekTargetTimeUs = frameInfo!!.presentationTimeUs
                    seekTargetTimeMs = seekTargetTimeUs / 1000
                }
                if (frameInfo!!.endOfStream)
                {
                    Log.d(TAG, "end of stream reached, seeking to last frame")
                    releaseFrame(frameInfo!!, false)
                    return seekTo(seekMode, lastPTS, extractor, codec)
                }
                lastPTS = frameInfo!!.presentationTimeUs
                releaseFrame(frameInfo!!, false)
                frameInfo = decodeFrame(true, true)
                presentationTimeMs = frameInfo!!.presentationTimeUs / 1000
            }
            Log.d(TAG, "frame new position: " + frameInfo!!.presentationTimeUs)
            Log.d(TAG, "seeking finished, skipped " + frameSkipCount + " frames")
            if (seekMode === MediaPlayer.SeekMode.EXACT && presentationTimeMs > seekTargetTimeMs)
            {
                if (frameSkipCount == 0)
                {
                    // In a single stream, the initiating seek always seeks before or directly
                    // to the requested frame, and this case never happens. With DASH, when the seek
                    // target is very near a segment border, it can happen that a wrong segment
                    // (the following one) is determined as target seek segment, which means the
                    // target of the initiating seek is too far, and we cannot go back either because
                    // it is the first frame of the segment
                    // TODO avoid this case by fixing DASH seek (fix segment calculation or reissue
                    // seek to previous segment when this case is detected)
                    Log.w(TAG, "this should never happen")
                }
                else
                {
                    /* If the current frame's PTS it after the seek target time, we're
           * one frame too far into the stream. This is because we do not know
           * the frame rate of the video and therefore can't decide for a frame
           * if its interval covers the seek target time of if there's already
           * another frame coming. We know after the next frame has been
           * decoded though if we're too far into the stream, and if so, and if
           * EXACT mode is desired, we need to take the previous frame's PTS
           * and repeat the seek with that PTS to arrive at the desired frame.
           */
                    Log.d(TAG, "exact seek: repeat seek for previous frame at " + lastPTS)
                    releaseFrame(frameInfo, false)
                    return seekTo(seekMode, lastPTS, extractor, codec)
                }
            }
        }
        if (presentationTimeMs == seekTargetTimeMs)
        {
            Log.d(TAG, "exact seek match!")
        }
        //
        // if (mAudioExtractor != null && mAudioExtractor != mExtractor) {
        // mAudioExtractor.seekTo(mBufferInfo.presentationTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        // }
        return frameInfo!!
    }
    @Throws(IOException::class)
    private fun fastSeek(targetTime:Long, extractor:MediaExtractor, codec:MediaCodec):Long {
        codec.flush()
        var targetTime:Long = targetTime
        extractor.seekTo(targetTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        if (extractor.sampleTime === targetTime)
        {
            Log.d(TAG, "skip fastseek, already there")
            return targetTime
        }
        // 1. Queue first sample which should be the sync/I frame
        skipToNextSample()
        queueSampleToCodec(false)
        // 2. Then, fast forward to target frame
        /* 2.1 Search for the best candidate frame, which is the one whose
     * right/positive/future distance is minimized
     */
        extractor.seekTo(targetTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        /* Specifies how many frames we continue to check after the first candidate,
     * to account for DTS picture reordering (this value is arbitrarily chosen) */
        var maxFrameLookahead = 20
        var candidatePTS:Long = 0
        var candidateDistance = java.lang.Long.MAX_VALUE
        var lookaheadCount = 0
        while (extractor.advance() && lookaheadCount < maxFrameLookahead)
        {
            val distance = targetTime - extractor.sampleTime
            if (distance >= 0 && distance < candidateDistance)
            {
                candidateDistance = distance
                candidatePTS = extractor.sampleTime
                //Log.d(TAG, "candidate " + candidatePTS + " d=" + candidateDistance);
            }
            if (distance < 0)
            {
                lookaheadCount++
            }
        }
        targetTime = candidatePTS // set best candidate frame as exact seek target
        // 2.2 Fast forward to chosen candidate frame
        extractor.seekTo(targetTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        while (extractor.sampleTime !== targetTime)
        {
            extractor.advance()
        }
        Log.d(TAG, "exact fastseek match: " + extractor.sampleTime)
        return targetTime
    }
}