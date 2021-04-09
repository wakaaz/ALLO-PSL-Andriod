package com.net.pslapllication.util.customvideoview

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.*
import java.util.List


internal class AudioPlayback() {
    private var mAudioFormat: MediaFormat? = null
    private var mAudioTrack: AudioTrack? = null
    private var mTransferBuffer: ByteArray?=null
    private var mFrameChunkSize: Int
    private var mFrameSize: Int = 0
    private var mSampleRate: Int = 0
    private val mBufferQueue: BufferQueue
    private var mPlaybackBufferSize: Int = 0
    private var mAudioThread: AudioThread? = null
    var lastPresentationTimeUs: Long = 0
        private set
    private var mAudioSessionId: Int
    var audioStreamType: Int
    private var mVolumeLeft: Float = 1f
    private var mVolumeRight: Float = 1f

    /**
     * Keeps track of the PTS of the moment when playback has started.
     * It is required to calculate the current PTS because the playback head
     * is reset to zero when playback is paused.
     */
    private var mPresentationTimeOffsetUs: Long = 0

    /**
     * Hold the previous playback head position time for comparison with the current playback
     * head position time to detect a position wrap/overflow.
     */
    private var mLastPlaybackHeadPositionUs: Long = 0

    /**
     * Initializes or reinitializes the audio track with the supplied format for playback
     * while keeping the playstate. Keeps the current configuration and skips reinitialization
     * if the new format is the same as the current format.
     */
    fun init(format: MediaFormat?) {
        Log.d(TAG, "init")
        var playing: Boolean = false
        if (isInitialized) {
            if (!checkIfReinitializationRequired(format)) {
                // Set new format that equals the old one (in case we compare references somewhere)
                mAudioFormat = format
                return
            }
            playing = isPlaying
            pause()
            stopAndRelease(false)
        } else {
            // deferred creation of the audio thread until its first use
            mAudioThread = AudioThread()
            mAudioThread!!.setPaused(true)
            mAudioThread!!.start()
        }
        mAudioFormat = format
        val channelCount: Int = format!!.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val bytesPerSample: Int = 2
        mFrameSize = bytesPerSample * channelCount
        mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        var channelConfig: Int = AudioFormat.CHANNEL_OUT_DEFAULT
        when (channelCount) {
            1 -> channelConfig = AudioFormat.CHANNEL_OUT_MONO
            2 -> channelConfig = AudioFormat.CHANNEL_OUT_STEREO
            4 -> channelConfig = AudioFormat.CHANNEL_OUT_QUAD
            6 -> channelConfig = AudioFormat.CHANNEL_OUT_5POINT1
            8 -> channelConfig = AudioFormat.CHANNEL_OUT_7POINT1
        }
        mPlaybackBufferSize = mFrameChunkSize * channelCount
        mAudioTrack = AudioTrack(
            audioStreamType,
            mSampleRate,
            channelConfig,
            AudioFormat.ENCODING_PCM_16BIT,
            mPlaybackBufferSize,  // at least twice the size to enable double buffering (according to docs)
            AudioTrack.MODE_STREAM, mAudioSessionId
        )
        if (mAudioTrack!!.state != AudioTrack.STATE_INITIALIZED) {
            stopAndRelease()
            throw IllegalStateException("audio track init failed")
        }
        mAudioSessionId = mAudioTrack!!.audioSessionId
        audioStreamType = mAudioTrack!!.streamType
        setStereoVolume(mVolumeLeft, mVolumeRight)
        mPresentationTimeOffsetUs = PTS_NOT_SET
        if (playing) {
            play()
        }
    }

    private fun checkIfReinitializationRequired(newFormat: MediaFormat?): Boolean {
        return (mAudioFormat!!.getInteger(MediaFormat.KEY_CHANNEL_COUNT) != newFormat!!.getInteger(
            MediaFormat.KEY_CHANNEL_COUNT
        )
                ) || (mAudioFormat!!.getInteger(MediaFormat.KEY_SAMPLE_RATE) != newFormat.getInteger(
            MediaFormat.KEY_SAMPLE_RATE
        )
                ) || !(mAudioFormat!!.getString(MediaFormat.KEY_MIME) == newFormat.getString(
            MediaFormat.KEY_MIME
        ))
    }

    /**
     * Can be used to set an audio session ID before calling [.init].
     */
    var audioSessionId: Int
        get() = mAudioSessionId
        set(sessionId) {
            if (isInitialized) {
                throw IllegalStateException("cannot set session id on an initialized audio track")
            }
            mAudioSessionId = sessionId
        }

    val isInitialized: Boolean
        get() = mAudioTrack != null && mAudioTrack!!.state == AudioTrack.STATE_INITIALIZED

    fun play() {
        //Log.d(TAG, "play");
        if (isInitialized) {
            mAudioTrack!!.play()
            mAudioThread!!.setPaused(false)
        } else {
            throw IllegalStateException()
        }
    }

    @JvmOverloads
    fun pause(flush: Boolean = true) {
        //Log.d(TAG, "pause(" + flush + ")");
        if (isInitialized) {
            mAudioThread!!.setPaused(true)
            mAudioTrack!!.pause()
            if (flush) {
                flush()
            }
        } else {
            throw IllegalStateException()
        }
    }

    fun flush() {
        if (isInitialized) {
            val playing: Boolean = isPlaying
            if (playing) {
                mAudioTrack!!.pause()
            }
            mAudioTrack!!.flush()
            mBufferQueue.flush()

            // Reset offset so it gets updated with the current PTS when playback continues
            mPresentationTimeOffsetUs = PTS_NOT_SET
            if (playing) {
                mAudioTrack!!.play()
            }
        } else {
            throw IllegalStateException()
        }
    }

    fun write(audioData: ByteBuffer, presentationTimeUs: Long) {
        val sizeInBytes: Int = audioData.remaining()

        // TODO find a way to determine the audio decoder max output frame size at configuration time
        if (mFrameChunkSize < sizeInBytes) {
            Log.d(
                TAG,
                "incoming frame chunk size increased to $sizeInBytes"
            )
            mFrameChunkSize = sizeInBytes
            // re-init the audio track to accommodate buffer to new chunk size
            init(mAudioFormat)
        }

        // Special handling of the first written audio buffer after a flush (pause with flush)
        if (mPresentationTimeOffsetUs == PTS_NOT_SET) {
            // Initialize with the PTS of the first audio buffer (which isn't necessarily zero)
            mPresentationTimeOffsetUs = presentationTimeUs
            mLastPlaybackHeadPositionUs = 0
            /** Handle playback head reset bug
             *
             * affected: Galaxy S2 API 16
             * not affected: Nexus 4 API 22
             *
             * Sometimes the playback head does not really reset to zero in a pause. During the
             * pause, it correctly returns zero (0), but when playback continues it sometimes
             * continues from the previous playback head position instead of starting from zero.
             * Since this does not always happen, this looks to be a bug in the Android framework.
             *
             * TODO find out if this is a reported bug
             *
             * To work around this issue, we subtract the playback head position time from the PTS
             * offset to adjust the base time by the playback head time. This leads to the
             * [.getCurrentPresentationTimeUs] method returning a correct value.
             */
            val playbackHeadPositionUs: Long = playbackheadPositionUs
            if (playbackHeadPositionUs > 0) {
                mPresentationTimeOffsetUs -= playbackHeadPositionUs
                Log.d(TAG, "playback head not reset")
            }
        }
        mBufferQueue.put(audioData, presentationTimeUs)
        //        Log.d(TAG, "buffer queue size " + mBufferQueue.bufferQueue.size()
//                + " data " + mBufferQueue.mQueuedDataSize
//                + " time " + getQueueBufferTimeUs());
        mAudioThread!!.notifyOfNewBufferInQueue()
    }

    private fun stopAndRelease(killThread: Boolean) {
        if (killThread && mAudioThread != null) {
            mAudioThread!!.interrupt()
        }
        if (mAudioTrack != null) {
            if (isInitialized) {
                mAudioTrack!!.stop()
            }
            mAudioTrack!!.release()
        }
        mAudioTrack = null
    }

    fun stopAndRelease() {
        stopAndRelease(true)
    }

    /**
     * Returns the length of the queued audio, that does not fit into the playback buffer yet.
     * @return the length of the queued audio in microsecs
     */
    val queueBufferTimeUs: Long
        get() = ((mBufferQueue.mQueuedDataSize / mFrameSize).toDouble()
                / mSampleRate * 1000000.0).toLong()

    /**
     * Returns the length of the playback buffer, without posidering the current playback position
     * inside the buffer (the remaining audio data that is waiting for playback can be less than
     * the buffer length).
     * @return the length of the playback buffer in microsecs
     */
    val playbackBufferTimeUs: Long
        get() {
            return ((mPlaybackBufferSize / mFrameSize).toDouble() / mSampleRate * 1000000.0).toLong()
        }

    // The playback head position is encoded as a uint in an int
    private val playbackheadPositionUs:
            // Convert frames to time
            Long
        private get() {
            // The playback head position is encoded as a uint in an int
            val playbackHeadPosition: Long =
                0xFFFFFFFFL and mAudioTrack!!.playbackHeadPosition.toLong()
            // Convert frames to time
            return (playbackHeadPosition.toDouble() / mSampleRate * 1000000).toLong()
        }// playback head position has wrapped around it's 32bit uint value
    // Add the full runtime to the PTS offset to advance it one playback head iteration

    // Return the playback head time, offset by the start offset PTS
// Return the PTS_NOT_SET flag when the PTS has not been initialized yet. At the start of
    // media playback, returning the playback head alone is reliable, but later on (e.g. after a
    // seek), a missing PTS offset leads to totally wrong values.

    // Handle playback head wrapping

    /**
     * Returns the current PTS of the playback head or PTS_NOT_SET if the PTS cannot be reliably
     * calculated yet.
     * For this method to return a PTS, audio samples need to be written before ([.write].
     * @return the PTS at the playback head or PTS_NOT_SET if unknown
     */
    val currentPresentationTimeUs: Long
        get() {
            // Return the PTS_NOT_SET flag when the PTS has not been initialized yet. At the start of
            // media playback, returning the playback head alone is reliable, but later on (e.g. after a
            // seek), a missing PTS offset leads to totally wrong values.
            if (mPresentationTimeOffsetUs == PTS_NOT_SET) {
                return PTS_NOT_SET
            }
            val playbackHeadPositionUs: Long = playbackheadPositionUs

            // Handle playback head wrapping
            if (playbackHeadPositionUs < mLastPlaybackHeadPositionUs) {
                // playback head position has wrapped around it's 32bit uint value
                Log.d(TAG, "playback head has wrapped")
                // Add the full runtime to the PTS offset to advance it one playback head iteration
                mPresentationTimeOffsetUs += ((-0x1).toDouble() / mSampleRate * 1000000).toLong()
            }
            mLastPlaybackHeadPositionUs = playbackHeadPositionUs

            // Return the playback head time, offset by the start offset PTS
            return mPresentationTimeOffsetUs + playbackHeadPositionUs
        }

    fun setPlaybackSpeed(speed: Float) {
        if (isInitialized) {
            mAudioTrack!!.playbackRate = (mSampleRate * speed).toInt()
        } else {
            throw IllegalStateException()
        }
    }

    val isPlaying: Boolean
        get() {
            return mAudioTrack!!.playState == AudioTrack.PLAYSTATE_PLAYING
        }

    private fun writeToPlaybackBuffer(
        audioData: ByteBuffer,
        presentationTimeUs: Long
    ) {
        val size: Int = audioData.remaining()
        if (mTransferBuffer == null || mTransferBuffer!!.size < size) {
            mTransferBuffer = ByteArray(size)
        }
        audioData.get(mTransferBuffer, 0, size)

        //Log.d(TAG, "audio write / chunk count " + mPlaybackBufferChunkCount);
        lastPresentationTimeUs = presentationTimeUs
        mAudioTrack!!.write(mTransferBuffer!!, 0, size)
    }

    /**
     * @see android.media.AudioTrack.setStereoVolume
     */
    @Deprecated("deprecated in API21, prefer use of {@link #setVolume(float)}")
    fun setStereoVolume(leftGain: Float, rightGain: Float) {
        mVolumeLeft = leftGain
        mVolumeRight = rightGain
        if (mAudioTrack != null) {
            mAudioTrack!!.setStereoVolume(leftGain, rightGain)
        }
    }

    /**
     * @see android.media.AudioTrack.setVolume
     */
    fun setVolume(gain: Float) {
        //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
        //mAudioTrack.setVolume(gain);
        setStereoVolume(gain, gain)
    }

    /*
     * This thread reads buffers from the queue and supplies them to the playback buffer. If the
     * queue is empty, it waits until a buffer item becomes available. If the playback buffer is
     * full, it blocks until it empties because of the AudioTrack#write blocking behaviour, and
     * since this is a separate audio thread, it does not block the video playback thread.
     * The thread is necessary because the AudioTrack#setPositionNotificationPeriod + listener
     * combination does only seem top work reliably if the written frame chunk sizes are constant
     * and the notification period is set to exactly this chunk size, which is impossible when
     * dealing with variable chunk sizes. Workarounds would be to set the notification period to the
     * least common multiple and split the written chunk also in pieces of this size (not sure if
     * very small notifications work though), or to add a transformation layer in the queue that
     * redistributes the incoming chunks of variable size into chunks of constant size; both
     * solutions would be more complex than this thread and also add noticeable overhead (many
     * method calls in the first workaround, many data copy operations in the second).
     */
    private inner class AudioThread internal constructor() :
        Thread(TAG) {
        private val SYNC = Object()
        private var mPaused: Boolean = true
        fun setPaused(paused: Boolean) {
            mPaused = paused
            synchronized(this) {SYNC.notify() }
        }

        fun notifyOfNewBufferInQueue() {
            synchronized(SYNC) { SYNC.notify() }
        }

        override fun run() {
            while (!isInterrupted) {
                try {
                    synchronized(this) {
                        while (mPaused) {
                            SYNC.wait()
                        }
                    }
                    var bufferItem: BufferQueue.Item? = null
                    synchronized(SYNC) {
                        while ((mBufferQueue.take().also { bufferItem = it }) == null) {
                            SYNC.wait()
                        }
                    }
                    writeToPlaybackBuffer(bufferItem!!.buffer, bufferItem!!.presentationTimeUs)
                    mBufferQueue.put(bufferItem)
                } catch (e: InterruptedException) {
                    interrupt()
                }
            }
        }

    }

    /**
     * Intermediate buffer queue for audio chunks. When an audio chunk is decoded, it is put into
     * this queue until the audio track periodic notification event gets fired, telling that a certain
     * amount of the audio playback buffer has been consumed, which then enqueues another chunk to
     * the playback output buffer.
     */
    private class BufferQueue internal constructor() {
        class Item internal constructor(size: Int) {
            var buffer: ByteBuffer
            var presentationTimeUs: Long = 0

            init {
                buffer = ByteBuffer.allocate(size)
            }
        }

        private var bufferSize: Int = 0
        private val bufferQueue: Queue<Item?>
        private val emptyBuffers: MutableList<Item?>
        var mQueuedDataSize: Int = 0

        @Synchronized
        fun put(data: ByteBuffer, presentationTimeUs: Long) {
            //Log.d(TAG, "put");
            if (data.remaining() > bufferSize) {
                /* Buffer size has increased, invalidate all empty buffers since they can not be
                 * reused any more. */
                emptyBuffers.clear()
                bufferSize = data.remaining()
            }
            val item: Item?
            if (!emptyBuffers.isEmpty()) {
                item = emptyBuffers.removeAt(0)
            } else {
                item = Item(data.remaining())
            }
            item!!.buffer.limit(data.remaining())
            item.buffer.mark()
            item.buffer.put(data)
            item.buffer.reset()
            item.presentationTimeUs = presentationTimeUs
            bufferQueue.add(item)
            mQueuedDataSize += item.buffer.remaining()
        }

        /**
         * Takes a buffer item out of the queue to read the data. Returns NULL if there is no
         * buffer ready.
         */
        @Synchronized
        fun take(): Item? {
            //Log.d(TAG, "take");
            val item: Item? = bufferQueue.poll()
            if (item != null) {
                mQueuedDataSize -= item.buffer.remaining()
            }
            return item
        }

        /**
         * Returns a buffer to the queue for reuse.
         */
        @Synchronized
        fun put(returnItem: Item?) {
            if (returnItem!!.buffer.capacity() != bufferSize) {
                /* The buffer size has changed and the returned buffer is not valid any more and
                 * can be discarded. */
                return
            }
            returnItem.buffer.rewind()
            emptyBuffers.add(returnItem)
        }

        /**
         * Removes all remaining buffers from the queue and returns them to the empty-item store.
         */
        @Synchronized
        fun flush() {
            var item: Item?
            while ((bufferQueue.poll().also { item = it }) != null) {
                put(item)
            }
            mQueuedDataSize = 0
        }

        init {
            bufferQueue = LinkedList()
            emptyBuffers = ArrayList()
        }
    }

    companion object {
        private val TAG: String = AudioPlayback::class.java.simpleName
        var PTS_NOT_SET: Long = Long.MIN_VALUE
    }

    init {
        mFrameChunkSize = 4096 * 2 // arbitrary default chunk size
        mBufferQueue = BufferQueue()
        mAudioSessionId = 0 // AudioSystem.AUDIO_SESSION_ALLOCATE;
        audioStreamType = AudioManager.STREAM_MUSIC
    }
}