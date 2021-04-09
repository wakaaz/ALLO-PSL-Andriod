package com.net.pslapllication.util.customvideoview

class Cue internal constructor(
    /**
     * Gets the time at which this cue is cued. This must not necessarily be the exact playback
     * time as cue events can be slightly delayed. Use [MediaPlayer.getCurrentPosition]
     * to get the current playback time instead.
     * @return the time at which this cue is cued
     */
    val time: Int,
    /**
     * Gets the custom data object attached to this cue.
     * @return the data attached to this cue
     */
    val data: Any?
) {

    /**
     * Checks if this cue has data attached.
     * @return true if this cue has data attached, else false
     */
    fun hasData(): Boolean {
        return data != null
    }

    override fun toString(): String {
        return "Cue{" +
                "time=" + time +
                ", data=" + data +
                '}'
    }

}