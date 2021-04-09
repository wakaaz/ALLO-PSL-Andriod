package com.net.pslapllication.util.customvideoview

class TimeBase {
    private var mStartTime: Long = 0

    /**
     * Sets the playback speed. Can be used for fast forward and slow motion.
     * speed 0.5 = half speed / slow motion
     * speed 2.0 = double speed / fast forward
     * @param speed
     */
    var speed = 1.0

    fun start() {
        startAt(0)
    }

    fun startAt(mediaTime: Long) {
        mStartTime = microTime() - mediaTime
    }

    val currentTime: Long
        get() = microTime() - mStartTime

    fun getOffsetFrom(from: Long): Long {
        return from - currentTime
    }

    private fun microTime(): Long {
        return (System.nanoTime() / 1000 * speed).toLong()
    }

    init {
        start()
    }
}