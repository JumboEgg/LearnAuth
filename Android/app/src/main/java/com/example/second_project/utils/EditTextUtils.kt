package com.example.second_project.utils
import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText

class EnterLimitFilter(private val maxEnter: Int) : InputFilter {
    override fun filter(
        source: CharSequence?, start: Int, end: Int,
        dest: Spanned?, dstart: Int, dend: Int
    ): CharSequence? {
        val currentText = dest?.toString() ?: ""
        val newText = currentText.substring(0, dstart) +
                source?.subSequence(start, end).toString() +
                currentText.substring(dend)

        val enterCount = newText.count { it == '\n' }

        return if (enterCount > maxEnter) {
            "" // 입력 막음
        } else null // 허용
    }
}

/**
 * EditText에 엔터 제한 필터 설정
 */
fun EditText.setEnterLimit(maxEnter: Int) {
    val currentFilters = filters.orEmpty().toMutableList()
    // 기존에 같은 필터가 있으면 제거하고 다시 추가
    currentFilters.removeAll { it is EnterLimitFilter }
    currentFilters.add(EnterLimitFilter(maxEnter))
    filters = currentFilters.toTypedArray()
}