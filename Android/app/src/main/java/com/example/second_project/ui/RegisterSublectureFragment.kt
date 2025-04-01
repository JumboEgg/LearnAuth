package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.adapter.RegisterSublectureAdapter
import com.example.second_project.databinding.FragmentRegisterSublectureBinding
import com.example.second_project.interfaces.RegisterStepSavable
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
                val videoId = extractYoutubeVideoId(url)
                if (videoId != null) {
                    Toast.makeText(requireContext(), "추출된 ID: $videoId", Toast.LENGTH_SHORT).show()
                    // 추후 fetchYoutubeMetaData(videoId, ...) 호출할 예정
                } else {
                    Toast.makeText(requireContext(), "올바른 YouTube 링크가 아닙니다.", Toast.LENGTH_SHORT).show()
                }

            }

        )

        // ✅ 기존 데이터 있으면 어댑터에 세팅
        if (viewModel.subLectures.isNotEmpty()) {
            sublectureAdapter.setItems(viewModel.subLectures)
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
        val lectures = sublectureAdapter.getSubLectures()

        val hasEmptyTitleOrUrl = lectures.any {
            it.subLectureTitle.isBlank() || it.subLectureUrl.isBlank()
        }

        if (hasEmptyTitleOrUrl) {
            Toast.makeText(requireContext(), "모든 개별 강의의 제목과 링크를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        viewModel.subLectures.clear()
        viewModel.subLectures.addAll(lectures)

        return true
    }

    // 영상 링크에서 영상 id 추출
    private fun extractYoutubeVideoId(url: String): String? {
        val regex = Regex(
            "(?:https?://)?(?:www\\.)?(?:youtube\\.com/(?:watch\\?v=|embed/|v/)|youtu\\.be/)([\\w-]{11})"
        )
        val matchResult = regex.find(url)
        return matchResult?.groups?.get(1)?.value
    }

    private fun fetchYoutubeMetaData(videoId: String, onResult: (title: String, thumbnailUrl: String, duration: Int) -> Unit) {
        // retrofit 호출 or Web API 요청
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}