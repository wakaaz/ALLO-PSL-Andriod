package com.net.pslapllication.util.customvideoview

import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

internal class Timeline {
    /**
     * A linked list that stores the sequence of cues in the timeline ascending by time.
     */
    private var mList: LinkedList<Cue>? = null

    /**
     * The iterator to traverse the timeline sequentially.
     */
    private var mListIterator: ListIterator<Cue>? = null

    /**
     * Tracks the current position in the list so we can easily create a new list iterator
     * when necessary.
     */
    private var mListPosition = 0

    /**
     * A hashtable to keep track of cues in the timeline that can be used to check for existing
     * cues in O(1). This is solely used to determine the return value of [.removeCue]
     * and to keep track of the [.count].
     */
    private var mCues: HashSet<Cue>? = null

    /**
     * A list of cues to be added by the next [.setPlaybackPosition] or
     * [.movePlaybackPosition].
     *
     * We do not insert the cues directly into the timeline for performance reasons:
     * - to avoid the need to execute the playback position methods in a synchronized block
     * - because batch insertions can be done with a single iteration of the timeline
     */
    private var mCuesToAdd: ArrayList<Cue>? = null

    /**
     * A list of cues to be removed by the next [.setPlaybackPosition] or
     * [.movePlaybackPosition].
     *
     * Same performance reasons as [.mCuesToAdd].
     */
    private var mCuesToRemove: ArrayList<Cue>? = null

    /**
     * Keeps track of the number of additions and removals so we can determine when the cues
     * have been added/removed and we need to do a [.updateCueList].
     */
    private var mModCount = 0

    /**
     * Keeps track of the number of modifications after the last [.updateCueList]. Is used
     * together with [.mModCount] to determine if the cue list needs to be updated.
     */
    private var mLastUpdateModCount = 0

    /**
     * Sorts cues in ascending time order.
     */
    private val mCueTimeSortComparator: Comparator<Cue?> = object : Comparator<Cue?> {
        override fun compare(lhs: Cue?, rhs: Cue?): Int {
            return if (lhs!!.time == rhs!!.time) {
                0
            } else if (lhs.time < rhs.time) {
                -1
            } else {
                1
            }
        }


    }

    @Synchronized
    fun addCue(cue: Cue) {
        mCuesToAdd!!.add(cue)
        mCues!!.add(cue)
        mModCount++
    }

    @Synchronized
    fun removeCue(cue: Cue): Boolean {
        if (mCues!!.contains(cue)) {
            mCues!!.remove(cue)
            mCuesToRemove!!.add(cue)
            mModCount++
            return true
        }
        return false
    }

    /**
     * Sets the playback position to a new position without announcing cues, e.g. when seeking.
     * @param position the new playback position
     */
    fun setPlaybackPosition(position: Int) {
        if (mModCount != mLastUpdateModCount) {
            // Update the timeline list if cues have been added or removed
            // We check the mod count here to avoid an unnecessary function call
            updateCueList()
        }

        // Create a new iterator, ...
        val iterator: ListIterator<Cue> = mList!!.listIterator()

        // move to the desired position, ...
        while (iterator.hasNext()) {
            val c = iterator.next()
            if (c.time > position) {
                break
            }
        }
        if (iterator.hasPrevious()) {
            iterator.previous()
        }

        // And replace the previous iterator
        mListIterator = iterator
        mListPosition = iterator.nextIndex()
    }

    /**
     * Moves the playback position from the current to the requested position, announcing all
     * passed cues that are positioned in between.
     * @param position the new playback position
     * @param listener a listener that receives all cues between the previous and new position
     */
    fun movePlaybackPosition(position: Int, listener: OnCueListener) {
        if (mModCount != mLastUpdateModCount) {
            // Update the timeline list if cues have been added or removed
            // We check the mod count here to avoid an unnecessary function call
            updateCueList()
        }

        // Move through the timeline and announce cues
        while (mListIterator!!.hasNext()) {
            val cue = mListIterator!!.next()
            mListPosition++
            if (cue.time <= position) {
                listener.onCue(cue)
            } else {
                mListIterator!!.previous()
                mListPosition--
                break
            }
        }
    }

    /**
     * Gets the number of cues.
     * @return the number of cues
     */
    fun count(): Int {
        return mCues!!.size
    }

    /**
     * Resets the timeline to its initial empty state.
     */
    @Synchronized
    fun reset() {
        mList = LinkedList()
        mListIterator = mList!!.listIterator()
        mListPosition = 0
        mCues = HashSet()
        mCuesToAdd = ArrayList()
        mCuesToRemove = ArrayList()
        mModCount = 0
        mLastUpdateModCount = 0
    }

    @Synchronized
    private fun updateCueList() {
        if (!mCuesToAdd!!.isEmpty()) {
            Collections.sort(mCuesToAdd, mCueTimeSortComparator)
            var cuesToAddIndex = 0
            val iterator: MutableListIterator<Cue> = mList!!.listIterator()

            // Add cues into list
            while (cuesToAddIndex < mCuesToAdd!!.size && iterator.hasNext()) {
                val cue = iterator.next()
                if (cue.time < mCuesToAdd!![cuesToAddIndex].time) {
                    iterator.add(mCuesToAdd!![cuesToAddIndex])
                    cuesToAddIndex++
                    val cueIndex = iterator.previousIndex()
                    if (cueIndex < mListPosition) {
                        mListPosition++
                    }
                }
            }

            // Append remaining cues to end of list
            while (cuesToAddIndex < mCuesToAdd!!.size) {
                iterator.add(mCuesToAdd!![cuesToAddIndex])
                cuesToAddIndex++
            }
            mCuesToAdd!!.clear()
        }
        if (!mCuesToRemove!!.isEmpty()) {
            val cuesToRemove = HashSet(mCuesToRemove)
            var cuesToRemoveIndex = 0
            val iterator: MutableListIterator<Cue> = mList!!.listIterator()
            while (cuesToRemoveIndex < mCuesToRemove!!.size && iterator.hasNext()) {
                val cue = iterator.next()
                if (cuesToRemove.contains(cue)) {
                    iterator.remove()
                    cuesToRemoveIndex++
                    val cueIndex = iterator.nextIndex()
                    if (cueIndex < mListPosition) {
                        mListPosition--
                    }
                }
            }
            mCuesToRemove!!.clear()
        }
        mLastUpdateModCount = mModCount

        // We possibly modified the cue list so we need to create a new iterator instance
        mListIterator = mList!!.listIterator(mListPosition)
    }

    interface OnCueListener {
        fun onCue(cue: Cue?)
    }

    init {
        reset()
    }
}