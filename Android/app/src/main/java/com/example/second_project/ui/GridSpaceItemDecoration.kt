//import android.graphics.Rect
//import android.view.View
//import androidx.recyclerview.widget.RecyclerView

package com.example.second_project.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpaceItemDecoration(private val spanCount: Int, private val space: Int): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount  // 열 번호 계산 (0부터 시작)

        // 각 아이템에 대해 여백 설정
        outRect.left = space
        outRect.right = space

        // 첫 번째 행에 상단 여백 추가
        if (position < spanCount) {
            outRect.top = space
        }

        // 마지막 행에 하단 여백 추가
        if (position >= state.itemCount - spanCount) {
            outRect.bottom = space
        }

        // 첫 번째 열에 대해서는 좌측 여백을 0으로 하지 않음
        // 첫 번째 열에서 좌측 여백을 0으로 하지 않음 (그래서 좌측 여백을 항상 추가)
        if (column == 0) {
            outRect.left = space  // 첫 번째 열에도 좌측 여백을 추가
        }

        // 마지막 열에 대해서는 우측 여백을 0으로 하지 않음 (그래서 우측 여백을 항상 추가)
        if (column == spanCount - 1) {
            outRect.right = space  // 마지막 열에도 우측 여백을 추가
        }
    }
}



//class GridSpacingItemDecoration(
//    private val spanCount: Int, // Grid의 column 수
//    private val spacing: Int // 간격
//) : RecyclerView.ItemDecoration() {
//
//    override fun getItemOffsets(
//        outRect: Rect,
//        view: View,
//        parent: RecyclerView,
//        state: RecyclerView.State
//    ) {
//        val position: Int = parent.getChildAdapterPosition(view)
//
//        if (position >= 0) {
//            val column = position % spanCount // item column
//            outRect.apply {
//                // spacing - column * ((1f / spanCount) * spacing)
//                left = spacing - column * spacing / spanCount
//                // (column + 1) * ((1f / spanCount) * spacing)
//                right = (column + 1) * spacing / spanCount
//                if (position < spanCount) top = spacing
//                bottom = spacing
//            }
//        } else {
//            outRect.apply {
//                left = 0
//                right = 0
//                top = 0
//                bottom = 0
//            }
//        }
//    }
//}