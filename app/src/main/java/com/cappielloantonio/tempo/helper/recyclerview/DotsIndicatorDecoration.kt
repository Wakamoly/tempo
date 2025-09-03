package com.cappielloantonio.tempo.helper.recyclerview

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import kotlin.math.ceil
import kotlin.math.max

class DotsIndicatorDecoration(
    radius: Int,
    padding: Int,
    indicatorHeight: Int,
    @ColorInt colorInactive: Int,
    @ColorInt colorActive: Int
) : ItemDecoration() {
    private val indicatorHeight: Int
    private val indicatorItemPadding: Int
    private val radius: Int

    private val inactivePaint = Paint()
    private val activePaint = Paint()

    init {
        val strokeWidth = Resources.getSystem().displayMetrics.density * 1
        this.radius = radius

        inactivePaint.strokeCap = Paint.Cap.ROUND
        inactivePaint.strokeWidth = strokeWidth
        inactivePaint.style = Paint.Style.STROKE
        inactivePaint.isAntiAlias = true
        inactivePaint.setColor(colorInactive)

        activePaint.strokeCap = Paint.Cap.ROUND
        activePaint.strokeWidth = strokeWidth
        activePaint.style = Paint.Style.FILL
        activePaint.isAntiAlias = true
        activePaint.setColor(colorActive)

        this.indicatorItemPadding = padding
        this.indicatorHeight = indicatorHeight
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        if (parent.adapter == null) return

        val itemCount = ceil(parent.adapter!!.itemCount.toDouble() / 5).toInt()

        if (itemCount <= 1) {
            return
        }

        // center horizontally, calculate width and subtract half from center
        val totalLength = (this.radius * 2 * itemCount).toFloat()
        val paddingBetweenItems = (max(0, itemCount - 1) * indicatorItemPadding).toFloat()
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f

        // center vertically in the allotted space
        val indicatorPosY =
            parent.height - indicatorHeight - indicatorItemPadding.toFloat() / 4

        drawInactiveDots(c, indicatorStartX, indicatorPosY, itemCount)

        val activePosition: Int

        if (parent.layoutManager is GridLayoutManager) {
            activePosition =
                (parent.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
        } else if (parent.layoutManager is LinearLayoutManager) {
            activePosition =
                (parent.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        } else {
            // not supported layout manager
            return
        }

        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }

        // find offset of active page if the user is scrolling
        val activeChild = parent.layoutManager!!.findViewByPosition(activePosition)
        if (activeChild == null) {
            return
        }

        drawActiveDot(c, indicatorStartX, indicatorPosY, activePosition)
    }

    private fun drawInactiveDots(
        c: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        itemCount: Int
    ) {
        // width of item indicator including padding
        val itemWidth = (this.radius * 2 + indicatorItemPadding).toFloat()

        var start = indicatorStartX + radius
        for (i in 0 until itemCount) {
            c.drawCircle(start, indicatorPosY, radius.toFloat(), inactivePaint)
            start += itemWidth
        }
    }

    private fun drawActiveDot(
        c: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        highlightPosition: Int
    ) {
        // width of item indicator including padding
        val itemWidth = (this.radius * 2 + indicatorItemPadding).toFloat()
        val highlightStart =
            ceil((indicatorStartX + radius + itemWidth * highlightPosition / 5).toDouble()).toFloat()
        c.drawCircle(highlightStart, indicatorPosY, radius.toFloat(), activePaint)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = indicatorHeight
    }

    companion object {
        private const val TAG = "DotsIndicatorDecoration"
    }
}