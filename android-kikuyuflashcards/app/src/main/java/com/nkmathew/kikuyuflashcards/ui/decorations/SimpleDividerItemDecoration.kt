package com.nkmathew.kikuyuflashcards.ui.decorations

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nkmathew.kikuyuflashcards.R

/**
 * Simple divider decoration for RecyclerView items
 */
class SimpleDividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        // Use a subtle color that works with both dark and light themes
        color = ContextCompat.getColor(context, R.color.divider_color)
        strokeWidth = context.resources.displayMetrics.density * 1f // 1dp line
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // Get the left and right coordinates for the divider lines
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        // Draw divider for each item except the last one
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            // Calculate top and bottom positions for the divider
            val top = child.bottom + params.bottomMargin
            val bottom = top + 1

            // Draw the divider line
            c.drawLine(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }
}