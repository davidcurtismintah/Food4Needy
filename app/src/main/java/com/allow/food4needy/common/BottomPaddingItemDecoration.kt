package com.allow.food4needy.common

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class BottomPaddingItemDecoration(private val bottomPadding: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.bottom = bottomPadding
        }
    }
}
