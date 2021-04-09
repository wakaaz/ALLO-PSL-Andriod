package com.net.pslapllication.util.customvideoview

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View
import android.widget.MediaController;
import android.widget.Toast;

import java.io.IOException;
import java.util.Map;


class VideoView : SurfaceView, SurfaceHolder.Callback, MediaController.MediaPlayerControl {
    private var mCurrentState = STATE_IDLE
    private var mTargetState = STATE_IDLE
    private var mSource: MediaSource? = null
    private var mVideoTrackIndex = 0
    private var mAudioTrackIndex = 0

    // TODO do not return the real media player
    // Handling width it could result in invalid states, better return a "censored" wrapper interface
    var mediaPlayer: MediaPlayer? = null
        private set
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mSeekWhenPrepared = 0
    private var mPlaybackSpeedWhenPrepared = 0f
    private var mOnPreparedListener: MediaPlayer.OnPreparedListener? =
        null
    private var mOnSeekListener: MediaPlayer.OnSeekListener? = null
    private var mOnSeekCompleteListener: MediaPlayer.OnSeekCompleteListener? =
        null
    private var mOnCompletionListener: MediaPlayer.OnCompletionListener? =
        null
    private var mOnErrorListener: MediaPlayer.OnErrorListener? =
        null
    private var mOnInfoListener: MediaPlayer.OnInfoListener? =
        null
    private var mOnBufferingUpdateListener: MediaPlayer.OnBufferingUpdateListener? =
        null

    constructor(context: Context?) : super(context) {
        initVideoView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        initVideoView()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        initVideoView()
    }

    private fun initVideoView() {
        holder.addCallback(this)
    }

    /**
     * Sets a media source and track indices. See [MediaPlayer.setDataSource]
     * for a detailed explanation of the parameters.
     *
     * @param source the media source
     * @param videoTrackIndex a video track index or one of the MediaPlayer#TRACK_INDEX_* constants
     * @param audioTrackIndex an video audio index or one of the MediaPlayer#TRACK_INDEX_* constants
     */
    fun setVideoSource(
        source: MediaSource?,
        videoTrackIndex: Int,
        audioTrackIndex: Int
    ) {
        mCurrentState = STATE_IDLE
        mTargetState = STATE_IDLE
        mSource = source
        mVideoTrackIndex = videoTrackIndex
        mAudioTrackIndex = audioTrackIndex
        mSeekWhenPrepared = 0
        mPlaybackSpeedWhenPrepared = 1f
        openVideo()
        requestLayout()
        invalidate()
    }

    /**
     * Sets a media source.
     * @param source the media source
     */
    fun setVideoSource(source: MediaSource?) {
        setVideoSource(
            source,
            MediaPlayer.TRACK_INDEX_AUTO,
            MediaPlayer.TRACK_INDEX_AUTO
        )
    }

    /**
     * @see android.widget.VideoView.setVideoPath
     * @param path
     */
    @Deprecated("only for compatibility with Android API")
    fun setVideoPath(path: String?) {
        setVideoSource(UriSource(context, Uri.parse(path)))
    }

    /**
     * @see android.widget.VideoView.setVideoURI
     * @param uri
     */
    @Deprecated("only for compatibility with Android API")
    fun setVideoURI(uri: Uri?) {
        setVideoSource(UriSource(context, uri!!))
    }

    /**
     * @see android.widget.VideoView.setVideoURI
     * @param uri
     * @param headers
     */
    @Deprecated("only for compatibility with Android API")
    fun setVideoURI(
        uri: Uri?,
        headers: kotlin.collections.Map<String, String>?
    ) {
        setVideoSource(UriSource(context!!, uri!!, headers))
    }

    private fun openVideo() {
        if (mSource == null || mSurfaceHolder == null) {
            // not ready for playback yet, will be called again later
            return
        }
        release()
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDisplay(mSurfaceHolder)
        mediaPlayer!!.setScreenOnWhilePlaying(true)
        mediaPlayer!!.setOnPreparedListener(mPreparedListener)
        mediaPlayer!!.setOnSeekListener(mSeekListener)
        mediaPlayer!!.setOnSeekCompleteListener(mSeekCompleteListener)
        mediaPlayer!!.setOnCompletionListener(mCompletionListener)
        mediaPlayer!!.setOnVideoSizeChangedListener(mSizeChangedListener)
        mediaPlayer!!.setOnErrorListener(mErrorListener)
        mediaPlayer!!.setOnInfoListener(mInfoListener)
        mediaPlayer!!.setOnBufferingUpdateListener(mBufferingUpdateListener)

        // Create a handler for the error message in case an exceptions happens in the following thread
        val exceptionHandler =
            Handler(object : Handler.Callback {
                override fun handleMessage(msg: Message): Boolean {
                    mCurrentState = STATE_ERROR
                    mTargetState = STATE_ERROR
                    mErrorListener.onError(
                        mediaPlayer,
                        MediaPlayer.MEDIA_ERROR_UNKNOWN,
                        0
                    )
                    return true
                }
            })

        // Copy the player reference for use in the thread which allows us to later compare
        // references and detect player changes (release, reload).
        val currentPlayer = mediaPlayer!!

        // Set the data source asynchronously as this might take a while, e.g. if data has to be
        // requested from the network/internet.
        // IMPORTANT:
        // We use a Thread instead of an AsyncTask for performance reasons, because threads started
        // in an AsyncTask perform much worse, no matter the priority the Thread gets (unless the
        // AsyncTask's priority is elevated before creating the Thread).
        // See comment in MediaPlayer#prepareAsync for detailed explanation.
        Thread(object : Runnable {
            override fun run() {
                try {
                    mCurrentState = STATE_PREPARING
                    currentPlayer.setDataSource(mSource!!, mVideoTrackIndex, mAudioTrackIndex)
                    if (mediaPlayer != currentPlayer) {
                        // Player has been released or recreated while the data source was set,
                        // so we can ditch the instance and do not need to prepare it.
                        // This is a special case that can only happen because we execute
                        // setDataSource asynchronously, so the player could be released or a new
                        // source loaded and thus a new player created in the meantime.
                        // In Android's VideoView, setDataSource is executed synchronously and
                        // this case therefore cannot happen there.
                        // Just to be sure, we release the player again from within here, e.g. for
                        // the case that extractors have been created after the outer release call.
                        currentPlayer.release()
                        return
                    }

                    // Async prepare spawns another thread inside this thread which really isn't
                    // necessary; we call this method anyway because of the events it triggers
                    // when it fails, and to stay in sync which the Android VideoView that does
                    // the same.
                    currentPlayer.prepareAsync()
                    Log.d(TAG, "video opened")
                } catch (e: IOException) {
                    Log.e(TAG, "video open failed", e)

                    // Send message to the handler that an error occurred
                    // (we don't need a message id as the handler only handles this single message)
                    // We only do that for the current player, not for previous instances
                    if (mediaPlayer == currentPlayer) {
                        exceptionHandler.sendEmptyMessage(0)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "something went wrong", e)
                }
            }
        }).start()
    }

    /**
     * Resizes the video view according to the video size to keep aspect ratio.
     * Code copied from [android.widget.VideoView.onMeasure].
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
        //        + MeasureSpec.toString(heightMeasureSpec) + ")");
        var width = View.getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = View.getDefaultSize(mVideoHeight, heightMeasureSpec)
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * mVideoHeight / mVideoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * mVideoWidth / mVideoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth
                height = mVideoHeight
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * mVideoWidth / mVideoHeight
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * mVideoHeight / mVideoWidth
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height)
    }

    private fun release() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        mCurrentState = STATE_IDLE
        mTargetState = STATE_IDLE
    }

    fun setOnPreparedListener(l: MediaPlayer.OnPreparedListener?) {
        mOnPreparedListener = l
    }

    fun setOnSeekListener(l: MediaPlayer.OnSeekListener?) {
        mOnSeekListener = l
    }

    fun setOnSeekCompleteListener(l: MediaPlayer.OnSeekCompleteListener?) {
        mOnSeekCompleteListener = l
    }

    fun setOnCompletionListener(l: MediaPlayer.OnCompletionListener?) {
        mOnCompletionListener = l
    }

    fun setOnBufferingUpdateListener(l: MediaPlayer.OnBufferingUpdateListener?) {
        mOnBufferingUpdateListener = l
    }

    fun setOnErrorListener(l: MediaPlayer.OnErrorListener?) {
        mOnErrorListener = l
    }

    fun setOnInfoListener(l: MediaPlayer.OnInfoListener?) {
        mOnInfoListener = l
    }

    override fun start() {
        if (isInPlaybackState) {
            mediaPlayer!!.start()
        } else {
            mTargetState = STATE_PLAYING
        }
    }

    override fun pause() {
        if (isInPlaybackState) {
            mediaPlayer!!.pause()
        }
        mTargetState = STATE_PAUSED
    }

    fun stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mCurrentState = STATE_IDLE
            mTargetState = STATE_IDLE
        }
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
        get() = if (isInPlaybackState) {
            mediaPlayer!!.playbackSpeed
        } else {
            mPlaybackSpeedWhenPrepared
        }
        set(speed) {
            require(speed >= 0) { "speed cannot be negative" }
            if (isInPlaybackState) {
                mediaPlayer!!.playbackSpeed = speed
            }
            mPlaybackSpeedWhenPrepared = speed
        }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mSurfaceHolder = holder
        openVideo()
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
        // nothing yet
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mSurfaceHolder = null
        release()
    }

    override fun getDuration(): Int {
        return if (mediaPlayer != null) mediaPlayer!!.duration else 0
    }

    override fun getCurrentPosition(): Int {
        return if (isInPlaybackState) {
            mediaPlayer!!.currentPosition
        } else 0
    }

    override fun seekTo(msec: Int) {
        mSeekWhenPrepared = if (isInPlaybackState) {
            mediaPlayer!!.seekTo(msec)
            0
        } else {
            msec
        }
    }

    var seekMode: MediaPlayer.SeekMode?
        get() = mediaPlayer!!.seekMode
        set(seekMode) {
            mediaPlayer!!.seekMode = seekMode!!
        }

    private val isInPlaybackState: Boolean
        private get() = mediaPlayer != null && mCurrentState >= STATE_PREPARED

    override fun isPlaying(): Boolean {
        return mediaPlayer != null && mediaPlayer!!.isPlaying
    }

    override fun getBufferPercentage(): Int {
        return if (mediaPlayer != null) mediaPlayer!!.bufferPercentage else 0
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getAudioSessionId(): Int {
        return if (mediaPlayer != null) mediaPlayer!!.audioSessionId else 0
    }

    private val mPreparedListener: MediaPlayer.OnPreparedListener =
        object : MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer?) {
                mCurrentState = STATE_PREPARED
                playbackSpeed = mPlaybackSpeedWhenPrepared
                if (mOnPreparedListener != null) {
                    mOnPreparedListener!!.onPrepared(mp)
                }
                val seekToPosition =
                    mSeekWhenPrepared // mSeekWhenPrepared may be changed after seekTo() call
                if (seekToPosition != 0) {
                    seekTo(seekToPosition)
                }
                if (mTargetState == STATE_PLAYING) {
                    start()
                }
            }
        }
    private val mSizeChangedListener: MediaPlayer.OnVideoSizeChangedListener =
        object :
            MediaPlayer.OnVideoSizeChangedListener {
            override fun onVideoSizeChanged(
                mp: MediaPlayer?,
                width: Int,
                height: Int
            ) {
                mVideoWidth = width
                mVideoHeight = height
                requestLayout()
            }
        }
    private val mSeekListener: MediaPlayer.OnSeekListener = object : MediaPlayer.OnSeekListener {
        override fun onSeek(mp: MediaPlayer?) {
            if (mOnSeekListener != null) {
                mOnSeekListener!!.onSeek(mp)
            }
        }
    }
    private val mSeekCompleteListener: MediaPlayer.OnSeekCompleteListener =
        object : MediaPlayer.OnSeekCompleteListener {
            override fun onSeekComplete(mp: MediaPlayer?) {
                if (mOnSeekCompleteListener != null) {
                    mOnSeekCompleteListener!!.onSeekComplete(mp)
                }
            }
        }
    private val mCompletionListener: MediaPlayer.OnCompletionListener =
        object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                mCurrentState = STATE_PLAYBACK_COMPLETED
                mTargetState = STATE_PLAYBACK_COMPLETED
                if (mOnCompletionListener != null) {
                    mOnCompletionListener!!.onCompletion(mp)
                }
            }
        }
    private val mErrorListener: MediaPlayer.OnErrorListener =
        object : MediaPlayer.OnErrorListener {
            override fun onError(
                mp: MediaPlayer?,
                what: Int,
                extra: Int
            ): Boolean {
                mCurrentState = STATE_ERROR
                mTargetState = STATE_ERROR
                if (mOnErrorListener != null) {
                    return mOnErrorListener!!.onError(mp, what, extra)
                }
                Toast.makeText(context, "Cannot play the video", Toast.LENGTH_LONG).show()
                return true
            }
        }
    private val mInfoListener: MediaPlayer.OnInfoListener =
        object : MediaPlayer.OnInfoListener {
            override fun onInfo(
                mp: MediaPlayer?,
                what: Int,
                extra: Int
            ): Boolean {
                return if (mOnInfoListener != null) {
                    mOnInfoListener!!.onInfo(mp, what, extra)
                } else true
            }
        }
    private val mBufferingUpdateListener: MediaPlayer.OnBufferingUpdateListener =
        object : MediaPlayer.OnBufferingUpdateListener {
            override fun onBufferingUpdate(
                mp: MediaPlayer?,
                percent: Int
            ) {
                if (mOnBufferingUpdateListener != null) {
                    mOnBufferingUpdateListener!!.onBufferingUpdate(mp, percent)
                }
            }
        }

    companion object {
        private val TAG = VideoView::class.java.simpleName
        private const val STATE_ERROR = -1
        private const val STATE_IDLE = 0
        private const val STATE_PREPARING = 1
        private const val STATE_PREPARED = 2
        private const val STATE_PLAYING = 3
        private const val STATE_PAUSED = 4
        private const val STATE_PLAYBACK_COMPLETED = 5
    }
}