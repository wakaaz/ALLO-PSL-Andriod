package com.net.pslapllication.util.customvideoview

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.SystemClock
import android.util.Log

import java.io.IOException
import java.nio.ByteBuffer
import java.util.ArrayList

internal abstract class MediaCodecDecoder(
    extractor: com.net.pslapllication.util.customvideoview.MediaExtractor?, passive: Boolean, trackIndex: Int,
    listener: OnDecoderEventListener?
) {
    internal class FrameInfo {
        var buffer = 0
        var data: ByteBuffer? = null
        var presentationTimeUs: Long = 0
        var endOfStream = false
        var representationChanged = false
        fun clear() {
            buffer = -1
            data = null
            presentationTimeUs = -1
            endOfStream = false
            representationChanged = false
        }

        override fun toString(): String {
            return "FrameInfo{" +
                    "buffer=" + buffer +
                    ", data=" + data +
                    ", presentationTimeUs=" + presentationTimeUs +
                    ", endOfStream=" + endOfStream +
                    ", representationChanged=" + representationChanged +
                    '}'
        }

        init {
            clear()
        }
    }

    internal interface OnDecoderEventListener {
        fun onBuffering(decoder: MediaCodecDecoder?)
    }

    protected var TAG = MediaCodecDecoder::class.java.simpleName
    private val mExtractor: com.net.pslapllication.util.customvideoview.MediaExtractor
    private val mTrackIndex: Int
    protected var format: MediaFormat
        private set
    protected var codec: MediaCodec
        private set
    private lateinit var mCodecInputBuffers: Array<ByteBuffer>
    private lateinit var mCodecOutputBuffers: Array<ByteBuffer>
    private var mBufferInfo: MediaCodec.BufferInfo? = null
    protected var isInputEos = false
        private set
    var isOutputEos = false
        private set
    private var mEmptyFrameInfos: MutableList<FrameInfo>? = null

    /* Flag notifying that the representation has changed in the extractor and needs to be passed
     * to the decoder. This transition state is only needed in playback, not when seeking. */
    private var mRepresentationChanging = false

    /* Flag notifying that the decoder has changed to a new representation, post-actions need to
     * be carried out. */
    private var mRepresentationChanged = false
    private val mOnDecoderEventListener: OnDecoderEventListener?

    /**
     * Flag for passive mode. When a decoder is in passive mode, it does not actively control
     * the extractor, because the extractor is controlled from another decoder instance. It does
     * therefore also not execute any operations that affect the extractor in any way (e.g. seeking).
     *
     */
    protected val isPassive: Boolean

    /**
     * Returns the PTS of the current frame enqueued for decoding. This is always ahead of
     * [.getCurrentDecodingPTS].
     * @return the PTS of the most recent frame enqueued for decoding
     */
    var inputSamplePTS: Long = 0
        private set

    /**
     * Returns the PTS of the current, that is, the most recently decoded frame.
     * @return the PTS of the most recent decoded frame
     */
    var currentDecodingPTS: Long
        private set
    private var mCurrentFrameInfo: FrameInfo? = null

    /**
     * Starts or restarts the codec with a new format, e.g. after a representation change.
     */
    @Throws(IOException::class)
    protected fun reinitCodec() {
        try {
            val t1 = SystemClock.elapsedRealtime()
            /* On some versions of Android it does not work to reconfigure an existing codec
             * instance. The codec just won't return any input buffer afterwards, stalling
             * decoding and thus playback. Instead, we need to create a completely new instance.
             *
             * https://github.com/protyposis/MediaPlayer-Extended/issues/67
             *
             * Issue observed on:
             * - Android 4.1.2 (LG Lucid 2 [VS870 4G])
             * - CM 10.1.3 / Android 4.2.2 (Samsung Galaxy S2 [GT-I9100])
             * - CM 10.1.3 / Android 4.2.2 (Samsung Galaxy S3 [SGH-I747])
             *
             * Non-issue on:
             * - Android 4.1.2 (Samsung Galaxy S2 / GT-I9100)
             * - Android 4.4.4 (Samsung Galaxy S3 [SGH-I747])
             * - Android 5.x (Nexus 5)
             * - Android 5.1 (Blu Energy X2)
             * - Android 6.x (Nexus 5X)
             * - Android 7.x (Nexus 5X)
             * - Android 7.1 (Nexus 5X)
             * - Android 7.1.1 (OnePlus 3 [A3000])
             *
             * Since this issue has only been observed on Android below 4.4, we limit this fix to
             * these versions.
              */
            val createNewDecoder =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT

            // Get new format and restart codec with this format
            format = mExtractor.getTrackFormat(mTrackIndex)
            codec.stop()
            if (createNewDecoder) {
                codec.release()
                codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            }
            configureCodec(codec, format)
            codec.start() // TODO speedup, but how? this takes a long time and introduces lags when switching DASH representations (AVC codec)
            mCodecInputBuffers = codec.inputBuffers
            mCodecOutputBuffers = codec.outputBuffers
            mBufferInfo = MediaCodec.BufferInfo()
            isInputEos = false
            isOutputEos = false

            // Create FrameInfo objects for later reuse
            mEmptyFrameInfos = ArrayList()
            for (i in mCodecOutputBuffers.indices) {
                (mEmptyFrameInfos as ArrayList<FrameInfo>).add(FrameInfo())
            }
            Log.d(TAG, "reinitCodec " + (SystemClock.elapsedRealtime() - t1) + "ms")
        } catch (e: IllegalArgumentException) {
            codec.release() // Release failed codec to not leak a codec thread (MediaCodec_looper)
            Log.e(TAG, "reinitCodec: invalid surface or format")
            throw e
        } catch (e: IllegalStateException) {
            codec.release() // Release failed codec to not leak a codec thread (MediaCodec_looper)
            Log.e(TAG, "reinitCodec: illegal state")
            throw e
        }
    }

    /**
     * Configures the codec during initialization. Should be overwritten by subclasses that require
     * a more specific configuration.
     *
     * @param codec the codec to configure
     * @param format the format to configure the codec with
     */
    protected open fun configureCodec(codec: MediaCodec, format: MediaFormat?) {
        codec.configure(format, null, null, 0)
    }

    /**
     * Skips to the next sample of this decoder's track by skipping all samples belonging to other decoders.
     */
    fun skipToNextSample() {
        if (isPassive) return
        var trackIndex: Int
        while (mExtractor.sampleTrackIndex
                .also { trackIndex = it } != -1 && trackIndex != mTrackIndex && !isInputEos
        ) {
            mExtractor.advance()
        }
    }

    /**
     * Checks any constraints if it is a good idea to decode another frame. Returns true by default,
     * and is meant to be overwritten by subclasses with special behavior, e.g. an audio track might
     * limit filling of the playback buffer.
     *
     * @return value telling if another frame should be decoded
     */
    protected open fun shouldDecodeAnotherFrame(): Boolean {
        return true
    }

    /**
     * Queues a sample from the MediaExtractor to the input of the MediaCodec. The return value
     * signals if the operation was successful and can be tried another time (return true), or if
     * there are no more input buffers available, the next sample does not belong to this decoder
     * (if skip is false) or the input EOS is reached (return false).
     *
     * @param skip if true, samples belonging to foreign tracks are skipped
     * @return true if the operation can be repeated for another sample, false if it's another
     * decoder's turn or the EOS
     */
    fun queueSampleToCodec(skip: Boolean): Boolean {
        if (isInputEos || !shouldDecodeAnotherFrame()) return false

        // If we are not at the EOS and the current extractor track is not the this track, we
        // return false because it is some other decoder's turn now.
        // If we are at the EOS, the following code will issue a BUFFER_FLAG_END_OF_STREAM.
        if (mExtractor.sampleTrackIndex != -1 && mExtractor.sampleTrackIndex != mTrackIndex) {
            return if (skip) mExtractor.advance() else false
        }
        var sampleQueued = false
        val inputBufIndex = codec.dequeueInputBuffer(TIMEOUT_US)
        if (inputBufIndex >= 0) {
            val inputBuffer: ByteBuffer = mCodecInputBuffers[inputBufIndex]
            if (mExtractor.hasTrackFormatChanged()) {
                /* The mRepresentationChanging flag and BUFFER_FLAG_END_OF_STREAM flag together
                 * notify the decoding loop that the representation changes and the codec
                 * needs to be reconfigured.
                 */
                mRepresentationChanging = true
                codec.queueInputBuffer(
                    inputBufIndex,
                    0,
                    0,
                    0,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )

                // Check buffering state before representation changes (and possibly a new segment needs to be downloaded)
                if (mExtractor.cachedDuration > -1) {
                    mOnDecoderEventListener?.onBuffering(this)
                }
            } else {
                // Check buffering state before the blocking readSampleData call
                if (mExtractor.cachedDuration > -1) {
                    mOnDecoderEventListener?.onBuffering(this)
                }
                var sampleSize = mExtractor.readSampleData(inputBuffer, 0)
                var presentationTimeUs: Long = 0
                if (sampleSize < 0) {
                    Log.d(TAG, "EOS input")
                    isInputEos = true
                    sampleSize = 0
                } else {
                    presentationTimeUs = mExtractor.sampleTime
                    sampleQueued = true
                }
                codec.queueInputBuffer(
                    inputBufIndex,
                    0,
                    sampleSize,
                    presentationTimeUs,
                    if (isInputEos) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                )
                inputSamplePTS = presentationTimeUs

                //Log.d(TAG, "queued PTS " + presentationTimeUs);
                if (!isInputEos) {
                    mExtractor.advance()
                }
            }
        }
        return sampleQueued
    }

    /**
     * Consumes a decoded frame from the decoder output and returns information about it.
     *
     * @return a FrameInfo if a frame was available; NULL if the decoder needs more input
     * samples/decoding time or if the output EOS has been reached
     */
    @Throws(IOException::class)
    fun dequeueDecodedFrame(): FrameInfo? {
        if (isOutputEos) return null
        val res =
            codec.dequeueOutputBuffer(mBufferInfo!!, TIMEOUT_US)
        isOutputEos = res >= 0 && mBufferInfo!!.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
        if (isOutputEos && mRepresentationChanging) {
            /* Here, the output is not really at its end, it's just the end of the
             * current representation segment, and the codec needs to be reconfigured to
             * the following representation format to carry on.
             */
            reinitCodec()
            isOutputEos = false
            mRepresentationChanging = false
            mRepresentationChanged = true
        } else if (res >= 0) {
            // Frame decoded. Fill frame info object and return to caller...

            // Adjust buffer: http://bigflake.com/mediacodec/#q11
            // This is done on audio buffers only, video decoder does not return actual buffers
            val data: ByteBuffer = mCodecOutputBuffers[res]
            if (data != null && mBufferInfo!!.size != 0) {
                data.position(mBufferInfo!!.offset)
                data.limit(mBufferInfo!!.offset + mBufferInfo!!.size)
                //Log.d(TAG, "raw data bytes: " + mBufferInfo.size);
            }
            val fi = mEmptyFrameInfos!![0]
            fi.buffer = res
            fi.data = data
            fi.presentationTimeUs = mBufferInfo!!.presentationTimeUs
            fi.endOfStream = isOutputEos
            if (mRepresentationChanged) {
                mRepresentationChanged = false
                fi.representationChanged = true
            }
            if (fi.endOfStream) {
                Log.d(TAG, "EOS output")
            } else {
                currentDecodingPTS = fi.presentationTimeUs
            }

            //Log.d(TAG, "decoded PTS " + fi.presentationTimeUs);
            return fi
        } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            mCodecOutputBuffers = codec.outputBuffers
            Log.d(TAG, "output buffers have changed.")
        } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // NOTE: this is the format of the raw output, not the format as specified by the container
            val format = codec.outputFormat
            Log.d(TAG, "output format has changed to $format")
            onOutputFormatChanged(format)
        } else if (res == MediaCodec.INFO_TRY_AGAIN_LATER) {
            //Log.d(TAG, "dequeueOutputBuffer timed out");
        }

        //Log.d(TAG, "EOS NULL");
        return null // EOS already reached, no frame left to return
    }

    /**
     * Returns the duration if the cached data in the extractor, or -1 if the extractor does not
     * support or does not need caching (e.g. local files).
     * @return the duration of the cached data or -1 if caching is not active
     */
    val cachedDuration: Long
        get() = mExtractor.cachedDuration

    /**
     * Returns true iff we are caching data and the cache has reached the
     * end of the data stream.
     * @see MediaExtractor.hasCacheReachedEndOfStream
     * @return true if caching and end of stream has been reached, else false
     */
    fun hasCacheReachedEndOfStream(): Boolean {
        return mExtractor.hasCacheReachedEndOfStream()
    }

    /**
     * Renders a frame at the specified offset time to some output (e.g. video frame to screen,
     * audio frame to audio track).
     * @param frameInfo the frame info holding the frame buffer
     * @param offsetUs the offset from now when the frame should be rendered
     */
    open fun renderFrame(frameInfo: FrameInfo, offsetUs: Long) {
        releaseFrame(frameInfo)
    }

    /**
     * Renders the current frame instantly.
     * This only works if the decoder holds a current frame, e.g. after a seek.
     * @see .renderFrame
     */
    fun renderFrame() {
        if (mCurrentFrameInfo != null) renderFrame(mCurrentFrameInfo!!, 0)
    }

    /**
     * Dismisses a frame without rendering it.
     * @param frameInfo the frame info holding the frame buffer to dismiss
     */
    fun dismissFrame(frameInfo: FrameInfo) {
        releaseFrame(frameInfo)
    }

    /**
     * Dismisses the current frame.
     * This only works if the decoder holds a current frame, e.g. after a seek.
     */
    fun dismissFrame() {
        if (mCurrentFrameInfo != null) dismissFrame(mCurrentFrameInfo!!)
    }

    /**
     * Releases a frame and all its associated resources.
     * When overwritten, this method must release the output buffer through
     * [MediaCodec.releaseOutputBuffer] or [MediaCodec.releaseOutputBuffer],
     * and then release the frame info through [.releaseFrameInfo].
     *
     * @param frameInfo information about the current frame
     */
    fun releaseFrame(frameInfo: FrameInfo) {
        codec.releaseOutputBuffer(frameInfo.buffer, false)
        releaseFrameInfo(frameInfo)
    }

    /**
     * Releases the frame info back into the decoder for later reuse. This method must always be
     * called after handling a frame.
     *
     * @param frameInfo information about a frame
     */
    protected fun releaseFrameInfo(frameInfo: FrameInfo) {
        frameInfo.clear()
        mEmptyFrameInfos!!.add(frameInfo)
    }

    /**
     * Overwrite in subclass to handle a change of the output format.
     * @param format the new media format
     */
    protected open fun onOutputFormatChanged(format: MediaFormat?) {
        // nothing to do here
    }

    /**
     * Runs the decoder loop, optionally until a new frame is available.
     * The returned FrameInfo object keeps metadata of the decoded frame. To release its data,
     * call [.releaseFrame].
     *
     * @param skip skip frames of other tracks
     * @param force force decoding in a loop until a frame becomes available or the EOS is reached
     * @return a FrameInfo object holding metadata of a decoded frame or NULL if no frame has been decoded
     */
    @Throws(IOException::class)
    fun decodeFrame(skip: Boolean, force: Boolean): FrameInfo? {
        //Log.d(TAG, "decodeFrame");
        while (!isOutputEos) {
            // Dequeue decoded frames
            val frameInfo = dequeueDecodedFrame()

            // Enqueue encoded buffers into decoders
            while (queueSampleToCodec(skip)) {
            }
            if (frameInfo != null) {
                // If a frame has been decoded, return it
                return frameInfo
            }
            if (!force) {
                // If we have not decoded a frame and we're not forcing decoding until a frame becomes available, return null
                return null
            }
        }
        Log.d(TAG, "EOS NULL")
        return null // EOS already reached, no frame left to return
    }

    /**
     * Seeks to the specified target PTS with the specified seek mode. After the seek, the decoder
     * holds the frame from the target position which must either be rendered through [.renderFrame]
     * or dismissed through [.dismissFrame].
     *
     * @param seekMode the mode how the seek should be carried out
     * @param seekTargetTimeUs the target PTS to seek to
     * @throws IOException
     */
    @Throws(IOException::class)
    fun seekTo(seekMode: MediaPlayer.SeekMode, seekTargetTimeUs: Long) {
        currentDecodingPTS = PTS_NONE
        inputSamplePTS = PTS_UNKNOWN
        mCurrentFrameInfo = seekTo(seekMode, seekTargetTimeUs, mExtractor, codec)
    }

    /**
     * This method implements the actual seeking and can be overwritten by subclasses to implement
     * custom seeking methods.
     *
     * @see .seekTo
     */
    @Throws(IOException::class)
    protected open fun seekTo(
        seekMode: MediaPlayer.SeekMode, seekTargetTimeUs: Long,
        extractor: com.net.pslapllication.util.customvideoview.MediaExtractor, codec: MediaCodec
    ): FrameInfo? {
        if (isPassive) {
            // Even when not actively seeking, the codec must be flushed to get rid of left over
            // audio frames from the previous playback position and the EOS flags need to be reset too.
            isInputEos = false
            isOutputEos = false
            codec.flush()
            return null
        }
        Log.d(TAG, "seeking to:                 $seekTargetTimeUs")
        Log.d(TAG, "extractor current position: " + extractor.sampleTime)
        extractor.seekTo(seekTargetTimeUs, seekMode.baseSeekMode)
        Log.d(TAG, "extractor new position:     " + extractor.sampleTime)

        // TODO add seek cancellation possibility
        // e.g. by returning an object with a cancel method and checking the flag at fitting places within this method
        isInputEos = false
        isOutputEos = false
        codec.flush()
        if (extractor.hasTrackFormatChanged()) {
            reinitCodec()
            mRepresentationChanged = true
        }
        return decodeFrame(true, true)
    }

    /**
     * Releases the codec and its resources. Must be called when the decoder is no longer in use.
     */
    fun release() {
        codec.stop()
        codec.release()
        Log.d(TAG, "decoder released")
    }

    companion object {
        const val PTS_NONE = Long.MIN_VALUE
        const val PTS_EOS = Long.MAX_VALUE
        const val PTS_UNKNOWN: Long = -1
        private const val TIMEOUT_US: Long = 0
        const val INDEX_NONE = -1
    }

    init {
        // Apply the name of the concrete class that extends this base class to the logging tag
        // THis is really not a nice solution but there's no better one: http://stackoverflow.com/a/936724
        TAG = javaClass.simpleName
        require(!(extractor == null || trackIndex == INDEX_NONE)) { "no track specified" }
        mExtractor = extractor
        isPassive = passive
        mTrackIndex = trackIndex
        format = extractor.getTrackFormat(mTrackIndex)
        mOnDecoderEventListener = listener
        codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
        currentDecodingPTS = PTS_NONE
    }
}