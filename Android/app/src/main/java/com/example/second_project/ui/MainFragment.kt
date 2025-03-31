package com.example.second_project.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.second_project.R
import com.example.second_project.adapter.BannerAdapter
import com.example.second_project.adapter.LectureAdapter
import com.example.second_project.databinding.FragmentMainBinding
import com.example.second_project.viewmodel.MainViewModel

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private lateinit var bannerAdapter: BannerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 예시: ViewModel 데이터 관찰
//        viewModel.text.observe(viewLifecycleOwner) {
//            binding.mainText.text = it
//        }

        // dp -> px로 변환
        val spacing = dpToPx(4) // 16dp 여백 설정

        // 추천 강의
        binding.recommendList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val lectureAdapter = LectureAdapter(mainPage = true)
        binding.recommendList.adapter = lectureAdapter
        binding.recommendList.addItemDecoration(HorizontalSpacingItemDecoration(spacing))

        // 예시 아이템 넣기
        lectureAdapter.submitList(listOf(1, 2, 3, 4, 5))

        // 최근 등록 강의
        binding.newList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.newList.adapter = lectureAdapter
        binding.newList.addItemDecoration(HorizontalSpacingItemDecoration(spacing))

        // 배너 설정
        val bannerList = listOf(
            R.drawable.sample_plzdelete,
            R.drawable.sample_plzdelete2,
            R.drawable.sample_plzdelete3
        )

        bannerAdapter = BannerAdapter(bannerList)

        val viewPager = view.findViewById<ViewPager2>(R.id.bannerArea)
        viewPager.adapter = bannerAdapter

        // 자동 슬라이드 기능 추가
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            var currentItem = 0
            override fun run() {
                if (currentItem >= bannerList.size) {
                    currentItem = 0
                }
                viewPager.currentItem = currentItem++
                handler.postDelayed(this, 3000) // 3초마다 변경
            }
        }
        handler.postDelayed(runnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
