package com.net.pslapllication.util.customvideoview

import android.media.MediaExtractor

import java.io.File
import java.io.IOException


class FileSource  : MediaSource {
    private var mFile: File
    private var mAudioFile: File? = null

    /**
     * Creates a media source from a local file. The file can be either video-only, or multiplexed
     * audio/video.
     * @param file the av source file
     */
    constructor(file: File) {
        mFile = file
    }

    /**
     * Creates a media source from separate local video and audio files.
     * @param videoFile the video source file
     * @param audioFile the audio source file
     */
    constructor(videoFile: File, audioFile: File?) {
        mFile = videoFile
        mAudioFile = audioFile
    }

    val file: File
        get() = mFile

    val audioFile: File?
        get() = mAudioFile

    @get:Throws(IOException::class)
    override val videoExtractor: com.net.pslapllication.util.customvideoview.MediaExtractor
        get() {
            val mediaExtractor = com.net.pslapllication.util.customvideoview.MediaExtractor()
            mediaExtractor.setDataSource(mFile.getAbsolutePath())
            return mediaExtractor
        }

    // In case of a separate audio file, return an audio extractor
    @get:Throws(IOException::class)
    override val audioExtractor: com.net.pslapllication.util.customvideoview.MediaExtractor?
        get() {
            if (mAudioFile != null) {
                // In case of a separate audio file, return an audio extractor
                val mediaExtractor = com.net.pslapllication.util.customvideoview.MediaExtractor()
                mediaExtractor.setDataSource(mAudioFile!!.getAbsolutePath())
                return mediaExtractor
            }
            // We do not need a separate audio extractor when only a single (multiplexed) file
            // is passed into this class.
            return null
        }
}