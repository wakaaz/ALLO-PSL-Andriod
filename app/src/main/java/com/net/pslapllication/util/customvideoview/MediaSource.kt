package com.net.pslapllication.util.customvideoview

import android.media.MediaExtractor
import java.io.IOException


interface MediaSource {
    /**
     * Returns a media extractor for video data and possibly multiplexed audio data.
     */
    @get:Throws(IOException::class)
    val videoExtractor: com.net.pslapllication.util.customvideoview.MediaExtractor

    /**
     * Returns a media extractor for audio data from a separate audio stream, or NULL if the source
     * does not have a separate audio source or the audio is multiplexed with the video in a single
     * stream.
     */
    @get:Throws(IOException::class)
    val audioExtractor: com.net.pslapllication.util.customvideoview.MediaExtractor?
}