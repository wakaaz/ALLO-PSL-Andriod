package com.net.pslapllication.util.customvideoview

import android.media.MediaCodec
import android.media.MediaFormat

internal class MediaCodecAudioDecoder(
    extractor: com.net.pslapllication.util.customvideoview.MediaExtractor?, passive: Boolean, trackIndex: Int,
    listener: OnDecoderEventListener?, private val mAudioPlayback: AudioPlayback
) : MediaCodecDecoder(extractor, passive, trackIndex, listener) {
    override fun configureCodec(codec: MediaCodec, format: MediaFormat?) {
        super.configureCodec(codec, format)
        mAudioPlayback.init(format)
    }

    override fun shouldDecodeAnotherFrame(): Boolean {
        // If this is an active audio track, decode and buffer only as much as this arbitrarily
        // chosen threshold time to avoid filling up the memory with buffered audio data and
        // requesting too much data from the network too fast (e.g. DASH segments).
        return if (!isPassive) {
            mAudioPlayback.queueBufferTimeUs < 200000
        } else {
            super.shouldDecodeAnotherFrame()
        }
    }

    override fun renderFrame(frameInfo: FrameInfo, offsetUs: Long) {
        frameInfo.data?.let { mAudioPlayback.write(it, frameInfo.presentationTimeUs) }
        releaseFrame(frameInfo)
    }

    override fun onOutputFormatChanged(format: MediaFormat?) {
        mAudioPlayback.init(format)
    }

    init {
        reinitCodec()
    }
}