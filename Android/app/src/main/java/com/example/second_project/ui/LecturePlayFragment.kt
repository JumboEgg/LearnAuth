package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.R
import com.example.second_project.adapter.OwnedLectureDetailAdapter
import com.example.second_project.data.model.dto.response.SubLecture
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.databinding.FragmentLecturePlayBinding
import com.example.second_project.utils.YoutubeUtil
import com.example.second_project.viewmodel.OwnedLectureDetailViewModel
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

private const val TAG = "LecturePlayFragment_야옹"
class LecturePlayFragment: Fragment() {

    private var _binding: FragmentLecturePlayBinding? = null
    private val binding get() = _binding!!
    private var currentLectureId: Int = 0
    private var currentSubLectureId: Int = 0
    private var allSubLectures: List<SubLecture> = emptyList()

    private val viewModel: OwnedLectureDetailViewModel by lazy {
        OwnedLectureDetailViewModel(LectureDetailRepository())
    }

    private var youTubePlayer: YouTubePlayer? = null
    private var lastKnownSecondWatched: Int = 0
    private val watchTimeMap = mutableMapOf<Int, Int>() // 각 subLectureId별 시청 시간 저장



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLecturePlayBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: LecturePlayFragmentArgs by navArgs()
        currentLectureId = args.lectureId
        currentSubLectureId = args.subLectureId
//        currentSubLectureId = 16 //확인용 임시
        val userId = args.userId


        Log.d(TAG, "onViewCreated: $currentLectureId, $userId, $currentSubLectureId")

        viewModel.fetchLectureDetail(currentLectureId, userId)

        binding.playLectureName.isSelected = true

        // YouTubePlayerView 생명주기 등록
        binding.youtubePlayerView.apply {
            lifecycle.addObserver(this)
        }

        viewModel.lectureDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                allSubLectures = it.data.subLectures ?: emptyList()
                val subLecture = allSubLectures.find { sub -> sub.subLectureId == currentSubLectureId }

                if (it.data.lectureId == currentLectureId) {
                    binding.playLectureName.text = it.data.title
                }

                // subLecture가 null이 아닐 경우, 제목 설정
                if (subLecture != null) {
                    binding.playTitle.text = subLecture.subLectureTitle
                    binding.playNum.text = "${subLecture.lectureOrder}강"

                    val videoId = subLecture.lectureUrl
                    Log.d(TAG, "onViewCreated: ${subLecture.lectureUrl}")
                    Log.d(TAG, "썸네일: $videoId")
                    if (videoId != null) {
                        // ▶️ 유튜브 영상 로딩
                        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(player: YouTubePlayer) {
                                youTubePlayer = player
                                val startSecond = subLecture.continueWatching
                                player.cueVideo(videoId, startSecond.toFloat())
                            }
                            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                                lastKnownSecondWatched = second.toInt()
                                watchTimeMap[currentSubLectureId] = second.toInt()
                            }
                        })
                    } else {
                        Log.e(TAG, "onViewCreated: 유효한 유튜브 URL이 아님.", )
                    }
                } else {
                    binding.playTitle.text = "강의 제목 없음"
                    binding.playNum.text = " "
                }

                // RecyclerView 설정
                val adapter = OwnedLectureDetailAdapter(
                    subLectureList = allSubLectures,
                    onItemClick = { subLecture ->
                        saveCurrentWatchTime()
                        updateLectureContent(subLecture.subLectureId)
                    }
                )
                binding.playLectureList.layoutManager = LinearLayoutManager(requireContext())
                binding.playLectureList.adapter = adapter
                binding.playLectureList.isNestedScrollingEnabled = false

                //이전/다음 ui 업데이트
                updateBtnColors()
            }
        }

        // 이전, 다음 버튼
        binding.playPreviousBtnVisible.setOnClickListener {
            val currentLecture = allSubLectures.find { it.subLectureId == currentSubLectureId }
            val previousSubLecture = allSubLectures.find { it.lectureOrder == (currentLecture?.lectureOrder ?: 0) - 1 }

            if (previousSubLecture != null) {
                saveCurrentWatchTimeAndNavigate(previousSubLecture.subLectureId)
            } else {
                Toast.makeText(requireContext(), "이전 강의가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.playNextBtnVisible.setOnClickListener {
            val currentLecture = allSubLectures.find { it.subLectureId == currentSubLectureId }
            val nextSubLecture = allSubLectures.find { it.lectureOrder == (currentLecture?.lectureOrder ?: 0) + 1 }

            if (nextSubLecture != null) {
                saveCurrentWatchTimeAndNavigate(nextSubLecture.subLectureId)
            } else {
                Toast.makeText(requireContext(), "다음 강의가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.lectureDetailBack.setOnClickListener {
            saveCurrentWatchTime()
            findNavController().popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                saveCurrentWatchTime()
                findNavController().popBackStack()
            }
        })

    }

    private fun updateLectureContent(subLectureId: Int) {
        val subLecture = allSubLectures.find { it.subLectureId == subLectureId }
        if (subLecture != null) {
            currentSubLectureId = subLectureId

            binding.playTitle.text = subLecture.subLectureTitle
            binding.playNum.text = "${subLecture.lectureOrder}강"

            val videoId = subLecture.lectureUrl
            val startSecond = watchTimeMap[subLectureId] ?: subLecture.continueWatching
            if (!videoId.isNullOrEmpty()) {
                youTubePlayer?.pause()
                youTubePlayer?.cueVideo(videoId, startSecond.toFloat())
                // 디버깅용 로그
                Log.d("startSecond", "비디오 $subLectureId 로드 중, 위치: $startSecond")
            } else {
                Log.e(TAG, "updateLectureContent: 유효한 유튜브 URL이 아님.")
            }

            updateBtnColors()
        }
    }

    private fun updateBtnColors() {
        val previousSubLecture = allSubLectures.find { it.subLectureId == currentSubLectureId - 1 }
        val nextSubLecture = allSubLectures.find { it.subLectureId == currentSubLectureId + 1 }

        Log.d(TAG, "updateBtnColors: allSubLectures size = ${allSubLectures.size}")
        allSubLectures.forEach {
            Log.d(TAG, "SubLecture: id=${it.subLectureId}, title=${it.subLectureTitle}")
        }
        Log.d(TAG, "updateBtnColors: $previousSubLecture, $nextSubLecture")

        if (previousSubLecture != null) {
            binding.playPreviousBtnVisible.visibility = View.VISIBLE
            binding.playPreviousGone.visibility = View.GONE
        } else {
            binding.playPreviousBtnVisible.visibility = View.GONE
            binding.playPreviousGone.visibility = View.VISIBLE
        }

        if (nextSubLecture != null) {
            binding.playNextBtnVisible.visibility = View.VISIBLE
            binding.playNextBtnGone.visibility = View.GONE
        } else {
            binding.playNextBtnVisible.visibility = View.GONE
            binding.playNextBtnGone.visibility = View.VISIBLE
        }

    }

    private fun saveCurrentWatchTime() {
        val lectureData = viewModel.lectureDetail.value?.data ?: return
        val userLectureId = lectureData.userLectureId
        val subLecture = lectureData.subLectures.find { it.subLectureId == currentSubLectureId } ?: return
        val lectureLength = subLecture.lectureLength

        val currentTimeSec = lastKnownSecondWatched
        val endFlag = currentTimeSec >= lectureLength * 0.98

        viewModel.saveWatchTime(userLectureId, currentSubLectureId, currentTimeSec, endFlag)
        viewModel.updateLastViewedLecture(userLectureId, currentSubLectureId)
    }

    private fun saveCurrentWatchTimeAndNavigate(newSubLectureId: Int) {
        // 현재 진행 상황 먼저 저장
        saveCurrentWatchTime()

        // 그 다음 새 콘텐츠로 이동
        updateLectureContent(newSubLectureId)

        // UI 업데이트
        updateBtnColors()
    }
}