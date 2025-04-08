package com.example.second_project.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity

class LoadingOverlayHelper (
    private val activity: FragmentActivity,
    private val overlayView: View,
    private val catView: View? = null // 고양이 뷰는 선택적
){
    private var backCallback: OnBackPressedCallback? = null
    private var isOverlayVisible = false

    fun show() {
        overlayView.visibility = View.VISIBLE
        overlayView.isClickable = true
        overlayView.isFocusable = true
        isOverlayVisible = true

        // 뒤로 가기 차단
        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 아무 동작도 하지 않음
            }
        }
        activity.onBackPressedDispatcher.addCallback(backCallback!!)

        // 고양이 달리기 시작
        startCatRun()
    }

    fun hide() {
        overlayView.visibility = View.GONE
        overlayView.isClickable = false
        overlayView.isFocusable = false
        isOverlayVisible = false

        // 뒤로 가기 해제
        backCallback?.remove()
        backCallback = null

        // 고양이 멈춤
        stopCatRun()
    }

    private fun startCatRun() {
        catView ?: return

        // 먼저 달리기 전 위치 초기화
        catView.translationX = 0f

        overlayView.post {
            val parentWidth = overlayView.width
            val catWidth = catView.width

            val distanceX = if (parentWidth == 0 || catWidth == 0) {
                600f
            } else {
                (parentWidth - catWidth).toFloat()
            }

            doSingleRun(distanceX)
        }
    }

    private fun doSingleRun(distanceX: Float) {
        if (!isOverlayVisible || catView == null) return

        catView.translationX = 0f

        val anim = TranslateAnimation(
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, distanceX,
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, 0f
        ).apply {
            duration = 2000
            fillAfter = true
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    catView.post {
                        if (isOverlayVisible) {
                            doSingleRun(distanceX)
                        }
                    }
                }
                override fun onAnimationRepeat(animation: Animation) {}
            })
        }

        catView.startAnimation(anim)
    }

    private fun stopCatRun() {
        catView?.clearAnimation()
    }
}