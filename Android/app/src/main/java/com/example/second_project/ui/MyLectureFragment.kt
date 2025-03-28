package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.second_project.adapter.LectureViewPagerAdapter
import com.example.second_project.databinding.FragmentMyLectureBinding
import com.google.android.material.tabs.TabLayoutMediator

class MyLectureFragment : Fragment() {

    private var _binding: FragmentMyLectureBinding? = null
    private val binding get() = _binding!!

    // 뷰모델 사용 시
//    private val viewModel: MyLectureViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyLectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewPager2에 연결할 Adapter 설정
        val adapter = LectureViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "보유한 강의"
                1 -> tab.text = "참여한 강의"
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
