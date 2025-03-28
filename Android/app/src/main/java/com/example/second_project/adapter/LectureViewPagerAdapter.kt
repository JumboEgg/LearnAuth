package com.example.second_project.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.second_project.ui.OwnedLectureFragment
import com.example.second_project.ui.ParticipatedLectureFragment

class LectureViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    // 탭 2개 (보유한 강의, 참여한 강의)
    override fun getItemCount(): Int = 2

    // position 에 따라 각각 다른 Fragment 반환
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OwnedLectureFragment()      // 보유한 강의
            else -> ParticipatedLectureFragment()  // 참여한 강의
        }
    }
}
