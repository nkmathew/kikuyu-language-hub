package com.nkmathew.kikuyuflashcards.ui.decorations

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Item decoration for adding vertical margins between RecyclerView items
 */
class VerticalMarginItemDecoration(context: Context, verticalMarginDp: Int) : RecyclerView.ItemDecoration() {
    private val margin: Int

    init {
        // Convert DP to pixels
        val density = context.resources.displayMetrics.density
        margin = (verticalMarginDp * density).toInt()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // Add margin only to the bottom of each item
        // Apply to all items except the last one
        val position = parent.getChildAdapterPosition(view)
        if (position != parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = margin
        }
    }
}