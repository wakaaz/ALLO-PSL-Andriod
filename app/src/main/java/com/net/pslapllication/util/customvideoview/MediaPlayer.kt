package com.net.pslapllication.util.customvideoview

import android.content.Context
import android.media.AudioManager
import android.media.MediaCodec
import android.media.MediaFormat
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import com.net.pslapllication.util.customvideoview.MediaCodecDecoder.FrameInfo
import com.net.pslapllication.util.customvideoview.MediaCodecDecoder.OnDecoderEventListener
import java.io.IOException


class MediaPlayer {




    enum class SeekMode(baseSeekMode: Int) {
        /**
         * Seeks to the previous sync point.
         * This mode exists for backwards compatibility and is the same as [.FAST_TO_PREVIOUS_SYNC].
         */
        FAST(MediaExtractor.SEEK_TO_PREVIOUS_SYNC),

        /**
         * Seeks to the previous sync point.
         * This seek mode equals Android MediaExtractor's [android.media.MediaExtractor.SEEK_TO_PREVIOUS_SYNC].
         */
        FAST_TO_PREVIOUS_SYNC(MediaExtractor.SEEK_TO_PREVIOUS_SYNC),

        /**
         * Seeks to the next sync point.
         * This seek mode equals Android MediaExtractor's [android.media.MediaExtractor.SEEK_TO_NEXT_SYNC].
         */
        FAST_TO_NEXT_SYNC(MediaExtractor.SEEK_TO_NEXT_SYNC),

        /**
         * Seeks to to the closest sync point.
         * This seek mode equals Android MediaExtractor's [android.media.MediaExtractor.SEEK_TO_CLOSEST_SYNC].
         */
        FAST_TO_CLOSEST_SYNC(MediaExtractor.SEEK_TO_CLOSEST_SYNC),

        /**
         * Seeks to the exact frame if the seek time equals the frame time, else
         * to the following frame; this means that it will often seek one frame too far.
         */
        PRECISE(MediaExtractor.SEEK_TO_PREVIOUS_SYNC),

        /**
         * Default mode.
         * Always seeks to the exact frame. Can cost maximally twice the time than the PRECISE mode.
         */
        EXACT(MediaExtractor.SEEK_TO_PREVIOUS_SYNC),

        /**
         * Always seeks to the exact frame by skipping the decoding of all frames between the sync
         * and target frame, because of which it can result in block artifacts.
         */
        FAST_EXACT(MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

        var baseSeekMode =
            MediaExtractor.SEEK_TO_PREVIOUS_SYNC

        init {
            this.baseSeekMode = baseSeekMode
        }
    }

    /**
     * The mode of how to delay rendering of video frames until their target PTS.
     */
    enum class VideoRenderTimingMode {
        /**
         * Automatically chooses [VideoRenderTimingMode.SLEEP] for API < 21 and
         * [VideoRenderTimingMode.SURFACEVIEW_TIMESTAMP_API21] for API >= 21.
         */
        AUTO,

        /**
         * Defers rendering by putting the playback thread to sleep until the PTS is reached and renders
         * frames through [MediaCodec.releaseOutputBuffer].
         */
        SLEEP,

        /**
         * Defers rendering through [MediaCodec.releaseOutputBuffer] which blocks
         * until the PTS is reached. Supported on API 21+.
         */
        SURFACEVIEW_TIMESTAMP_API21;

        val isRenderModeApi21: Boolean
            get() {
                return when (this) {
                    AUTO -> Build.VERSION.SDK_INT >= 21
                    SLEEP -> false
                    SURFACEVIEW_TIMESTAMP_API21 -> true
                }
                return false
            }
    }

    private enum class State {
        IDLE, INITIALIZED, PREPARING, PREPARED, STOPPED, RELEASING, RELEASED, ERROR
    }

    var seekMode = SeekMode.EXACT
    private var mSurface: Surface? = null
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mVideoExtractor: MediaExtractor? = null
    private var mAudioExtractor: MediaExtractor? = null
    private var mVideoTrackIndex = 0
    private var mVideoFormat: MediaFormat? = null
    private var mVideoMinPTS: Long = 0
    private var mAudioTrackIndex = 0
    private var mAudioFormat: MediaFormat? = null
    private var mAudioMinPTS: Long = 0
    private var mAudioSessionId: Int

    /**
     * Gets the stream type of the audio playback session.
     * @return the stream type
     */// Can be set any time, no IllegalStateException is thrown, but value will be ignored if audio is already initialized
    var audioStreamType: Int
    private var mVolumeLeft = 1f
    private var mVolumeRight = 1f
    private var mPlaybackThread: PlaybackThread? = null
    private var mCurrentPosition: Long = 0
    private var mSeekTargetTime: Long = 0
    private var mSeeking = false
    var bufferPercentage = 0
        private set
    private val mTimeBase: TimeBase
    private val mEventHandler: EventHandler
    private var mOnPreparedListener: OnPreparedListener? = null
    private var mOnCompletionListener: OnCompletionListener? = null
    private var mOnSeekListener: OnSeekListener? = null
    private var mOnSeekCompleteListener: OnSeekCompleteListener? = null
    private var mOnErrorListener: OnErrorListener? = null
    private var mOnInfoListener: OnInfoListener? = null
    private var mOnVideoSizeChangedListener: OnVideoSizeChangedListener? = null
    private var mOnBufferingUpdateListener: OnBufferingUpdateListener? = null
    private var mOnCueListener: OnCueListener? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var mScreenOnWhilePlaying = false
    private var mStayAwake = false
    /**
     * @see android.media.MediaPlayer.isLooping
     */
    /**
     * @see android.media.MediaPlayer.setLooping
     */
    var isLooping = false
    private var mAudioPlayback: AudioPlayback? = null
    private var mDecoders: Decoders? = null
    private var mBuffering = false
    private var mVideoRenderTimingMode: VideoRenderTimingMode
    private val mCueTimeline: Timeline
    private var mCurrentState: State

    /**
     * A lock to sync release() with the actual releasing on the playback thread. This lock makes
     * sure that release() waits until everything has been released before returning to the caller,
     * and thus makes the async release look synchronized to an API caller.
     */
    private var mReleaseSyncLock: Object? = null

    /**
     * Sets the media source and track indices. The track indices can either be actual track indices
     * that have been determined externally, [.TRACK_INDEX_AUTO] to automatically select
     * the first fitting track index, or [.TRACK_INDEX_NONE] to not select any track.
     *
     * @param source the media source
     * @param videoTrackIndex a video track index or one of the TRACK_INDEX_* constants
     * @param audioTrackIndex an audio track index or one of the TRACK_INDEX_* constants
     * @throws IOException
     * @throws IllegalStateException
     */
    @Throws(IOException::class, IllegalStateException::class)
    fun setDataSource(
        source: MediaSource,
        videoTrackIndex: Int,
        audioTrackIndex: Int
    ) {
        check(mCurrentState == State.IDLE)
        releaseMediaExtractors()
        mVideoExtractor = source.videoExtractor
        mAudioExtractor = source.audioExtractor
        if (mVideoExtractor != null && mAudioExtractor == null) {
            mAudioExtractor = mVideoExtractor
        }
        mVideoTrackIndex = when (videoTrackIndex) {
            TRACK_INDEX_AUTO -> getTrackIndex(mVideoExtractor, "video/")
            TRACK_INDEX_NONE -> MediaCodecDecoder.INDEX_NONE
            else -> videoTrackIndex
        }
        mAudioTrackIndex = when (audioTrackIndex) {
            TRACK_INDEX_AUTO -> getTrackIndex(mAudioExtractor, "audio/")
            TRACK_INDEX_NONE -> MediaCodecDecoder.INDEX_NONE
            else -> audioTrackIndex
        }

        // Select video track
        if (mVideoTrackIndex != MediaCodecDecoder.INDEX_NONE) {
            mVideoExtractor!!.selectTrack(mVideoTrackIndex)
            mVideoFormat = mVideoExtractor!!.getTrackFormat(mVideoTrackIndex)
            mVideoMinPTS = mVideoExtractor!!.sampleTime
            Log.d(
                TAG,
                "selected video track #" + mVideoTrackIndex + " " + mVideoFormat.toString()
            )
        }

        // Select audio track
        if (mAudioTrackIndex != MediaCodecDecoder.INDEX_NONE) {
            mAudioExtractor!!.selectTrack(mAudioTrackIndex)
            mAudioFormat = mAudioExtractor!!.getTrackFormat(mAudioTrackIndex)
            mAudioMinPTS = mAudioExtractor!!.sampleTime
            Log.d(
                TAG,
                "selected audio track #" + mAudioTrackIndex + " " + mAudioFormat.toString()
            )
        }
        if (mVideoTrackIndex == MediaCodecDecoder.INDEX_NONE) {
            mVideoExtractor = null
        }
        if (mVideoTrackIndex == MediaCodecDecoder.INDEX_NONE && mAudioTrackIndex == MediaCodecDecoder.INDEX_NONE) {
            throw IOException("invalid data source, no supported stream found")
        }
        if (mVideoTrackIndex != MediaCodecDecoder.INDEX_NONE && mPlaybackThread == null && mSurface == null) {
            Log.i(TAG, "no video output surface specified")
        }
        mCurrentState = State.INITIALIZED
    }

    private fun releaseMediaExtractors() {
        // Audio and video extractors could be the same object,
        // but calling release twice does not hurt.
        if (mAudioExtractor != null) {
            mAudioExtractor!!.release()
            mAudioExtractor = null
        }
        if (mVideoExtractor != null) {
            mVideoExtractor!!.release()
            mVideoExtractor = null
        }
    }

    /**
     * Sets the media source and automatically selects fitting tracks.
     *
     * @param source the media source
     * @throws IOException
     * @throws IllegalStateException
     */
    @Throws(IOException::class, IllegalStateException::class)
    fun setDataSource(source: MediaSource) {
        setDataSource(
            source,
            TRACK_INDEX_AUTO,
            TRACK_INDEX_AUTO
        )
    }

    private fun getTrackIndex(
        mediaExtractor: MediaExtractor?,
        mimeType: String
    ): Int {
        if (mediaExtractor == null) {
            return MediaCodecDecoder.INDEX_NONE
        }
        for (i in 0 until mediaExtractor.trackCount) {
            val format = mediaExtractor.getTrackFormat(i)
            Log.d(TAG, format.toString())
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith(mimeType)) {
                return i
            }
        }
        return MediaCodecDecoder.INDEX_NONE
    }

    /**
     * @see android.media.MediaPlayer.setDataSource
     */
    @Deprecated("only for compatibility with Android API")
    @Throws(IOException::class)
    fun setDataSource(
        context: Context?,
        uri: Uri?,
        headers: Map<String, String>?
    ) {
        setDataSource(UriSource(context!!, uri!!, headers))
    }

    /**
     * @see android.media.MediaPlayer.setDataSource
     */
    @Deprecated("only for compatibility with Android API")
    @Throws(IOException::class)
    fun setDataSource(context: Context?, uri: Uri?) {
        setDataSource(context, uri, null)
    }

    @Throws(IOException::class, IllegalStateException::class)
    private fun prepareInternal() {
        mCueTimeline.reset()
        val decoderEventListener: OnDecoderEventListener = object : OnDecoderEventListener {
            override fun onBuffering(decoder: MediaCodecDecoder?) {
                // Enter buffering mode (playback pause) if cached amount is below water mark
                // Do not enter buffering mode is player is already paused (buffering mode will be
                // entered when playback is started and buffer is too empty).
                if (mPlaybackThread != null && !mPlaybackThread!!.isPaused
                    && !mBuffering
                    && mDecoders!!.cachedDuration < BUFFER_LOW_WATER_MARK_US && !mDecoders!!.hasCacheReachedEndOfStream()
                ) {
                    mBuffering = true
                    mEventHandler.sendMessage(
                        mEventHandler.obtainMessage(
                            MEDIA_INFO,
                            MEDIA_INFO_BUFFERING_START, 0
                        )
                    )
                }
            }
        }
        if (mCurrentState == State.RELEASING) {
            // release() has already been called, drop out of prepareAsync() (can only happen with async prepare)
            return
        }
        mDecoders = Decoders()
        if (mVideoTrackIndex != MediaCodecDecoder.INDEX_NONE) {
            try {
                val vd: MediaCodecDecoder = MediaCodecVideoDecoder(
                    mVideoExtractor!!, false, mVideoTrackIndex,
                    decoderEventListener, mSurface!!, mVideoRenderTimingMode.isRenderModeApi21
                )
                mDecoders!!.addDecoder(vd)
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "cannot create video decoder: " + e.message
                )
            }
        }
        if (mAudioTrackIndex != MediaCodecDecoder.INDEX_NONE) {
            mAudioPlayback = AudioPlayback()
            // Initialize settings in case they have already been set before the preparation
            mAudioPlayback!!.audioSessionId = mAudioSessionId
            setVolume(mVolumeLeft, mVolumeRight) // sets the volume on mAudioPlayback
            try {
                val passive =
                    mAudioExtractor == mVideoExtractor || mAudioExtractor == null
                val ad: MediaCodecDecoder = MediaCodecAudioDecoder(
                    if (mAudioExtractor != null) mAudioExtractor else mVideoExtractor,
                    passive, mAudioTrackIndex, decoderEventListener, mAudioPlayback!!
                )
                mDecoders!!.addDecoder(ad)
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "cannot create audio decoder: " + e.message
                )
                mAudioPlayback = null
            }
        }

        // If no decoder could be initialized, there is nothing to play back, so we throw an exception
        if (mDecoders!!.decoders.isEmpty()) {
            throw IOException("cannot decode any stream")
        }
        if (mAudioPlayback != null) {
            mAudioSessionId = mAudioPlayback!!.audioSessionId
            audioStreamType = mAudioPlayback!!.audioStreamType
        }

        // After the decoder is initialized, we know the video size
        if (mDecoders!!.videoDecoder != null) {
            var width = mDecoders!!.videoDecoder!!.videoWidth
            var height = mDecoders!!.videoDecoder!!.videoHeight
            val rotation = mDecoders!!.videoDecoder!!.videoRotation

            // Swap width/height to report correct dimensions of rotated portrait video (rotated by 90 or 270 degrees)
            if (rotation > 0 && rotation != 180) {
                val temp = width
                width = height
                height = temp
            }
            mEventHandler.sendMessage(
                mEventHandler.obtainMessage(
                    MEDIA_SET_VIDEO_SIZE,
                    width,
                    height
                )
            )
        }
        if (mCurrentState == State.RELEASING) {
            // release() has already been called, drop out of prepareAsync()
            return
        }

        // Decode the first frame to initialize the decoder, and seek back to the start
        // This is necessary on some platforms, else a seek directly after initialization will fail,
        // or the decoder goes into a state where it does not accept any input and does not deliver
        // any output, locking up playback (observed on N4 API22).
        // N4 API22 Test: disable this code open video, seek to end, press play to start from beginning
        //                -> results in infinite decoding loop without output
        if (true) {
            if (mDecoders!!.videoDecoder != null) {
                val vfi = mDecoders!!.decodeFrame(true)
                mDecoders!!.videoDecoder!!.releaseFrame(vfi!!)
            } else {
                mDecoders!!.decodeFrame(false)
            }
            if (mAudioPlayback != null) mAudioPlayback!!.pause(true)
            mDecoders!!.seekTo(SeekMode.FAST_TO_PREVIOUS_SYNC, 0)
        }
    }

    /**
     * @see android.media.MediaPlayer.prepare
     */
    @Throws(IOException::class, IllegalStateException::class)
    fun prepare() {
        check(!(mCurrentState != State.INITIALIZED && mCurrentState != State.STOPPED))
        mCurrentState = State.PREPARING

        // Prepare synchronously on caller thread
        prepareInternal()

        // Create the playback loop handler thread
        mPlaybackThread = PlaybackThread()
        mPlaybackThread!!.start()
        mCurrentState = State.PREPARED
    }

    /**
     * @see android.media.MediaPlayer.prepareAsync
     */
    @Throws(IllegalStateException::class)
    fun prepareAsync() {
        check(!(mCurrentState != State.INITIALIZED && mCurrentState != State.STOPPED))
        mCurrentState = State.PREPARING

        // Create the playback loop handler thread
        mPlaybackThread = PlaybackThread()
        mPlaybackThread!!.start()

        // Execute prepare asynchronously on playback thread
        mPlaybackThread!!.prepare()
    }

    /**
     * @see android.media.MediaPlayer.setDisplay
     */
    fun setDisplay(sh: SurfaceHolder?) {
        mSurfaceHolder = sh
        mSurface = sh?.surface
        if (mDecoders != null && mDecoders!!.videoDecoder != null) {
            //mDecoders.getVideoDecoder().updateSurface(mSurface);
        }
        if (mPlaybackThread == null) {
            // Player not prepared yet, so we can set the timing mode
            setVideoRenderTimingMode(VideoRenderTimingMode.AUTO)
            updateSurfaceScreenOn()
        } else {
            // Player is already prepared, just change the surface
            mPlaybackThread!!.setSurface(mSurface)
        }
    }

    /**
     * @see android.media.MediaPlayer.setSurface
     */
    fun setSurface(surface: Surface?) {
        mSurface = surface
        if (mScreenOnWhilePlaying && surface != null) {
            Log.w(
                TAG,
                "setScreenOnWhilePlaying(true) is ineffective for Surface"
            )
        }
        mSurfaceHolder = null
        if (mPlaybackThread == null) {
            // Player not prepared yet, so we can set the timing mode
            setVideoRenderTimingMode(VideoRenderTimingMode.SLEEP) // the surface could be a GL texture, so we switch to sleep timing mode
            updateSurfaceScreenOn()
        } else {
            // Player is already prepared, just change the surface
            mPlaybackThread!!.setSurface(mSurface)
        }
    }

    fun start() {
        if (mCurrentState != State.PREPARED) {
            mCurrentState = State.ERROR
            throw IllegalStateException()
        }
        mPlaybackThread!!.play()
        stayAwake(true)
    }

    fun pause() {
        if (mCurrentState != State.PREPARED) {
            mCurrentState = State.ERROR
            throw IllegalStateException()
        }
        mPlaybackThread!!.pause()
        stayAwake(false)
    }

    fun seekTo(usec: Long) {
        if (mCurrentState.ordinal < State.PREPARED.ordinal && mCurrentState.ordinal >= State.RELEASING.ordinal) {
            mCurrentState = State.ERROR
            throw IllegalStateException()
        }

        /* A seek needs to be performed in the decoding thread to execute commands in the correct
         * order. Otherwise it can happen that, after a seek in the media decoder, seeking procedure
         * starts, then a frame is decoded, and then the codec is flushed; the PTS of the decoded frame
         * then interferes the seeking procedure, the seek stops prematurely and a wrong waiting time
         * gets calculated. */Log.d(
            TAG,
            "seekTo $usec with video sample offset $mVideoMinPTS"
        )
        if (mOnSeekListener != null) {
            mOnSeekListener!!.onSeek(this@MediaPlayer)
        }
        mSeeking = true
        // The passed in target time is always aligned to a zero start time, while the actual video
        // can have an offset and must not necessarily start at zero. The offset can e.g. come from
        // the CTTS box SampleOffset field, and is only reported on Android 5+. In Android 4, the
        // offset is handled by the framework, not reported, and videos always start at zero.
        // By adding the offset to the seek target time, we always seek to a zero-reference time in
        // the stream.
        mSeekTargetTime = mVideoMinPTS + usec
        mPlaybackThread!!.seekTo(mSeekTargetTime)
    }

    fun seekTo(msec: Int) {
        seekTo(msec * 1000L)
    }

    /**
     * Gets the current playback speed. See [.setPlaybackSpeed] for details.
     * @return the current playback speed
     */
    /**
     * Sets the playback speed. Can be used for fast forward and slow motion.
     * The speed must not be negative but can otherwise be set to any value. The player will not
     * skip frames though and only playback at the maximum speed that the device can decode and
     * process (setting 10x speed thus will not lead to an actual 10x speedup).
     *
     * speed 0.5 = half speed / slow motion
     * speed 2.0 = double speed / fast forward
     * speed 0.0 equals to pause
     *
     * @param speed the playback speed to set
     * @throws IllegalArgumentException if the speed is negative
     */
    var playbackSpeed: Float
        get() = mTimeBase.speed.toFloat()
        set(speed) {
            require(speed >= 0) { "speed cannot be negative" }
            mTimeBase.speed = speed.toDouble()
            mTimeBase.startAt(mCurrentPosition)
        }

    val isPlaying: Boolean
        get() {
            if (mCurrentState.ordinal >= State.RELEASING.ordinal) {
                mCurrentState = State.ERROR
                throw IllegalStateException()
            }
            return mPlaybackThread != null && !mPlaybackThread!!.isPaused
        }

    /**
     * Stops the player and releases the playback thread. The player will consume minimal resources
     * after calling this method. To continue playback, the player must first be prepared with
     * [.prepare] or [.prepareAsync].
     */
    fun stop() {
        if (mPlaybackThread != null) {
            // Create a new lock object for this release cycle
            mReleaseSyncLock = Object()
            synchronized(mReleaseSyncLock!!) {
                try {
                    // Schedule release on the playback thread
                    val awaitingRelease = mPlaybackThread!!.release()
                    mPlaybackThread = null

                    // Wait for the release on the playback thread to finish
                    if (awaitingRelease) {
                        mReleaseSyncLock!!.wait()
                    }
                } catch (e: InterruptedException) {
                    // nothing to do here
                }
            }
            mReleaseSyncLock = null
        }
        stayAwake(false)
        mAudioPlayback = null
        mCurrentState = State.STOPPED
    }

    /**
     * Resets the player to its initial state, similar to a freshly created instance. To reuse the
     * player instance, set a data source and call [.prepare] or [.prepareAsync].
     */
    fun reset() {
        stop()
        mCurrentState = State.IDLE
    }

    /**
     * Stops the player and releases all resources (e.g. memory, codecs, event listeners). Once
     * the player instance is released, it cannot be used any longer.
     * Call this method as soon as you're finished using the player instance, and latest when
     * destroying the activity or fragment that contains this player. Not releasing the player can
     * lead to memory leaks.
     */
    fun release() {
        if (mCurrentState == State.RELEASING || mCurrentState == State.RELEASED) {
            return
        }
        mCurrentState = State.RELEASING
        stop()
        releaseMediaExtractors()
        mCurrentState = State.RELEASED

        // Listeners must not be invoked after the player is released so we clear them here
        // https://github.com/protyposis/MediaPlayer-Extended/issues/66
        mOnBufferingUpdateListener = null
        mOnCompletionListener = null
        mOnErrorListener = null
        mOnInfoListener = null
        mOnPreparedListener = null
        mOnSeekCompleteListener = null
        mOnSeekListener = null
        mOnVideoSizeChangedListener = null
    }

    /**
     * @see android.media.MediaPlayer.setWakeMode
     */
    fun setWakeMode(context: Context, mode: Int) {
        var washeld = false
        if (mWakeLock != null) {
            if (mWakeLock!!.isHeld) {
                washeld = true
                mWakeLock!!.release()
            }
            mWakeLock = null
        }
        val pm =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock =
            pm.newWakeLock(mode or PowerManager.ON_AFTER_RELEASE, MediaPlayer::class.java.name)
        mWakeLock!!.setReferenceCounted(false)
        if (washeld) {
            mWakeLock!!.acquire()
        }
    }

    /**
     * @see android.media.MediaPlayer.setScreenOnWhilePlaying
     */
    fun setScreenOnWhilePlaying(screenOn: Boolean) {
        if (mScreenOnWhilePlaying != screenOn) {
            if (screenOn && mSurfaceHolder == null) {
                Log.w(
                    TAG,
                    "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder"
                )
            }
            mScreenOnWhilePlaying = screenOn
            updateSurfaceScreenOn()
        }
    }

    private fun stayAwake(awake: Boolean) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock!!.isHeld) {
                mWakeLock!!.acquire()
            } else if (!awake && mWakeLock!!.isHeld) {
                mWakeLock!!.release()
            }
        }
        mStayAwake = awake
        updateSurfaceScreenOn()
    }

    private fun updateSurfaceScreenOn() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder!!.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake)
        }
    }

    val duration: Int
        get() {
            if (mCurrentState.ordinal <= State.PREPARING.ordinal && mCurrentState.ordinal >= State.RELEASING.ordinal) {
                mCurrentState = State.ERROR
                throw IllegalStateException()
            }
            return if (mVideoFormat != null) (mVideoFormat!!.getLong(MediaFormat.KEY_DURATION) / 1000).toInt() else if (mAudioFormat != null && mAudioFormat!!.containsKey(
                    MediaFormat.KEY_DURATION
                )
            ) (mAudioFormat!!.getLong(MediaFormat.KEY_DURATION) / 1000).toInt() else 0
        }

    /* During a seek, return the temporary seek target time; otherwise a seek bar doesn't
         * update to the selected seek position until the seek is finished (which can take a
         * while in exact mode). */
    val currentPosition: Int
        get() {
            if (mCurrentState.ordinal >= State.RELEASING.ordinal) {
                mCurrentState = State.ERROR
                throw IllegalStateException()
            }
            /* During a seek, return the temporary seek target time; otherwise a seek bar doesn't
             * update to the selected seek position until the seek is finished (which can take a
             * while in exact mode). */return ((if (mSeeking) mSeekTargetTime else mCurrentPosition) / 1000).toInt()
        }

    val videoWidth: Int
        get() {
            if (mCurrentState.ordinal >= State.RELEASING.ordinal) {
                mCurrentState = State.ERROR
                throw IllegalStateException()
            }
            return if (mVideoFormat != null) (mVideoFormat!!.getInteger(MediaFormat.KEY_HEIGHT)
                    * mVideoFormat!!.getFloat(MediaExtractor.MEDIA_FORMAT_EXTENSION_KEY_DAR)).toInt() else 0
        }

    val videoHeight: Int
        get() {
            if (mCurrentState.ordinal >= State.RELEASING.ordinal) {
                mCurrentState = State.ERROR
                throw IllegalStateException()
            }
            return if (mVideoFormat != null) mVideoFormat!!.getInteger(MediaFormat.KEY_HEIGHT) else 0
        }

    /**
     * @see android.media.MediaPlayer.setVolume
     */
    fun setVolume(leftVolume: Float, rightVolume: Float) {
        mVolumeLeft = leftVolume
        mVolumeRight = rightVolume
        if (mAudioPlayback != null) {
            mAudioPlayback!!.setStereoVolume(leftVolume, rightVolume)
        }
    }

    /**
     * This API method in the Android MediaPlayer is hidden, but may be unhidden in the future. Here
     * it can already be used.
     * see android.media.MediaPlayer#setVolume(float)
     */
    fun setVolume(volume: Float) {
        setVolume(volume, volume)
    }

    /**
     * @see android.media.MediaPlayer.getAudioSessionId
     */
    /**
     * @see android.media.MediaPlayer.setAudioSessionId
     */
    var audioSessionId: Int
        get() = mAudioSessionId
        set(sessionId) {
            check(mCurrentState == State.IDLE)
            mAudioSessionId = sessionId
        }

    /**
     * Sets the timing mode for video frame rendering.
     * This only works before the calling [.prepare] or [.prepareAsync].
     *
     * This method is only needed for the special case of rendering the video to a GL surface texture,
     * where [MediaCodec.releaseOutputBuffer] does not defer the frame rendering
     * and thus does not block until the PTS is reached. This only seems to work correctly on a
     * [android.view.SurfaceView]. It is therefore required to manually set the
     * [VideoRenderTimingMode.SLEEP] mode on API 21+ platforms to get timed frame rendering.
     *
     * TODO find out how to get deferred/blocking rendering to work with a surface texture
     *
     * @see VideoRenderTimingMode
     *
     * @param mode the desired timing mode
     * @throws IllegalStateException
     */
    fun setVideoRenderTimingMode(mode: VideoRenderTimingMode) {
        check(mPlaybackThread == null) { "called after prepare/prepareAsync" }
        require(!(mode == VideoRenderTimingMode.SURFACEVIEW_TIMESTAMP_API21 && Build.VERSION.SDK_INT < 21)) { "this mode needs min API 21" }
        Log.d(TAG, "setVideoRenderTimingMode $mode")
        mVideoRenderTimingMode = mode
    }
    /**
     * Adds a cue point to the media playback timeline. When the cue point is passed, a cue event
     * with this data will be issued to a registered cue listener with
     * [.setOnCueListener]. The cue point can carry arbitrary data as an
     * attachment.
     *
     * @param timeMs the time in milliseconds on the playback timeline at which this cue will be fired
     * @param data   optional data that will be exposed through the cue event
     * @return A cue object that can be used to remove the cue from the playback timeline through
     * [.removeCue]. This is the same cue object that will be provided to the
     * [OnCueListener] so this can be used as a lookup key for associated data.
     */
    /**
     * @see .addCue
     */
    @JvmOverloads
    fun addCue(timeMs: Int, data: Any? = null): Cue {
        val cue = Cue(timeMs, data)
        mCueTimeline.addCue(cue)
        return cue
    }

    /**
     * Removes a cue added by [.addCue] from the playback timeline.
     *
     * @param cue the cue to remove
     * @return true if removal was successful, false if the cue wasn't cued on the timeline,
     * i.e. it has already been removed before
     */
    fun removeCue(cue: Cue?): Boolean {
        return mCueTimeline.removeCue(cue!!)
    }

    private inner class PlaybackThread :
        HandlerThread(
            TAG + "#" + PlaybackThread::class.java.simpleName,
            Process.THREAD_PRIORITY_AUDIO
        ),
        Handler.Callback {
        private var mHandler: Handler? = null
        var isPaused = true
            private set
        private var mReleasing = false
        private var mVideoFrameInfo: FrameInfo? = null
        private val mRenderModeApi21 // Usage of timed outputBufferRelease on API 21+
                : Boolean
        private var mRenderingStarted // Flag to know if decoding the first frame
                : Boolean
        private var mPlaybackSpeed = 0.0
        private var mAVLocked: Boolean
        private var mLastBufferingUpdateTime: Long
        private var mLastCueEventTime: Long
        private val mOnTimelineCueListener: Timeline.OnCueListener

        init {
            // Give this thread a high priority for more precise event timing

            // Init fields
            mRenderModeApi21 = mVideoRenderTimingMode.isRenderModeApi21
            mRenderingStarted = true
            mAVLocked = false
            mLastBufferingUpdateTime = 0
            mLastCueEventTime = 0
            mOnTimelineCueListener =
                object : Timeline.OnCueListener {
                    override fun onCue(cue: Cue?) {
                        mEventHandler.sendMessage(
                            mEventHandler.obtainMessage(
                                MEDIA_CUE,
                                cue
                            )
                        )
                    }
                }
        }
        @Synchronized
        override fun start() {
            super.start()

            // Create the handler that will process the messages on the handler thread
            mHandler = Handler(this.looper, this)
            Log.d(TAG, "PlaybackThread started")
        }

        fun prepare() {
            mHandler!!.sendEmptyMessage(Companion.PLAYBACK_PREPARE)
        }

        fun play() {
            isPaused = false
            mHandler!!.sendEmptyMessage(Companion.PLAYBACK_PLAY)
        }

        fun pause() {
            isPaused = true
            mHandler!!.sendEmptyMessage(Companion.PLAYBACK_PAUSE)
        }

        fun seekTo(usec: Long) {
            // When multiple seek requests come in, e.g. when a user slides the finger on a
            // seek bar in the UI, we don't want to process all of them and can therefore remove
            // all requests from the queue and only keep the most recent one.
            mHandler!!.removeMessages(Companion.PLAYBACK_SEEK) // remove any previous requests
            mHandler!!.obtainMessage(Companion.PLAYBACK_SEEK, usec).sendToTarget()
        }

        fun setSurface(surface: Surface?) {
            mHandler!!.sendMessage(
                mHandler!!.obtainMessage(
                    Companion.DECODER_SET_SURFACE,
                    surface
                )
            )
        }

        fun release(): Boolean {
            if (!isAlive) {
                return false
            }
            isPaused = true // Set this flag so the loop does not schedule next loop iteration
            mReleasing = true

            // Call actual release method
            // Actually it does not matter what we schedule here, we just need to schedule
            // something so {@link #handleMessage} gets called on the handler thread, read the
            // mReleasing flag, and call {@link #releaseInternal}.
            mHandler!!.sendEmptyMessage(Companion.PLAYBACK_RELEASE)
            return true
        }

        override fun handleMessage(msg: Message): Boolean {
            try {
                if (mReleasing) {
                    // When the releasing flag is set, just release without processing any more messages
                    releaseInternal()
                    return true
                }
                return when (msg.what) {
                    Companion.PLAYBACK_PREPARE -> {
                        prepareInternal()
                        true
                    }
                    Companion.PLAYBACK_PLAY -> {
                        playInternal()
                        true
                    }
                    Companion.PLAYBACK_PAUSE -> {
                        pauseInternal()
                        true
                    }
                    Companion.PLAYBACK_PAUSE_AUDIO -> {
                        pauseInternalAudio()
                        true
                    }
                    Companion.PLAYBACK_LOOP -> {
                        loopInternal()
                        true
                    }
                    Companion.PLAYBACK_SEEK -> {
                        seekInternal(msg.obj as Long)
                        true
                    }
                    Companion.PLAYBACK_RELEASE -> {
                        releaseInternal()
                        true
                    }
                    Companion.DECODER_SET_SURFACE -> {
                        setVideoSurface(msg.obj as Surface)
                        true
                    }
                    else -> {
                        Log.d(TAG, "unknown/invalid message")
                        false
                    }
                }
            } catch (e: InterruptedException) {
                Log.d(TAG, "decoder interrupted", e)
                mEventHandler.sendMessage(
                    mEventHandler.obtainMessage(
                        MEDIA_ERROR,
                        MEDIA_ERROR_UNKNOWN, 0
                    )
                )
            } catch (e: IllegalStateException) {
                Log.e(
                    TAG,
                    "decoder error, too many instances?",
                    e
                )
                mEventHandler.sendMessage(
                    mEventHandler.obtainMessage(
                        MEDIA_ERROR,
                        MEDIA_ERROR_UNKNOWN, 0
                    )
                )
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "decoder error, codec can not be created",
                    e
                )
                mEventHandler.sendMessage(
                    mEventHandler.obtainMessage(
                        MEDIA_ERROR,
                        MEDIA_ERROR_UNKNOWN,
                        MEDIA_ERROR_IO
                    )
                )
            }

            // Release after an exception
            releaseInternal()
            return true
        }

        private fun prepareInternal() {
            try {
                this@MediaPlayer.prepareInternal()
                mCurrentState = MediaPlayer.State.PREPARED

                // This event is only triggered after a successful async prepare (not after the sync prepare!)
                mEventHandler.sendEmptyMessage(MEDIA_PREPARED)
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "prepareAsync() failed: cannot decode stream(s)",
                    e
                )
                mEventHandler.sendMessage(
                    mEventHandler.obtainMessage(
                        MEDIA_ERROR,
                        MEDIA_ERROR_UNKNOWN,
                        MEDIA_ERROR_IO
                    )
                )
                releaseInternal()
            } catch (e: IllegalStateException) {
                Log.e(
                    TAG,
                    "prepareAsync() failed: something is in a wrong state",
                    e
                )
                mEventHandler.sendMessage(
                    mEventHandler.obtainMessage(
                        MEDIA_ERROR,
                        MEDIA_ERROR_UNKNOWN, 0
                    )
                )
                releaseInternal()
            } catch (e: IllegalArgumentException) {
                Log.e(
                    TAG,
                    "prepareAsync() failed: surface might be gone",
                    e
                )
                mEventHandler.sendMessage(
                    mEventHandler.obtainMessage(
                        MEDIA_ERROR,
                        MEDIA_ERROR_UNKNOWN, 0
                    )
                )
                releaseInternal()
            }
        }

        @Throws(IOException::class, InterruptedException::class)
        private fun playInternal() {
            if (mDecoders!!.isEOS) {
                mCurrentPosition = 0
                mDecoders!!.seekTo(SeekMode.FAST_TO_PREVIOUS_SYNC, 0)
                mCueTimeline.setPlaybackPosition(0)
            }

            // reset time (otherwise playback tries to "catch up" time after a pause)
            mTimeBase.startAt(mDecoders!!.currentDecodingPTS)
            if (mAudioPlayback != null) {
                mHandler!!.removeMessages(Companion.PLAYBACK_PAUSE_AUDIO)
                mAudioPlayback!!.play()
            }
            mPlaybackSpeed = mTimeBase.speed
            // Sync audio playback speed to playback speed (to account for speed changes during pause)
            if (mAudioPlayback != null) {
                mAudioPlayback!!.setPlaybackSpeed(mPlaybackSpeed.toFloat())
            }
            mHandler!!.removeMessages(Companion.PLAYBACK_LOOP)
            loopInternal()
        }

        private fun pauseInternal(drainAudioPlayback: Boolean = false) {
            // When playback is paused in timed API21 render mode, the remaining cached frames will
            // still be rendered, resulting in a short but noticeable pausing lag. This can be avoided
            // by switching to the old render timing mode.
            mHandler!!.removeMessages(Companion.PLAYBACK_LOOP) // removes remaining loop requests (required when EOS is reached)
            if (mAudioPlayback != null) {
                if (drainAudioPlayback) {
                    // Defer pausing the audio playback for the length of the playback buffer, to
                    // make sure that all audio samples have been played out.
                    mHandler!!.sendEmptyMessageDelayed(
                        Companion.PLAYBACK_PAUSE_AUDIO,
                        (mAudioPlayback!!.queueBufferTimeUs + mAudioPlayback!!.playbackBufferTimeUs) / 1000 + 1
                    )
                } else {
                    mAudioPlayback!!.pause(false)
                }
            }
        }

        private fun pauseInternalAudio() {
            if (mAudioPlayback != null) {
                mAudioPlayback!!.pause()
            }
        }

        @Throws(IOException::class, InterruptedException::class)
        private fun loopInternal() {
            // If this is an online stream, notify the client of the buffer fill level.
            val cachedDuration = mDecoders!!.cachedDuration
            if (cachedDuration != -1L) {
                // The cached duration from the MediaExtractor returns the cached time from
                // the current position onwards, but the Android MediaPlayer returns the
                // total time consisting of the current playback point and the length of
                // the prefetched data.
                // This comes before the buffering pause to update the clients buffering info
                // also during a buffering playback pause.
                // Use the PTS of the decoder input for a more precise buffer calculation (the
                // cached duration is the duration from the read position in the media extractor,
                // which is about the same position as the position from where the last sample
                // has been read).
                var currentPosition = mDecoders!!.inputSamplePTS
                if (currentPosition == MediaCodecDecoder.PTS_UNKNOWN) {
                    // If this input PTS is not available, e.g. directly after a seek, fall
                    // back to the current playback position.
                    currentPosition = mCurrentPosition
                }
                updateBufferPercentage((100.0 / (duration * 1000) * (currentPosition + cachedDuration)).toInt())
            }

            // If we are in buffering mode, check if the buffer has been filled until the low water
            // mark or the end of the stream has been reached, and pause playback if it isn't filled
            // high enough yet.
            if (mBuffering && cachedDuration > -1 && cachedDuration < BUFFER_LOW_WATER_MARK_US && !mDecoders!!.hasCacheReachedEndOfStream()) {
                //Log.d(TAG, "buffering... " + mDecoders.getCachedDuration() + " / " + BUFFER_LOW_WATER_MARK_US);
                // To pause playback for buffering, we simply skip this loop and call it again later
                mHandler!!.sendEmptyMessageDelayed(Companion.PLAYBACK_LOOP, 100)
                return
            }
            if (mDecoders!!.videoDecoder != null && mVideoFrameInfo == null) {
                // This method needs a video frame to operate on. If there is no frame, we need
                // to decode one first.
                mVideoFrameInfo = mDecoders!!.decodeFrame(false)
                if (mVideoFrameInfo == null && !mDecoders!!.isEOS) {
                    // If the decoder didn't return a frame, we need to give it some processing time
                    // and come back later...
                    mHandler!!.sendEmptyMessageDelayed(Companion.PLAYBACK_LOOP, 10)
                    return
                }
            }
            val startTime = SystemClock.elapsedRealtime()

            // When we are in buffering mode, and a frame has been decoded, the buffer is
            // obviously refilled so we can send the buffering end message and exit buffering mode.
            if (mBuffering) {
                mBuffering = false
                mEventHandler.sendMessage(
                    mEventHandler.obtainMessage(
                        MEDIA_INFO,
                        MEDIA_INFO_BUFFERING_END, 0
                    )
                )

                // Reset timebase so player does not try to catch up time lost while caching
                mTimeBase.startAt(mDecoders!!.currentDecodingPTS)
            }

            // When the waiting time to the next frame is too long, we defer rendering through
            // the handler here instead of relying on releaseOutputBuffer(buffer, renderTimestampNs),
            // which does not work well with long waiting times and many frames in the queue.
            // On API < 21 the frame rendering is timed with a sleep() and this is not really necessary,
            // but still shifts some waiting time from the sleep() to here.
            if (mVideoFrameInfo != null && mTimeBase.getOffsetFrom(mVideoFrameInfo!!.presentationTimeUs) > 60000) {
                mHandler!!.sendEmptyMessageDelayed(Companion.PLAYBACK_LOOP, 50)
                return
            }

            // Update the current position of the player
            mCurrentPosition = mDecoders!!.currentDecodingPTS

            // fire cue events
            // Rate limited to 10 Hz (every 100ms)
            if (mCueTimeline.count() > 0 && startTime - mLastCueEventTime > 100) {
                mLastCueEventTime = startTime
                mCueTimeline.movePlaybackPosition(
                    (mCurrentPosition / 1000).toInt(),
                    mOnTimelineCueListener
                )
            }
            if (mDecoders!!.videoDecoder != null && mVideoFrameInfo != null) {
                renderVideoFrame(mVideoFrameInfo!!)
                mVideoFrameInfo = null

                // When the first frame is rendered, video rendering has started and the event triggered
                if (mRenderingStarted) {
                    mRenderingStarted = false
                    mEventHandler.sendMessage(
                        mEventHandler.obtainMessage(
                            MEDIA_INFO,
                            MEDIA_INFO_VIDEO_RENDERING_START, 0
                        )
                    )
                }
            }
            if (mAudioPlayback != null) {
                // Sync audio playback speed to playback speed (to account for speed changes during playback)
                // Change the speed on the audio playback object only if it has really changed, to avoid runtime overhead
                if (mPlaybackSpeed != mTimeBase.speed) {
                    mPlaybackSpeed = mTimeBase.speed
                    mAudioPlayback!!.setPlaybackSpeed(mPlaybackSpeed.toFloat())
                }

                // Sync timebase to audio timebase when there is audio data available
                val currentAudioPTS = mAudioPlayback!!.currentPresentationTimeUs
                if (currentAudioPTS > AudioPlayback.PTS_NOT_SET) {
                    mTimeBase.startAt(currentAudioPTS)
                }
            }

            // Handle EOS
            if (mDecoders!!.isEOS) {
                mEventHandler.sendEmptyMessage(MEDIA_PLAYBACK_COMPLETE)

                // If looping is on, seek back to the start...
                if (isLooping) {
                    if (mAudioPlayback != null) {
                        // Flush audio buffer to reset audio PTS
                        mAudioPlayback!!.flush()
                    }
                    mDecoders!!.seekTo(SeekMode.FAST_TO_PREVIOUS_SYNC, 0)
                    mCueTimeline.setPlaybackPosition(0)
                    mDecoders!!.renderFrames()
                } else {
                    isPaused = true
                    pauseInternal(true) // pause but play remaining buffered audio
                }
            } else {
                // Get next frame
                mVideoFrameInfo = mDecoders!!.decodeFrame(false)
            }
            if (!isPaused) {
                // Static delay time until the next call of the playback loop
                var delay: Long = 10
                // Scale delay by playback speed to avoid limiting framerate
                delay = (delay / mTimeBase.speed).toLong()
                // Calculate the duration taken for the current call
                val duration = SystemClock.elapsedRealtime() - startTime
                // Adjust the delay by the time taken
                delay = delay - duration
                if (delay > 0) {
                    // Sleep for some time and then continue processing the loop
                    // This replaces the very unreliable and jittery Thread.sleep in the old decoder thread
                    mHandler!!.sendEmptyMessageDelayed(Companion.PLAYBACK_LOOP, delay)
                } else {
                    // The current call took too much time; there is no time left for delaying, call instantly
                    mHandler!!.sendEmptyMessage(Companion.PLAYBACK_LOOP)
                }
            }
        }

        @Throws(IOException::class, InterruptedException::class)
        private fun seekInternal(usec: Long) {
            if (mVideoFrameInfo != null) {
                // A decoded video frame is waiting to be rendered, dismiss it
                mDecoders!!.videoDecoder!!.dismissFrame(mVideoFrameInfo!!)
                mVideoFrameInfo = null
            }

            // Clear the audio cache
            if (mAudioPlayback != null) mAudioPlayback!!.pause(true)

            // Seek to the target time
            mDecoders!!.seekTo(seekMode, usec)

            // Reset time to keep frame rate constant
            // (otherwise it's too fast on back seeks and waits for the PTS time on fw seeks)
            mTimeBase.startAt(mDecoders!!.currentDecodingPTS)

            // Check if another seek has been issued in the meantime
            val newSeekWaiting =
                mHandler!!.hasMessages(Companion.PLAYBACK_SEEK)

            // Render seek target frame (if no new seek is waiting to be processed)
            if (newSeekWaiting) {
                mDecoders!!.dismissFrames()
            } else {
                mDecoders!!.renderFrames()
            }

            // When there are no more seek requests in the queue, notify of finished seek operation
            if (!newSeekWaiting) {
                // Set the final seek position as the current position
                // (the final seek position may be off the initial target seek position)
                mCurrentPosition = mDecoders!!.currentDecodingPTS
                mSeeking = false
                mAVLocked = false
                mEventHandler.sendEmptyMessage(MEDIA_SEEK_COMPLETE)
                if (!isPaused) {
                    playInternal()
                }
                mCueTimeline.setPlaybackPosition((mCurrentPosition / 1000).toInt())
            }
        }

        private fun releaseInternal() {
            // post interrupt to avoid all further execution of messages/events in the queue
            interrupt()

            // quit message processing and exit thread
            quit()
            if (mDecoders != null) {
                if (mVideoFrameInfo != null) {
                    mDecoders!!.videoDecoder!!.releaseFrame(mVideoFrameInfo!!)
                    mVideoFrameInfo = null
                }
            }
            if (mDecoders != null) {
                mDecoders!!.release()
            }
            if (mAudioPlayback != null) mAudioPlayback!!.stopAndRelease()
            releaseMediaExtractors()
            Log.d(TAG, "PlaybackThread destroyed")

            // Notify #release() that it can now continue because #releaseInternal is finished
            if (mReleaseSyncLock != null) {
                synchronized(mReleaseSyncLock!!) {
                    mReleaseSyncLock!!.notify()
                    mReleaseSyncLock = null
                }
            }
        }

        @Throws(InterruptedException::class)
        private fun renderVideoFrame(videoFrameInfo: FrameInfo) {
            if (videoFrameInfo.endOfStream) {
                // The EOS frame does not contain a video frame, so we dismiss it
                mDecoders!!.videoDecoder!!.dismissFrame(videoFrameInfo)
                return
            }

            // Calculate waiting time until the next frame's PTS
            // The waiting time might be much higher that a frame's duration because timed API21
            // rendering caches multiple released output frames before actually rendering them.
            val waitingTime =
                mTimeBase.getOffsetFrom(videoFrameInfo.presentationTimeUs)
            //            Log.d(TAG, "VPTS " + mCurrentPosition
//                    + " APTS " + mAudioPlayback.getCurrentPresentationTimeUs()
//                    + " waitingTime " + waitingTime);
            if (waitingTime < -1000) {
                // we need to catch up time by skipping rendering of this frame
                // this doesn't gain enough time if playback speed is too high and decoder at full load
                // TODO improve fast forward mode
                Log.d(TAG, "LAGGING $waitingTime")
                mEventHandler.sendMessage(
                    mEventHandler.obtainMessage(
                        MEDIA_INFO,
                        MEDIA_INFO_VIDEO_TRACK_LAGGING, 0
                    )
                )
            }

            // Defer the video size changed message until the first frame of the new size is being rendered
            if (videoFrameInfo.representationChanged) {
                mEventHandler.sendMessage(
                    mEventHandler.obtainMessage(
                        MEDIA_SET_VIDEO_SIZE,
                        mDecoders!!.videoDecoder!!.videoWidth,
                        mDecoders!!.videoDecoder!!.videoHeight
                    )
                )
            }

            // Slow down playback, if necessary, to keep frame rate
            if (!mRenderModeApi21 && waitingTime > 5000) {
                // Sleep until it's time to render the next frame
                // This is not v-synced to the display. Not required any more on API 21+.
                Thread.sleep(waitingTime / 1000)
            }
            // Release the current frame and render it to the surface
            mDecoders!!.videoDecoder!!.renderFrame(videoFrameInfo, waitingTime)
        }

        @Throws(IOException::class)
        private fun setVideoSurface(surface: Surface) {
            if (mDecoders != null && mDecoders!!.videoDecoder != null) {
                if (mVideoFrameInfo != null) {
                    // Dismiss queued video frame
                    // After updating the surface, which re-initializes the codec,
                    // the frame buffer will not be valid any more and trying to decode
                    // it would result in an error; so we throw it away.
                    mDecoders!!.videoDecoder!!.dismissFrame(mVideoFrameInfo!!)
                    mVideoFrameInfo = null
                }
                mDecoders!!.videoDecoder!!.updateSurface(surface)
            }
        }

        private fun updateBufferPercentage(percent: Int) {
            val currentTime = SystemClock.elapsedRealtime()

            // Throttle the MEDIA_BUFFERING_UPDATE frequency to at most once per second
            // and only issue updates when the percentage actually changes.
            if (currentTime - mLastBufferingUpdateTime > 1000 && percent != bufferPercentage) {
                mLastBufferingUpdateTime = currentTime
                // Clamp the reported percent to 100% to avoid percentages above 100, which can
                // happen due to the not exactly precise buffer level calculation
                mEventHandler.sendMessage(
                    mEventHandler.obtainMessage(
                        MEDIA_BUFFERING_UPDATE,
                        Math.min(percent, 100), 0
                    )
                )
            }

            // Update the buffer percentage at every call so a user of the library can decide
            // to update the buffer fill state more often than the buffering update handler is
            // called by calling getBufferPercentage at his desired frequency.
            bufferPercentage = percent
        }


    }

    /**
     * Interface definition for a callback to be invoked when the media
     * source is ready for playback.
     */
    interface OnPreparedListener {
        /**
         * Called when the media file is ready for playback.
         * @param mp the MediaPlayer that is ready for playback
         */
        fun onPrepared(mp: MediaPlayer?)
    }

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    fun setOnPreparedListener(listener: OnPreparedListener?) {
        mOnPreparedListener = listener
    }

    /**
     * Interface definition for a callback to be invoked when playback of
     * a media source has completed.
     */
    interface OnCompletionListener {
        /**
         * Called when the end of a media source is reached during playback.
         * @param mp the MediaPlayer that reached the end of the file
         */
        fun onCompletion(mp: MediaPlayer?)
    }

    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during playback.
     *
     * @param listener the callback that will be run
     */
    fun setOnCompletionListener(listener: OnCompletionListener?) {
        mOnCompletionListener = listener
    }

    /**
     * Interface definition of a callback to be invoked when a seek
     * is issued.
     */
    interface OnSeekListener {
        /**
         * Called to indicate that a seek operation has been started.
         * @param mp the mediaPlayer that the seek was called on
         */
        fun onSeek(mp: MediaPlayer?)
    }

    /**
     * Register a calback to be invoked when a seek operation has been started.
     * @param listener the callback that will be run
     */
    fun setOnSeekListener(listener: OnSeekListener?) {
        mOnSeekListener = listener
    }

    /**
     * Interface definition of a callback to be invoked indicating
     * the completion of a seek operation.
     */
    interface OnSeekCompleteListener {
        /**
         * Called to indicate the completion of a seek operation.
         * @param mp the MediaPlayer that issued the seek operation
         */
        fun onSeekComplete(mp: MediaPlayer?)
    }

    /**
     * Register a callback to be invoked when a seek operation has been
     * completed.
     *
     * @param listener the callback that will be run
     */
    fun setOnSeekCompleteListener(listener: OnSeekCompleteListener?) {
        mOnSeekCompleteListener = listener
    }

    /**
     * Interface definition of a callback to be invoked when the
     * video size is first known or updated
     */
    interface OnVideoSizeChangedListener {
        /**
         * Called to indicate the video size
         *
         * The video size (width and height) could be 0 if there was no video,
         * no display surface was set, or the value was not determined yet.
         *
         * @param mp        the MediaPlayer associated with this callback
         * @param width     the width of the video
         * @param height    the height of the video
         */
        fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int)
    }

    /**
     * Register a callback to be invoked when the video size is
     * known or updated.
     *
     * @param listener the callback that will be run
     */
    fun setOnVideoSizeChangedListener(listener: OnVideoSizeChangedListener?) {
        mOnVideoSizeChangedListener = listener
    }

    /**
     * Interface definition of a callback to be invoked indicating buffering
     * status of a media resource being streamed over the network.
     */
    interface OnBufferingUpdateListener {
        /**
         * Called to update status in buffering a media stream received through
         * progressive HTTP download. The received buffering percentage
         * indicates how much of the content has been buffered or played.
         * For example a buffering update of 80 percent when half the content
         * has already been played indicates that the next 30 percent of the
         * content to play has been buffered.
         *
         * @param mp      the MediaPlayer the update pertains to
         * @param percent the percentage (0-100) of the content
         * that has been buffered or played thus far
         */
        fun onBufferingUpdate(mp: MediaPlayer?, percent: Int)
    }

    /**
     * Register a callback to be invoked when the status of a network
     * stream's buffer has changed.
     *
     * @param listener the callback that will be run.
     */
    fun setOnBufferingUpdateListener(listener: OnBufferingUpdateListener?) {
        mOnBufferingUpdateListener = listener
    }

    /**
     * Interface definition of a callback to be invoked when there
     * has been an error during an asynchronous operation (other errors
     * will throw exceptions at method call time).
     */
    interface OnErrorListener {
        /**
         * Called to indicate an error.
         *
         * @param mp      the MediaPlayer the error pertains to
         * @param what    the type of error that has occurred:
         *
         *  * [.MEDIA_ERROR_UNKNOWN]
         *  * [.MEDIA_ERROR_SERVER_DIED]
         *
         * @param extra an extra code, specific to the error. Typically
         * implementation dependent.
         *
         *  * [.MEDIA_ERROR_IO]
         *  * [.MEDIA_ERROR_MALFORMED]
         *  * [.MEDIA_ERROR_UNSUPPORTED]
         *  * [.MEDIA_ERROR_TIMED_OUT]
         *
         * @return True if the method handled the error, false if it didn't.
         * Returning false, or not having an OnErrorListener at all, will
         * cause the OnCompletionListener to be called.
         */
        fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean
    }

    /**
     * Register a callback to be invoked when an error has happened
     * during an asynchronous operation.
     *
     * @param listener the callback that will be run
     */
    fun setOnErrorListener(listener: OnErrorListener?) {
        mOnErrorListener = listener
    }

    /**
     * Interface definition of a callback to be invoked to communicate some
     * info and/or warning about the media or its playback.
     */
    interface OnInfoListener {
        /**
         * Called to indicate an info or a warning.
         *
         * @param mp      the MediaPlayer the info pertains to.
         * @param what    the type of info or warning.
         *
         *  * [.MEDIA_INFO_VIDEO_TRACK_LAGGING]
         *  * [.MEDIA_INFO_VIDEO_RENDERING_START]
         *  * [.MEDIA_INFO_BUFFERING_START]
         *  * [.MEDIA_INFO_BUFFERING_END]
         *
         * @param extra an extra code, specific to the info. Typically
         * implementation dependent.
         * @return True if the method handled the info, false if it didn't.
         * Returning false, or not having an OnErrorListener at all, will
         * cause the info to be discarded.
         */
        fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean
    }

    /**
     * Register a callback to be invoked when an info/warning is available.
     * @param listener the callback that will be run
     */
    fun setOnInfoListener(listener: OnInfoListener?) {
        mOnInfoListener = listener
    }

    /**
     * Interface definition of an event callback to be invoked when playback passes a cue point.
     */
    interface OnCueListener {
        /**
         * Called to indicate that a cue point has been reached/passed during playback.
         * @param mp The MediaPlayer that this event originates from
         * @param cue the definition of the cue point
         */
        fun onCue(mp: MediaPlayer?, cue: Cue?)
    }

    /**
     * Register a callback to be invoked when a cue point cued with [.addCue] is
     * passed during playback.
     * @param listener the callback that will be called
     */
    fun setOnCueListener(listener: OnCueListener?) {
        mOnCueListener = listener
    }

    private inner class EventHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MEDIA_PREPARED -> {
                    Log.d(TAG, "onPrepared")
                    if (mOnPreparedListener != null) {
                        mOnPreparedListener!!.onPrepared(this@MediaPlayer)
                    }
                    return
                }
                MEDIA_SEEK_COMPLETE -> {
                    Log.d(TAG, "onSeekComplete")
                    if (mOnSeekCompleteListener != null) {
                        mOnSeekCompleteListener!!.onSeekComplete(this@MediaPlayer)
                    }
                    return
                }
                MEDIA_PLAYBACK_COMPLETE -> {
                    Log.d(TAG, "onPlaybackComplete")
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener!!.onCompletion(this@MediaPlayer)
                    }
                    stayAwake(false)
                    return
                }
                MEDIA_SET_VIDEO_SIZE -> {
                    Log.d(TAG, "onVideoSizeChanged")
                    if (mOnVideoSizeChangedListener != null) {
                        mOnVideoSizeChangedListener!!.onVideoSizeChanged(
                            this@MediaPlayer,
                            msg.arg1,
                            msg.arg2
                        )
                    }
                    return
                }
                MEDIA_ERROR -> {
                    Log.e(
                        TAG,
                        "Error (" + msg.arg1.toString() + "," + msg.arg2.toString() + ")"
                    )
                    var error_was_handled = false
                    if (mOnErrorListener != null) {
                        error_was_handled =
                            mOnErrorListener!!.onError(this@MediaPlayer, msg.arg1, msg.arg2)
                    }
                    if (mOnCompletionListener != null && !error_was_handled) {
                        mOnCompletionListener!!.onCompletion(this@MediaPlayer)
                    }
                    stayAwake(false)
                    return
                }
                MEDIA_INFO -> {
                    Log.d(TAG, "onInfo")
                    if (mOnInfoListener != null) {
                        mOnInfoListener!!.onInfo(this@MediaPlayer, msg.arg1, msg.arg2)
                    }
                    return
                }
                MEDIA_BUFFERING_UPDATE -> {
                    //Log.d(TAG, "onBufferingUpdate");
                    if (mOnBufferingUpdateListener != null) mOnBufferingUpdateListener!!.onBufferingUpdate(
                        this@MediaPlayer,
                        msg.arg1
                    )
                    return
                }
                MEDIA_CUE -> {
                    if (mOnCueListener != null) mOnCueListener!!.onCue(
                        this@MediaPlayer,
                        msg.obj as Cue
                    )
                    return
                }
                else -> {
                }
            }
        }
    }

    companion object {
        public val PLAYBACK_PREPARE = 1
        public val PLAYBACK_PLAY = 2
        public val PLAYBACK_PAUSE = 3
        public val PLAYBACK_LOOP = 4
        public val PLAYBACK_SEEK = 5
        public val PLAYBACK_RELEASE = 6
        public val PLAYBACK_PAUSE_AUDIO = 7
        public val DECODER_SET_SURFACE = 100

        private val TAG = MediaPlayer::class.java.simpleName
        private const val BUFFER_LOW_WATER_MARK_US: Long =
            2000000 // 2 seconds; NOTE: make sure this is below DashMediaExtractor's mMinBufferTimeUs

        /**
         * Pass as track index to tell the player that no track should be selected.
         */
        const val TRACK_INDEX_NONE = -1

        /**
         * Pass as track index to tell the player to automatically select the first fitting track.
         */
        const val TRACK_INDEX_AUTO = -2

        /** Unspecified media player error.
         * @see MediaPlayer.OnErrorListener
         */
        const val MEDIA_ERROR_UNKNOWN = 1

        /** Media server died. In this case, the application must release the
         * MediaPlayer object and instantiate a new one.
         * @see MediaPlayer.OnErrorListener
         */
        const val MEDIA_ERROR_SERVER_DIED = 100

        /** The video is streamed and its container is not valid for progressive
         * playback i.e the video's index (e.g moov atom) is not at the start of the
         * file.
         * @see MediaPlayer.OnErrorListener
         */
        const val MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200

        /** File or network related operation errors.  */
        const val MEDIA_ERROR_IO = -1004

        /** Bitstream is not conforming to the related coding standard or file spec.  */
        const val MEDIA_ERROR_MALFORMED = -1007

        /** Bitstream is conforming to the related coding standard or file spec, but
         * the media framework does not support the feature.  */
        const val MEDIA_ERROR_UNSUPPORTED = -1010

        /** Some operation takes too long to complete, usually more than 3-5 seconds.  */
        const val MEDIA_ERROR_TIMED_OUT = -110

        /** The player just pushed the very first video frame for rendering.
         * @see MediaPlayer.OnInfoListener
         */
        const val MEDIA_INFO_VIDEO_RENDERING_START = 3

        /** The video is too complex for the decoder: it can't decode frames fast
         * enough. Possibly only the audio plays fine at this stage.
         * @see MediaPlayer.OnInfoListener
         */
        const val MEDIA_INFO_VIDEO_TRACK_LAGGING = 700

        /** MediaPlayer is temporarily pausing playback internally in order to
         * buffer more data.
         * @see MediaPlayer.OnInfoListener
         */
        const val MEDIA_INFO_BUFFERING_START = 701

        /** MediaPlayer is resuming playback after filling buffers.
         * @see MediaPlayer.OnInfoListener
         */
        const val MEDIA_INFO_BUFFERING_END = 702
        private const val MEDIA_PREPARED = 1
        private const val MEDIA_PLAYBACK_COMPLETE = 2
        private const val MEDIA_BUFFERING_UPDATE = 3
        private const val MEDIA_SEEK_COMPLETE = 4
        private const val MEDIA_SET_VIDEO_SIZE = 5
        private const val MEDIA_ERROR = 100
        private const val MEDIA_INFO = 200
        private const val MEDIA_CUE = 1000
    }

    init {
        mEventHandler = EventHandler()
        mTimeBase = TimeBase()
        mVideoRenderTimingMode = VideoRenderTimingMode.AUTO
        mCurrentState = State.IDLE
        mAudioSessionId = 0 // AudioSystem.AUDIO_SESSION_ALLOCATE;
        audioStreamType = AudioManager.STREAM_MUSIC
        mCueTimeline = Timeline()
    }
}