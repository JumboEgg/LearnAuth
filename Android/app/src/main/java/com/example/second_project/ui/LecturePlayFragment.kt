package com.example.second_project.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.second_project.UserSession.userId
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

private const val TAG = "LecturePlayFragment_야옹"
class LecturePlayFragment: Fragment() {

    private var _binding: FragmentLecturePlayBinding? = null
    private val binding get() = _binding!!
    private var currentLectureId: Int = 0
    private var currentSubLectureId: Int = 0
    private var playingSubLectureId: Int = 0 // 현재 재생 중인 sublecture의 ID를 저장하는 변수 추가
    private var allSubLectures: List<SubLecture> = emptyList()
    private var isFirstLoad: Boolean = true // 최초 로드 여부를 확인하는 플래그 추가

    private val viewModel: OwnedLectureDetailViewModel by lazy {
        OwnedLectureDetailViewModel(LectureDetailRepository())
    }

    private var youTubePlayer: YouTubePlayer? = null
    private var lastKnownSecondWatched: Int = 0
    private val watchTimeMap = mutableMapOf<Int, Int>() // 각 subLectureId별 시청 시간 저장
    
    // 자동 저장을 위한 Handler 추가
    private val autoSaveHandler = Handler(Looper.getMainLooper())
    private val autoSaveRunnable = object : Runnable {
        override fun run() {
            saveCurrentWatchTime(currentSubLectureId)
            autoSaveHandler.postDelayed(this, 4000) // 5초마다 실행
        }
    }

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
        playingSubLectureId = args.subLectureId // playingSubLectureId 초기화
//        currentSubLectureId = 16 //확인용 임시
        val userId = args.userId


        Log.d(TAG, "onViewCreated: $currentLectureId, $userId, $currentSubLectureId")

        // 초기 버튼 상태 설정 - 모든 버튼 숨기기
        binding.playPreviousBtnVisible.visibility = View.GONE
        binding.playPreviousGone.visibility = View.GONE
        binding.playNextBtnVisible.visibility = View.GONE
        binding.playNextBtnGone.visibility = View.GONE

        viewModel.fetchLectureDetail(currentLectureId, userId)

        binding.playLectureName.isSelected = true

        // YouTubePlayerView 생명주기 등록
        binding.youtubePlayerView.apply {
            lifecycle.addObserver(this)
        }

        viewModel.lectureDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                // 1. 기본 데이터 설정 (빠른 데이터 처리)
                allSubLectures = it.data.subLectures ?: emptyList()
                
                // 2. 강의 제목 설정 (텍스트만 있는 빠른 UI)
                if (it.data.lectureId == currentLectureId) {
                    binding.playLectureName.text = it.data.title
                }

                // 3. 현재 서브강의 정보 찾기
                val subLecture = allSubLectures.find { sub -> sub.subLectureId == currentSubLectureId }

                // 4. 기본 텍스트 정보 설정 (빠른 UI)
                if (subLecture != null) {
                    binding.playTitle.text = subLecture.subLectureTitle
                    binding.playNum.text = "${subLecture.lectureOrder}강"
                } else {
                    binding.playTitle.text = "강의 제목 없음"
                    binding.playNum.text = " "
                }

                // 5. 이전/다음 버튼 상태 업데이트 (빠른 UI)
                updateBtnColors()

                // 6. RecyclerView 설정 (썸네일이 포함된 리스트)
                val adapter = OwnedLectureDetailAdapter(
                    subLectureList = allSubLectures,
                    onItemClick = { subLecture ->
                        saveCurrentWatchTime(currentSubLectureId)
                        updateLectureContent(subLecture.subLectureId)
                    }
                )
                binding.playLectureList.layoutManager = LinearLayoutManager(requireContext())
                binding.playLectureList.adapter = adapter
                binding.playLectureList.isNestedScrollingEnabled = false

                // 7. 유튜브 영상 로딩 (가장 느린 작업)
                if (subLecture != null) {
                    val videoId = subLecture.lectureUrl
                    Log.d(TAG, "onViewCreated: ${subLecture.lectureUrl}")
                    Log.d(TAG, "썸네일: $videoId")
                    if (videoId != null) {
                        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(player: YouTubePlayer) {
                                youTubePlayer = player
                                val startSecond = subLecture.continueWatching
                                player.cueVideo(videoId, startSecond.toFloat())
                                // 영상이 준비되면 자동 저장 시작
                                autoSaveHandler.post(autoSaveRunnable)
                            }
                            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                                lastKnownSecondWatched = second.toInt()
                                watchTimeMap[playingSubLectureId] = second.toInt()
                                Log.d(TAG, "재생 시간 업데이트: subLectureId=$playingSubLectureId, 시간=${second.toInt()}초")
                            }
                        })
                    } else {
                        Log.e(TAG, "onViewCreated: 유효한 유튜브 URL이 아님.", )
                    }
                }
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
            saveCurrentWatchTime(currentSubLectureId) {
                findNavController().popBackStack()
            }

//            findNavController().popBackStack()
//            Handler(Looper.getMainLooper()).postDelayed({
//                findNavController().popBackStack()
//            }, 300)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                saveCurrentWatchTime(currentSubLectureId){
                    findNavController().popBackStack()
                }
//                findNavController().popBackStack()
            }
        })

    }

    private fun updateLectureContent(subLectureId: Int) {
        val subLecture = allSubLectures.find { it.subLectureId == subLectureId }
        if (subLecture != null) {
            // 현재 sublecture의 재생 시간 저장
            val prevSubLectureId = currentSubLectureId
            val prevTimeSec = lastKnownSecondWatched
            
            // currentSubLectureId 업데이트
            currentSubLectureId = subLectureId
            playingSubLectureId = subLectureId // playingSubLectureId도 함께 업데이트

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
            
            // 서버에서 최신 데이터 가져오기
            refreshSubLectureList()


            // 이전/다음 버튼 상태 업데이트
            updateBtnColors()

            // 이전 sublecture의 재생 시간 저장
            if (prevTimeSec > 0) {
                watchTimeMap[prevSubLectureId] = prevTimeSec
                Log.d(TAG, "이전 sublecture 재생 시간 저장: subLectureId=$prevSubLectureId, 시간=${prevTimeSec}초")
            }
        }
    }

    // 서버에서 최신 sublecture 리스트를 가져오는 메서드
    private fun refreshSubLectureList() {
        val lectureData = viewModel.lectureDetail.value?.data ?: return
        val userId = userId
        
        // 서버에서 최신 데이터 가져오기
        viewModel.fetchLectureDetail(currentLectureId, userId)
        
        // 데이터가 업데이트되면 UI 갱신
        viewModel.lectureDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                allSubLectures = it.data.subLectures ?: emptyList()
                
                // 완강 상태 로그 추가
                allSubLectures.forEach { subLecture ->
                    Log.d(TAG, "서버에서 가져온 sublecture 상태: subLectureId=${subLecture.subLectureId}, 제목=${subLecture.subLectureTitle}, 완강여부=${subLecture.endFlag}, 시청시간=${subLecture.continueWatching}초")
                }
                
                // RecyclerView 어댑터 갱신
                val adapter = OwnedLectureDetailAdapter(
                    subLectureList = allSubLectures,
                    onItemClick = { subLecture ->
                        saveCurrentWatchTime(subLecture.subLectureId)
                        updateLectureContent(subLecture.subLectureId)
                    }
                )
                binding.playLectureList.adapter = adapter
                
                Log.d(TAG, "서버에서 sublecture 리스트 갱신 완료: 총 ${allSubLectures.size}개")
            }
        }
    }

    private fun updateBtnColors() {
        // 현재 sublecture 찾기
        val currentSubLecture = allSubLectures.find { it.subLectureId == currentSubLectureId }
        
        // 현재 sublecture의 순서를 기준으로 이전/다음 sublecture 찾기
        val currentOrder = currentSubLecture?.lectureOrder ?: 0
        val previousSubLecture = if (currentOrder > 0) {
            allSubLectures.find { it.lectureOrder == currentOrder - 1 }
        } else {
            null
        }
        val nextSubLecture = allSubLectures.find { it.lectureOrder == currentOrder + 1 }

        Log.d(TAG, "updateBtnColors: allSubLectures size = ${allSubLectures.size}")
        Log.d(TAG, "updateBtnColors: currentOrder = $currentOrder")
        Log.d(TAG, "updateBtnColors: previousSubLecture = $previousSubLecture")
        Log.d(TAG, "updateBtnColors: nextSubLecture = $nextSubLecture")
        Log.d(TAG, "updateBtnColors: isFirstLoad = $isFirstLoad")

        // 이전 버튼 상태 업데이트
        if (previousSubLecture != null) {
            binding.playPreviousBtnVisible.visibility = View.VISIBLE
            binding.playPreviousGone.visibility = View.GONE
        } else {
            binding.playPreviousBtnVisible.visibility = View.GONE
            binding.playPreviousGone.visibility = View.VISIBLE
        }

        // 다음 버튼 상태 업데이트
        if (nextSubLecture != null) {
            binding.playNextBtnVisible.visibility = View.VISIBLE
            binding.playNextBtnGone.visibility = View.GONE
        } else {
            binding.playNextBtnVisible.visibility = View.GONE
            binding.playNextBtnGone.visibility = View.VISIBLE
        }
        
        // 최초 로드 완료 표시
        if (isFirstLoad) {
            isFirstLoad = false
        }
    }

    private fun saveCurrentWatchTime(forSubLectureId: Int, onComplete: () -> Unit = {}) {
        val lectureData = viewModel.lectureDetail.value?.data ?: return
        val userLectureId = lectureData.userLectureId
        val subLecture = lectureData.subLectures.find { it.subLectureId == forSubLectureId } ?: return
        val lectureLength = subLecture.lectureLength

        // watchTimeMap에서 forSubLectureId에 해당하는 시청 시간을 가져옵니다.
        // 현재 재생 중인 sublecture인 경우 watchTimeMap의 값을 우선적으로 사용하고, 없으면 lastKnownSecondWatched를 사용합니다.
        val currentTimeSec = if (forSubLectureId == playingSubLectureId) {
            // watchTimeMap에 값이 있으면 그 값을 사용하고, 없으면 lastKnownSecondWatched를 사용합니다.
            watchTimeMap[forSubLectureId] ?: lastKnownSecondWatched
        } else {
            watchTimeMap[forSubLectureId] ?: subLecture.continueWatching
        }
        
        // 현재 시간이 0이면 이전에 저장된 시간을 사용합니다.
        val finalTimeSec = if (currentTimeSec == 0 && forSubLectureId == playingSubLectureId) {
            subLecture.continueWatching
        } else {
            currentTimeSec
        }
        
        // 완강 여부 판단 (강의 길이의 98% 이상 시청 시 완강으로 간주)
        val endFlag = finalTimeSec >= lectureLength * 0.80
        
        // 완강 여부 로그 추가
        Log.d(TAG, "완강 여부 판단: subLectureId=$forSubLectureId, 시간=${finalTimeSec}초, 강의길이=${lectureLength}초, 완강여부=$endFlag")

        Log.d(TAG, "재생 시간 저장: subLectureId=$forSubLectureId, 시간=${finalTimeSec}초, 완강여부=$endFlag, lastKnownSecondWatched=$lastKnownSecondWatched, watchTimeMap=${watchTimeMap[forSubLectureId]}")

        // 서버에 저장 요청
        viewModel.saveWatchTime(userLectureId, forSubLectureId, finalTimeSec, endFlag)
        viewModel.updateLastViewedLecture(userLectureId, forSubLectureId)
        
        // 로컬에서 sublecture 상태 업데이트
        var needUpdate = false
        if (endFlag && !subLecture.endFlag) {
            val index = allSubLectures.indexOfFirst { it.subLectureId == forSubLectureId }
            if (index != -1) {
                val updatedSubLecture = subLecture.copy(endFlag = true)
                val updatedList = allSubLectures.toMutableList()
                updatedList[index] = updatedSubLecture
                allSubLectures = updatedList
                needUpdate = true
                Log.d(TAG, "sublecture 완강 상태 업데이트: subLectureId=$forSubLectureId")
            }
        }
        
        // watchTimeMap 업데이트
        if (finalTimeSec > 0) {
            watchTimeMap[forSubLectureId] = finalTimeSec
            Log.d(TAG, "watchTimeMap 업데이트: subLectureId=$forSubLectureId, 시간=${finalTimeSec}초")
        }
        
        // watchTimeMap이 업데이트되었으므로 UI 갱신
        if (needUpdate || finalTimeSec > 0) {
            // RecyclerView 어댑터 갱신
            (binding.playLectureList.adapter as? OwnedLectureDetailAdapter)?.updateSubLectureList(allSubLectures)
            Log.d(TAG, "sublecture 리스트 갱신: subLectureId=$forSubLectureId, 시간=${finalTimeSec}초")
        }

        onComplete()
    }

    private fun saveCurrentWatchTimeAndNavigate(newSubLectureId: Int) {
        val prevId = currentSubLectureId
        // 현재 진행 상황 저장 (자동 저장이 있지만, 이동 시점의 정확한 시간을 위해 한 번 더 저장)
        saveCurrentWatchTime(prevId)
        // 새로운 sublecture로 이동
        updateLectureContent(newSubLectureId)
    }
    
    // sublecture로 이동할 때 리스트를 갱신하는 메서드
    private fun updateSubLectureList(newSubLectureId: Int) {
        // 현재 시청 중인 sublecture의 상태 업데이트
        val currentSubLecture = allSubLectures.find { it.subLectureId == currentSubLectureId }
        currentSubLecture?.let { subLecture ->
            // 현재 시청 시간이 강의 길이의 98% 이상이면 완강으로 표시
            val currentTimeSec = watchTimeMap[currentSubLectureId] ?: subLecture.continueWatching
            val lectureLength = subLecture.lectureLength
            val isCompleted = currentTimeSec >= lectureLength * 0.98
            
            // 완강 상태 업데이트
            if (isCompleted && !subLecture.endFlag) {
                // 로컬에서만 상태 업데이트 (실제 DB 업데이트는 saveCurrentWatchTime에서 처리)
                val updatedSubLecture = subLecture.copy(endFlag = true)
                val index = allSubLectures.indexOfFirst { it.subLectureId == currentSubLectureId }
                if (index != -1) {
                    // 리스트는 불변(immutable)이므로 새로운 리스트를 생성해야 함
                    val updatedList = allSubLectures.toMutableList()
                    updatedList[index] = updatedSubLecture
                    allSubLectures = updatedList
                }
            }
        }
        
        // 새로운 sublecture로 이동
        currentSubLectureId = newSubLectureId
        
        // 서버에서 최신 데이터 가져오기
        refreshSubLectureList()
        
        // 이전/다음 버튼 상태 업데이트
        updateBtnColors()
    }

    override fun onPause() {
        super.onPause()
        // 자동 저장 중지
        autoSaveHandler.removeCallbacks(autoSaveRunnable)
        // 마지막으로 한 번 더 저장
        saveCurrentWatchTime(currentSubLectureId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 자동 저장 중지
        autoSaveHandler.removeCallbacks(autoSaveRunnable)
        // 마지막으로 한 번 더 저장
        saveCurrentWatchTime(currentSubLectureId)
        binding.youtubePlayerView.release()
        _binding = null
    }
}