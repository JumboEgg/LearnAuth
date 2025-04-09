package com.example.second_project.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.adapter.BannerAdapter
import com.example.second_project.adapter.LectureAdapter
import com.example.second_project.databinding.FragmentMainBinding
import com.example.second_project.viewmodel.MainViewModel

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var recommendedAdapter: LectureAdapter
    private lateinit var recentAdapter: LectureAdapter
    private val bannerHandler = Handler(Looper.getMainLooper())
    private var isUserInteracting = false
    private var isAutoScrolling = false
    private val bannerRunnable = object : Runnable {
        override fun run() {
            if (!isUserInteracting && !isAutoScrolling) {
                isAutoScrolling = true
                val nextItem = (binding.bannerArea.currentItem + 1) % 3
                binding.bannerArea.currentItem = nextItem
                isAutoScrolling = false
            }
            bannerHandler.postDelayed(this, 3000)
        }
    }

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

//        val walletFile = File(requireContext().filesDir, walletPath)
//        val contents = walletFile.readText()
//        Log.d("MainFragment", "🧾 Keystore JSON 내용: $contents")
        val nickname = UserSession.nickname ?: "(닉네임)"
        binding.recommendTitle.text = "${nickname}님을 위한 추천 강의"

        Log.d("MainFragment", "✅ UserSession.userId = ${UserSession.userId}")
        Log.d("MainFragment", "✅ UserSession.nickname = $nickname")
        Log.d("MainFragment", "✅ UserSession.walletFilePath = $walletPath")
        Log.d("MainFragment", "✅ UserSession.walletPassword = $walletPassword")
        
        // dp -> px 변환
        val spacing = dpToPx(4)

        // '강의 올리기' 버튼 클릭 이벤트 설정
        binding.uploadBtn.setOnClickListener {
            findNavController().navigate(R.id.action_nav_main_to_registerMainFragment)
        }

        // 추천 강의 (랜덤 강의) RecyclerView 설정
        recommendedAdapter = LectureAdapter(mainPage = true) { lectureId, title ->
            // 강의 클릭 시 상세 정보 불러오기 -> 이동
            val lectureDetailLiveData = viewModel.loadLectureDetail(lectureId, UserSession.userId)
            lectureDetailLiveData.observe(viewLifecycleOwner) { lectureDetail ->
                lectureDetail?.let {
                    val action = if (it.data.owned == false) {
                        MainFragmentDirections.actionNavMainToLectureDetailFragment(
                            lectureId = it.data.lectureId,
                            userId = UserSession.userId
                        )
                    } else {
                        MainFragmentDirections.actionNavMainToOwnedLectureDetailFragment(
                            lectureId = it.data.lectureId,
                            userId = UserSession.userId
                        )
                    }
                    findNavController().navigate(action)
                }
            }
        }
        binding.recommendList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recommendList.adapter = recommendedAdapter
        binding.recommendList.addItemDecoration(HorizontalSpacingItemDecoration(spacing))

        // 최근 등록 강의 RecyclerView 설정
        recentAdapter = LectureAdapter(mainPage = true) { lectureId, title ->
            // 강의 클릭 시 상세 정보 불러오기 -> 이동
            val lectureDetailLiveData = viewModel.loadLectureDetail(lectureId, UserSession.userId)
            lectureDetailLiveData.observe(viewLifecycleOwner) { lectureDetail ->
                lectureDetail?.let {
                    val action = if (it.data.owned == false) {
                        MainFragmentDirections.actionNavMainToLectureDetailFragment(
                            lectureId = it.data.lectureId,
                            userId = UserSession.userId
                        )
                    } else {
                        MainFragmentDirections.actionNavMainToOwnedLectureDetailFragment(
                            lectureId = it.data.lectureId,
                            userId = UserSession.userId
                        )
                    }
                    findNavController().navigate(action)
                }
            }
        }
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

        // 배너 설정 - 가장 많이 수료한 강의 데이터 사용
        setupBanner()
        
        // 자동 슬라이드 시작
        startBannerAutoSlide()
    }
    
    private fun setupBanner() {
        // 가장 많이 수료한 강의 데이터 관찰
        viewModel.mostCompletedLectures.observe(viewLifecycleOwner) { lectureList ->
            if (lectureList.isNullOrEmpty()) {
                // 데이터가 없으면 빈 리스트 전달
                bannerAdapter = BannerAdapter(emptyList())
            } else {
                // 최대 3개의 강의만 사용
                val bannerLectures = lectureList.take(3)
                bannerAdapter = BannerAdapter(bannerLectures) { lecture ->
                    // 배너 클릭 시 해당 강의 상세 페이지로 이동
                    val lectureDetailLiveData = viewModel.loadLectureDetail(lecture.lectureId, UserSession.userId)
                    lectureDetailLiveData.observe(viewLifecycleOwner) { lectureDetail ->
                        lectureDetail?.let {
                            val action = if (it.data.owned == false) {
                                MainFragmentDirections.actionNavMainToLectureDetailFragment(
                                    lectureId = it.data.lectureId,
                                    userId = UserSession.userId
                                )
                            } else {
                                MainFragmentDirections.actionNavMainToOwnedLectureDetailFragment(
                                    lectureId = it.data.lectureId,
                                    userId = UserSession.userId
                                )
                            }
                            findNavController().navigate(action)
                        }
                    }
                }
            }
            binding.bannerArea.adapter = bannerAdapter
            
            // 페이지 변경 이벤트 감지
            binding.bannerArea.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (!isUserInteracting) {
                        // 타이머 리셋
                        bannerHandler.removeCallbacks(bannerRunnable)
                        bannerHandler.postDelayed(bannerRunnable, 3000)
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    when (state) {
                        androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING -> {
                            isUserInteracting = true
                            bannerHandler.removeCallbacks(bannerRunnable)
                        }
                        androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE -> {
                            isUserInteracting = false
                            bannerHandler.postDelayed(bannerRunnable, 3000)
                        }
                    }
                }
            })

            // 터치 이벤트 감지
            binding.bannerArea.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isUserInteracting = true
                        bannerHandler.removeCallbacks(bannerRunnable)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isUserInteracting = false
                        bannerHandler.postDelayed(bannerRunnable, 3000)
                    }
                }
                false
            }
        }
    }
    
    private fun startBannerAutoSlide() {
        bannerHandler.postDelayed(bannerRunnable, 3000)
    }
    
    override fun onPause() {
        super.onPause()
        // 화면이 일시 중지될 때 자동 슬라이드 중지
        bannerHandler.removeCallbacks(bannerRunnable)
    }
    
    override fun onResume() {
        super.onResume()
        // 화면이 다시 표시될 때 자동 슬라이드 재시작
        startBannerAutoSlide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // 화면이 파괴될 때 자동 슬라이드 중지
        bannerHandler.removeCallbacks(bannerRunnable)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
