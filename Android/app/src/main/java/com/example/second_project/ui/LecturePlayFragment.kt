package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.adapter.OwnedLectureDetailAdapter
import com.example.second_project.data.model.dto.response.SubLecture
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.databinding.FragmentLecturePlayBinding
import com.example.second_project.viewmodel.OwnedLectureDetailViewModel

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
        
        val args = arguments?.let { LecturePlayFragmentArgs.fromBundle(it) }
        currentLectureId = args?.lectureId ?: 0
        val userId = args?.userId ?: 0
        currentSubLectureId = 6 //임시!!! 입니다. 현재 DB가 안정화되지 않아 기입합니다.

        Log.d(TAG, "onViewCreated: $currentLectureId, $userId, $currentSubLectureId")

        viewModel.fetchLectureDetail(currentLectureId, userId)

        binding.playLectureName.isSelected = true

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
                    binding.playNum.text = "${subLecture.subLectureId}강"
                } else {
                    binding.playTitle.text = "강의 제목 없음"
                    binding.playNum.text = " "
                }

                // RecyclerView 설정
                val adapter = OwnedLectureDetailAdapter(subLectureList = allSubLectures)
                binding.playLectureList.layoutManager = LinearLayoutManager(requireContext())
                binding.playLectureList.adapter = adapter
            }
        }

        // 이전, 다음 버튼
        binding.playPreviousBtn.setOnClickListener {
            val previousSubLecture = allSubLectures.find { it.subLectureId == currentSubLectureId - 1 }
            if (previousSubLecture != null) {
                currentSubLectureId--
                updateLectureContent(currentSubLectureId)
            } else {
                Toast.makeText(requireContext(), "이전 강의가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.playNextBtn.setOnClickListener {
            val nextSubLecture = allSubLectures.find { it.subLectureId == currentSubLectureId + 1 }
            if (nextSubLecture != null) {
                currentSubLectureId++
                updateLectureContent(currentSubLectureId)
            } else {
                Toast.makeText(requireContext(), "다음 강의가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLectureContent(subLectureId: Int) {
        val subLecture = allSubLectures.find { it.subLectureId == subLectureId }
        if (subLecture != null) {
            binding.playTitle.text = subLecture.subLectureTitle
            binding.playNum.text = "${subLecture.subLectureId}강"
        }
    }

}