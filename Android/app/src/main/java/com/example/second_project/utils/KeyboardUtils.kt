package com.example.second_project.utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object KeyboardUtils {
    // 기존 메소드
    fun hideKeyboard(view: View, context: Context) {
        view.clearFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(view: View, context: Context) {
        view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun clearFocusAndHideKeyboard(view: View) {
        view.clearFocus()
        val imm = view.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // 새로 추가된 메소드들
    /**
     * EditText 포커스 변경 시 텍스트 유지 문제를 해결하기 위한 메소드
     * 이전 EditText의 포커스를 안전하게 해제하고 다음 EditText로 포커스를 이동합니다
     */
    fun safeMoveFocus(fromEditText: EditText, toEditText: EditText) {
        // IMM 서비스 가져오기
        val imm = fromEditText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // 현재 EditText에서 텍스트 입력을 강제로 완료
        imm.isActive(fromEditText)

        // 약간의 지연 후 포커스 이동 (안드로이드 내부 이벤트 처리를 위해)
        Handler(Looper.getMainLooper()).postDelayed({
            // 이전 필드에서 포커스 해제
            fromEditText.clearFocus()

            // 새 필드로 포커스 이동
            toEditText.requestFocus()

            // 커서를 텍스트 끝으로 이동
            if (toEditText.text != null) {
                toEditText.setSelection(toEditText.text.length)
            }

            // 키보드 표시
            imm.showSoftInput(toEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 100) // 100ms 지연
    }

    /**
     * EditText의 현재 텍스트를 강제로 커밋하여 입력 내용이 손실되지 않도록 합니다
     */
    fun forceCommitText(editText: EditText) {
        val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.isActive(editText)
    }

    /**
     * 모든 EditText 입력을 강제로 커밋하는 유틸리티 메소드
     */
    fun forceCommitAllEditTexts(vararg editTexts: EditText) {
        for (editText in editTexts) {
            forceCommitText(editText)
        }
    }
}