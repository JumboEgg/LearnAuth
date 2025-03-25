package com.example.second_project.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        // 첫 번째 아이템은 왼쪽 여백을 추가, 나머지는 오른쪽 여백을 추가
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        // 첫 번째 아이템일 경우 왼쪽 여백 추가
        if (position == 0) {
            outRect.left = 40
        }

        if (position == itemCount - 1) {
            outRect.right = 40
        }

        // 모든 아이템에 오른쪽 여백 추가
        if (position != itemCount-1) outRect.right = spacing
    }
}
