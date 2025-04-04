package com.example.second_project.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.adapter.BannerAdapter
import com.example.second_project.adapter.LectureAdapter
import com.example.second_project.databinding.FragmentMainBinding
import com.example.second_project.viewmodel.MainViewModel
import java.io.File

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var recommendedAdapter: LectureAdapter
    private lateinit var recentAdapter: LectureAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val walletPath = UserSession.walletFilePath
        val walletPassword = UserSession.walletPassword

        val walletFile = File(requireContext().filesDir, walletPath)
        val contents = walletFile.readText()
        Log.d("MainFragment", "🧾 Keystore JSON 내용: $contents")
        val nickname = UserSession.nickname ?: "(닉네임)"
        binding.recommendTitle.text = "${nickname}님을 위한 추천 강의"

        Log.d("MainFragment", "✅ UserSession.userId = ${UserSession.userId}")


        Log.d("MainFragment", "✅ UserSession.nickname = $nickname")
        Log.d("MainFragment", "✅ UserSession.walletFilePath = $walletPath")

        Log.d("MainFragment", "✅ UserSession.walletPassword = $walletPassword")
        // dp -> px 변환
        val spacing = dpToPx(4)

        // 추천 강의 (랜덤 강의) RecyclerView 설정
        recommendedAdapter = LectureAdapter(mainPage = true)
        binding.recommendList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recommendList.adapter = recommendedAdapter
        binding.recommendList.addItemDecoration(HorizontalSpacingItemDecoration(spacing))

        // 최근 등록 강의 RecyclerView 설정
        recentAdapter = LectureAdapter(mainPage = true)
        binding.newList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.newList.adapter = recentAdapter
        binding.newList.addItemDecoration(HorizontalSpacingItemDecoration(spacing))

        // ViewModel의 추천 강의 데이터 관찰 (랜덤 강의)
        viewModel.randomLectures.observe(viewLifecycleOwner) { lectureList ->
            if (lectureList.isNullOrEmpty()) {
                // 데이터가 없으면 adapter에 빈 리스트 전달
                recommendedAdapter.submitList(emptyList())
            } else {
                recommendedAdapter.submitList(lectureList)
            }
        }

        // ViewModel의 최근 등록 강의 데이터 관찰
        viewModel.recentLectures.observe(viewLifecycleOwner) { lectureList ->
            if (lectureList.isNullOrEmpty()) {
                recentAdapter.submitList(emptyList())
            } else {
                recentAdapter.submitList(lectureList)
            }
        }

        // 배너 설정
        val bannerList = listOf(
            R.drawable.sample_plzdelete,
            R.drawable.sample_plzdelete2,
            R.drawable.sample_plzdelete3
        )
        bannerAdapter = BannerAdapter(bannerList)
        val viewPager = view.findViewById<ViewPager2>(R.id.bannerArea)
        viewPager.adapter = bannerAdapter

        // 자동 슬라이드 기능
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            var currentItem = 0
            override fun run() {
                if (currentItem >= bannerList.size) {
                    currentItem = 0
                }
                viewPager.currentItem = currentItem++
                handler.postDelayed(this, 3000)
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
