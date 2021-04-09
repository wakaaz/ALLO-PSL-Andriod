package com.net.pslapllication.util.customvideoview

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

internal class Decoders {

    private val mDecoders: MutableList<MediaCodecDecoder>
    private var mVideoDecoder: MediaCodecVideoDecoder? = null
    private var mAudioDecoder: MediaCodecAudioDecoder? = null
    fun addDecoder(decoder: MediaCodecDecoder) {
        mDecoders.add(decoder)
        if (decoder is MediaCodecVideoDecoder) {
            mVideoDecoder = decoder as MediaCodecVideoDecoder
        } else if (decoder is MediaCodecAudioDecoder) {
            mAudioDecoder = decoder as MediaCodecAudioDecoder
        }
    }

    val decoders: MutableList<MediaCodecDecoder>
        get() = mDecoders

    val videoDecoder: MediaCodecVideoDecoder?
        get() = mVideoDecoder

    val audioDecoder: MediaCodecAudioDecoder?
        get() = mAudioDecoder

    /**
     * Runs the audio/video decoder loop, optionally until a new frame is available.
     * The returned frameInfo object keeps metadata of the decoded frame. To render the frame
     * to the screen and/or dismiss its data, call [MediaCodecVideoDecoder.releaseFrame]
     * or [MediaCodecVideoDecoder.releaseFrame].
     *
     * @param force force decoding in a loop until a frame becomes available or the EOS is reached
     * @return a VideoFrameInfo object holding metadata of a decoded video frame or NULL if no frame has been decoded
     */
    @Throws(IOException::class)
    fun decodeFrame(force: Boolean): MediaCodecDecoder.FrameInfo? {
        //Log.d(TAG, "decodeFrame");
        var outputEos = false
        while (!outputEos) {
            var outputEosCount = 0
            var fi: MediaCodecDecoder.FrameInfo?=null
            var vfi: MediaCodecDecoder.FrameInfo? = null
            for (decoder in mDecoders) {
                while (decoder.dequeueDecodedFrame().also({ fi = it }) != null) {
                    if (decoder === mVideoDecoder) {
                        vfi = fi!!
                        break
                    } else {
                        decoder.renderFrame(fi!!, 0)
                    }
                }
                while (decoder.queueSampleToCodec(false)) {
                }
                if (decoder.isOutputEos) {
                    outputEosCount++
                }
            }
            if (vfi != null) {
                // If a video frame has been decoded, return it
                return vfi
            }
            if (!force) {
                // If we have not decoded a video frame and we're not forcing decoding until a frame
                // becomes available, return null.
                return null
            }
            outputEos = outputEosCount == mDecoders.size
        }
        Log.d(TAG, "EOS NULL")
        return null // EOS already reached, no video frame left to return
    }

    /**
     * Releases all decoders. This must be called to free decoder resources when this object is no longer in use.
     */
    fun release() {
        for (decoder in mDecoders) {
            // Catch decoder.release() exceptions to avoid breaking the release loop on the first
            // exception and leaking unreleased decoders.
            try {
                decoder.release()
            } catch (e: Exception) {
                Log.e(TAG, "release failed", e)
            }
        }
        mDecoders.clear()
    }

    @Throws(IOException::class)
    fun seekTo(seekMode: MediaPlayer.SeekMode?, seekTargetTimeUs: Long) {
        for (decoder in mDecoders) {
            if (seekMode != null) {
                decoder.seekTo(seekMode, seekTargetTimeUs)
            }
        }
    }

    fun renderFrames() {
        for (decoder in mDecoders) {
            decoder.renderFrame()
        }
    }

    fun dismissFrames() {
        for (decoder in mDecoders) {
            decoder.dismissFrame()
        }
    }

    val currentDecodingPTS: Long
        get() {
            var minPTS = Long.MAX_VALUE
            for (decoder in mDecoders) {
                val pts: Long = decoder.inputSamplePTS
                if (pts != MediaCodecDecoder.PTS_NONE && minPTS > pts) {
                    minPTS = pts
                }
            }
            return minPTS
        }

    val inputSamplePTS: Long
        get() {
            var maxPTS: Long = MediaCodecDecoder.PTS_UNKNOWN
            for (decoder in mDecoders) {
                val pts: Long = decoder.inputSamplePTS
                if (pts > maxPTS) {
                    maxPTS = pts
                }
            }
            return maxPTS
        }

    //return getCurrentDecodingPTS() == MediaCodecDecoder.PTS_EOS;
    val isEOS: Boolean
        get() {
            //return getCurrentDecodingPTS() == MediaCodecDecoder.PTS_EOS;
            var eosCount = 0
            for (decoder in mDecoders) {
                if (decoder.isOutputEos) {
                    eosCount++
                }
            }
            return eosCount == mDecoders.size
        }// There were no decoders that updated this value, which means we don't have information
    // on a cached duration, so we return -1 to signal that the information is not available.

    // Init with the largest possible value...

    // ...then decrease to the lowest duration.
    // We always return the lowest value, because if only one decoder has to refill its buffer,
    // all others have to wait. If one decoder returns -1, this function returns -1 too (which
    // makes sense because we cannot calculate a meaningful cache duration in this case).
    val cachedDuration: Long
        get() {
            // Init with the largest possible value...
            var minCachedDuration = Long.MAX_VALUE

            // ...then decrease to the lowest duration.
            // We always return the lowest value, because if only one decoder has to refill its buffer,
            // all others have to wait. If one decoder returns -1, this function returns -1 too (which
            // makes sense because we cannot calculate a meaningful cache duration in this case).
            for (decoder in mDecoders) {
                val cachedDuration: Long = decoder.cachedDuration
                minCachedDuration = Math.min(cachedDuration, minCachedDuration)
            }
            return if (minCachedDuration == Long.MAX_VALUE) {
                // There were no decoders that updated this value, which means we don't have information
                // on a cached duration, so we return -1 to signal that the information is not available.
                -1
            } else minCachedDuration
        }

    /**
     * Returns true only if all decoders have reached the end of stream.
     */
    fun hasCacheReachedEndOfStream(): Boolean {
        for (decoder in mDecoders) {
            if (!decoder.hasCacheReachedEndOfStream()) {
                return false
            }
        }
        return true
    }

    companion object {
        private val TAG = Decoders::class.java.simpleName
    }

    init {
        mDecoders = ArrayList()
    }
}