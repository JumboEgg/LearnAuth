package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.R
import com.example.second_project.adapter.RegisterSublectureAdapter
import com.example.second_project.databinding.FragmentRegisterSublectureBinding
import com.example.second_project.interfaces.RegisterStepSavable
import com.example.second_project.utils.YoutubeUtil
import com.example.second_project.viewmodel.RegisterViewModel

class RegisterSublectureFragment: Fragment(), RegisterStepSavable {

    private var _binding: FragmentRegisterSublectureBinding? = null
    private val binding get() = _binding!!
    private lateinit var sublectureAdapter: RegisterSublectureAdapter
    private val viewModel: RegisterViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterSublectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerSubLectures.layoutManager = LinearLayoutManager(requireContext())

        // 어댑터 초기화
        sublectureAdapter = RegisterSublectureAdapter(
            subLectureCount = { sublectureAdapter.itemCount },
            onDeleteClick = { position ->
                // 삭제 동작은 어댑터 내에서 처리되므로 여기선 따로 할 일 없음
            },
            // 불러오기 버튼 클릭
            onLoadVideoClick = { position, url ->
                val videoId = YoutubeUtil.extractVideoId(url)
                if (videoId != null) {
                    viewModel.fetchYoutubeMetaData(
                        videoId = videoId,
                        apiKey = "AIzaSyBoQEjW4Cx2Z6p0DB5OniYbA8caxC88WYc", // string 리소스에서 불러오거나 BuildConfig 사용
                        onResult = { title, durationSeconds ->
                            val thumbnailUrl = YoutubeUtil.getThumbnailUrl(videoId)

                            // 어댑터의 특정 위치 아이템 업데이트
                            val tempList = sublectureAdapter.getTempSubLectures().toMutableList()
                            val item = tempList[position]
                            item.videoTitle = title
                            item.duration = durationSeconds
                            item.videoId = videoId
                            item.thumbnailUrl = thumbnailUrl

                            sublectureAdapter.setItems(tempList)
                        },
                        onError = { message ->
                            Toast.makeText(requireContext(), "유튜브 정보 불러오기 실패: $message", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(requireContext(), "올바른 YouTube 링크가 아닙니다.", Toast.LENGTH_SHORT).show()
                }

            }

        )

        // ✅ 기존 데이터 있으면 어댑터에 세팅
        if (viewModel.tempSubLectures.isNotEmpty()) {
            sublectureAdapter.setItems(viewModel.tempSubLectures)
        }


        binding.recyclerSubLectures.adapter = sublectureAdapter
        binding.recyclerSubLectures.visibility = View.VISIBLE

        // 개별 강의 추가하기 버튼 클릭
        binding.btnAddSubLecture.setOnClickListener {
            sublectureAdapter.addItem()
            binding.recyclerSubLectures.scrollToPosition(sublectureAdapter.itemCount - 1)
        }

        binding.btnToQuiz.setOnClickListener {
            (parentFragment as? RegisterMainFragment)?.moveToStep(4)
        }
    }

    override fun saveDataToViewModel(): Boolean {
        val tempLectures = sublectureAdapter.getTempSubLectures()

        tempLectures.forEachIndexed { index, lecture ->
            val videoId = YoutubeUtil.extractVideoId(lecture.inputUrl)
            if (videoId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "${index + 1}번째 강의의 유효한 링크를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return false
            }
            lecture.videoId = videoId // 추출된 ID로 업데이트
        }

        // ✅ ViewModel에 임시 저장만 해두자
        viewModel.tempSubLectures.clear()
        viewModel.tempSubLectures.addAll(tempLectures)

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}