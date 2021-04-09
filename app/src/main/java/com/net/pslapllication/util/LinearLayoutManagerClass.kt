package com.net.pslapllication.util

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.net.pslapllication.BuildConfig
import java.lang.reflect.Field;


class LinearLayoutManagerClass : LinearLayoutManager {
    private val childDimensions: IntArray? = IntArray(2)
    private val view: RecyclerView?
    private var childSize: Int = LinearLayoutManagerClass.Companion.DEFAULT_CHILD_SIZE
    private var hasChildSize = false
    private var overScrollMode = ViewCompat.OVER_SCROLL_ALWAYS
    private val tmpRect: Rect = Rect()

    constructor(context: Context?) : super(context) {
        view = null
    }

    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
        context,
        orientation,
        reverseLayout
    ) {
        view = null
    }

    constructor(view: RecyclerView) : super(view.context) {
        this.view = view
        overScrollMode = ViewCompat.getOverScrollMode(view)
    }

    constructor(
        view: RecyclerView,
        orientation: Int,
        reverseLayout: Boolean
    ) : super(view.context, orientation, reverseLayout) {
        this.view = view
        overScrollMode = ViewCompat.getOverScrollMode(view)
    }

    fun setOverScrollMode(overScrollMode: Int) {
        require(!(overScrollMode < ViewCompat.OVER_SCROLL_ALWAYS || overScrollMode > ViewCompat.OVER_SCROLL_NEVER)) { "Unknown overscroll mode: $overScrollMode" }
        checkNotNull(view) { "view == null" }
        this.overScrollMode = overScrollMode
        ViewCompat.setOverScrollMode(view, overScrollMode)
    }

    override fun onMeasure(
        recycler: Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        val widthMode: Int = View.MeasureSpec.getMode(widthSpec)
        val heightMode: Int = View.MeasureSpec.getMode(heightSpec)
        val widthSize: Int = View.MeasureSpec.getSize(widthSpec)
        val heightSize: Int = View.MeasureSpec.getSize(heightSpec)
        val hasWidthSize = widthMode != View.MeasureSpec.UNSPECIFIED
        val hasHeightSize = heightMode != View.MeasureSpec.UNSPECIFIED
        val exactWidth = widthMode == View.MeasureSpec.EXACTLY
        val exactHeight = heightMode == View.MeasureSpec.EXACTLY
        val unspecified: Int = LinearLayoutManagerClass.Companion.makeUnspecifiedSpec()
        if (exactWidth && exactHeight) {
            // in case of exact calculations for both dimensions let's use default "onMeasure" implementation
            super.onMeasure(recycler, state, widthSpec, heightSpec)
            return
        }
        val vertical = getOrientation() === VERTICAL
        initChildDimensions(widthSize, heightSize, vertical)
        var width = 0
        var height = 0

        // it's possible to get scrap views in recycler which are bound to old (invalid) adapter entities. This
        // happens because their invalidation happens after "onMeasure" method. As a workaround let's clear the
        // recycler now (it should not cause any performance issues while scrolling as "onMeasure" is never
        // called whiles scrolling)
        recycler.clear()
        val stateItemCount = state.itemCount
        val adapterItemCount: Int = getItemCount()
        // adapter always contains actual data while state might contain old data (f.e. data before the animation is
        // done). As we want to measure the view with actual data we must use data from the adapter and not from  the
        // state
        for (i in 0 until adapterItemCount) {
            if (vertical) {
                if (!hasChildSize) {
                    if (i < stateItemCount) {
                        // we should not exceed state count, otherwise we'll get IndexOutOfBoundsException. For such items
                        // we will use previously calculated dimensions
                        measureChild(recycler, i, widthSize, unspecified, childDimensions)
                    } else {
                        logMeasureWarning(i)
                    }
                }
                height += childDimensions!![LinearLayoutManagerClass.Companion.CHILD_HEIGHT]
                if (i == 0) {
                    width = childDimensions[LinearLayoutManagerClass.Companion.CHILD_WIDTH]
                }
                if (hasHeightSize && height >= heightSize) {
                    break
                }
            } else {
                if (!hasChildSize) {
                    if (i < stateItemCount) {
                        // we should not exceed state count, otherwise we'll get IndexOutOfBoundsException. For such items
                        // we will use previously calculated dimensions
                        measureChild(recycler, i, unspecified, heightSize, childDimensions)
                    } else {
                        logMeasureWarning(i)
                    }
                }
                width += childDimensions!![LinearLayoutManagerClass.Companion.CHILD_WIDTH]
                if (i == 0) {
                    height = childDimensions[LinearLayoutManagerClass.Companion.CHILD_HEIGHT]
                }
                if (hasWidthSize && width >= widthSize) {
                    break
                }
            }
        }
        if (exactWidth) {
            width = widthSize
        } else {
            width += getPaddingLeft() + getPaddingRight()
            if (hasWidthSize) {
                width = Math.min(width, widthSize)
            }
        }
        if (exactHeight) {
            height = heightSize
        } else {
            height += getPaddingTop() + getPaddingBottom()
            if (hasHeightSize) {
                height = Math.min(height, heightSize)
            }
        }
        setMeasuredDimension(width, height)
        if (view != null && overScrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS) {
            val fit = (vertical && (!hasHeightSize || height < heightSize)
                    || !vertical && (!hasWidthSize || width < widthSize))
            ViewCompat.setOverScrollMode(
                view,
                if (fit) ViewCompat.OVER_SCROLL_NEVER else ViewCompat.OVER_SCROLL_ALWAYS
            )
        }
    }

    private fun logMeasureWarning(child: Int) {
        if (BuildConfig.DEBUG) {
            Log.w(
                "LinearLayoutManager",
                "Can't measure child #" + child + ", previously used dimensions will be reused." +
                        "To remove this message either use #setChildSize() method or don't run RecyclerView animations"
            )
        }
    }

    private fun initChildDimensions(
        width: Int,
        height: Int,
        vertical: Boolean
    ) {
        if (childDimensions!![LinearLayoutManagerClass.Companion.CHILD_WIDTH] != 0 || childDimensions[LinearLayoutManagerClass.Companion.CHILD_HEIGHT] != 0
        ) {
            // already initialized, skipping
            return
        }
        if (vertical) {
            childDimensions[LinearLayoutManagerClass.Companion.CHILD_WIDTH] = width
            childDimensions[LinearLayoutManagerClass.Companion.CHILD_HEIGHT] = childSize
        } else {
            childDimensions[LinearLayoutManagerClass.Companion.CHILD_WIDTH] = childSize
            childDimensions[LinearLayoutManagerClass.Companion.CHILD_HEIGHT] = height
        }
    }

    override fun setOrientation(orientation: Int) {
        // might be called before the constructor of this class is called
        if (childDimensions != null) {
            if (getOrientation() !== orientation) {
                childDimensions[LinearLayoutManagerClass.Companion.CHILD_WIDTH] = 0
                childDimensions[LinearLayoutManagerClass.Companion.CHILD_HEIGHT] = 0
            }
        }
        super.setOrientation(orientation)
    }

    fun clearChildSize() {
        hasChildSize = false
        setChildSize(LinearLayoutManagerClass.Companion.DEFAULT_CHILD_SIZE)
    }

    fun setChildSize(childSize: Int) {
        hasChildSize = true
        if (this.childSize != childSize) {
            this.childSize = childSize
            requestLayout()
        }
    }

    private fun measureChild(
        recycler: Recycler,
        position: Int,
        widthSize: Int,
        heightSize: Int,
        dimensions: IntArray?
    ) {
        val child: View
        child = try {
            recycler.getViewForPosition(position)
        } catch (e: IndexOutOfBoundsException) {
            if (BuildConfig.DEBUG) {
                Log.w(
                    "LinearLayoutManager",
                    "LinearLayoutManager doesn't work well with animations. Consider switching them off",
                    e
                )
            }
            return
        }
        val p = child.getLayoutParams() as RecyclerView.LayoutParams
        val hPadding: Int = getPaddingLeft() + getPaddingRight()
        val vPadding: Int = getPaddingTop() + getPaddingBottom()
        val hMargin = p.leftMargin + p.rightMargin
        val vMargin = p.topMargin + p.bottomMargin

        // we must make insets dirty in order calculateItemDecorationsForChild to work
        LinearLayoutManagerClass.Companion.makeInsetsDirty(p)
        // this method should be called before any getXxxDecorationXxx() methods
        calculateItemDecorationsForChild(child, tmpRect)
        val hDecoration: Int = getRightDecorationWidth(child) + getLeftDecorationWidth(child)
        val vDecoration: Int = getTopDecorationHeight(child) + getBottomDecorationHeight(child)
        val childWidthSpec: Int = getChildMeasureSpec(
            widthSize,
            hPadding + hMargin + hDecoration,
            p.width,
            canScrollHorizontally()
        )
        val childHeightSpec: Int = getChildMeasureSpec(
            heightSize,
            vPadding + vMargin + vDecoration,
            p.height,
            canScrollVertically()
        )
        child.measure(childWidthSpec, childHeightSpec)
        dimensions!![LinearLayoutManagerClass.Companion.CHILD_WIDTH] =
            getDecoratedMeasuredWidth(child) + p.leftMargin + p.rightMargin
        dimensions[LinearLayoutManagerClass.Companion.CHILD_HEIGHT] =
            getDecoratedMeasuredHeight(child) + p.bottomMargin + p.topMargin

        // as view is recycled let's not keep old measured values
        LinearLayoutManagerClass.Companion.makeInsetsDirty(p)
        recycler.recycleView(child)
    }

    companion object {
        private var canMakeInsetsDirty = true
        private var insetsDirtyField: Field? = null
        private const val CHILD_WIDTH = 0
        private const val CHILD_HEIGHT = 1
        private const val DEFAULT_CHILD_SIZE = 100
        fun makeUnspecifiedSpec(): Int {
            return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        }

        private fun makeInsetsDirty(p: RecyclerView.LayoutParams) {
            if (!LinearLayoutManagerClass.Companion.canMakeInsetsDirty) {
                return
            }
            try {
                if (LinearLayoutManagerClass.Companion.insetsDirtyField == null) {
                    LinearLayoutManagerClass.Companion.insetsDirtyField =
                        RecyclerView.LayoutParams::class.java.getDeclaredField("mInsetsDirty")
                    LinearLayoutManagerClass.Companion.insetsDirtyField!!.setAccessible(true)
                }
                LinearLayoutManagerClass.Companion.insetsDirtyField!!.set(p, true)
            } catch (e: NoSuchFieldException) {
                LinearLayoutManagerClass.Companion.onMakeInsertDirtyFailed()
            } catch (e: IllegalAccessException) {
                LinearLayoutManagerClass.Companion.onMakeInsertDirtyFailed()
            }
        }

        private fun onMakeInsertDirtyFailed() {
            LinearLayoutManagerClass.Companion.canMakeInsetsDirty = false
            if (BuildConfig.DEBUG) {
                Log.w(
                    "LinearLayoutManager",
                    "Can't make LayoutParams insets dirty, decorations measurements might be incorrect"
                )
            }
        }
    }
}
