package com.net.pslapllication.util.customvideoview

import android.content.Context
import android.net.Uri
import java.io.IOException;

class UriSource : MediaSource {
    private var mContext: Context
    private var mUri: Uri
    private var mAudioUri: Uri? = null
      lateinit var headers: Map<String, String>
        private set
    var audioHeaders: Map<String, String>? = null
        private set

/**
     * Creates a media source from a URI. The media source must either be a video stream
     * or a multiplexed audio/video stream.
     * @param context the context to open the URI in
     * @param uri the URI pointing to the media source
     * @param headers the headers to be passed with the request to the URI*/


    constructor(
        context: Context,
        uri: Uri,
        headers: Map<String, String>?
    ) {
        mContext = context
        mUri = uri
        this.headers = headers!!
    }

/**
     * Creates a media source from a URI. The media source must either be a video stream
     * or a multiplexed audio/video stream.
     * @param context the context to open the URI in
     * @param uri the URI pointing to the media source*/


    constructor(context: Context, uri: Uri) {
        mContext = context
        mUri = uri
    }

/**
     * Creates a media source from separate video and audio URIs.
     * @param context the context to open the URIs in
     * @param videoUri the URI pointing to the video source
     * @param videoHeaders the headers to be passed with the request to the video URI
     * @param audioUri the URI pointing to the audio source
     * @param audioHeaders the headers to be passed with the request to the audio URI*/


    constructor(
        context: Context,
        videoUri: Uri,
        videoHeaders: Map<String, String>?,
        audioUri: Uri?,
        audioHeaders: Map<String, String>?
    ) {
        mContext = context
        mUri = videoUri
        headers = videoHeaders!!
        mAudioUri = audioUri
        this.audioHeaders = audioHeaders
    }

/**
     * Creates a media source from separate video and audio URIs.
     * @param context the context to open the URIs in
     * @param videoUri the URI pointing to the video source
     * @param audioUri the URI pointing to the audio source*/


    constructor(context: Context, videoUri: Uri, audioUri: Uri?) {
        mContext = context
        mUri = videoUri
        mAudioUri = audioUri
    }

    val context: Context
        get() = mContext

    val uri: Uri
        get() = mUri

    val audioUri: Uri?
        get() = mAudioUri

    @get:Throws(IOException::class)
    override val videoExtractor:MediaExtractor
        get() {
            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(mContext, mUri, headers)
            return mediaExtractor
        }
    override val audioExtractor:  MediaExtractor?
        get() {
            if (mAudioUri != null)
            {
                // In case of a separate audio file Uri, return an audio extractor
                val mediaExtractor:MediaExtractor? = MediaExtractor()
                mediaExtractor!!.setDataSource(mContext, mAudioUri, audioHeaders)
                return mediaExtractor
            }
            return null
        }


}
