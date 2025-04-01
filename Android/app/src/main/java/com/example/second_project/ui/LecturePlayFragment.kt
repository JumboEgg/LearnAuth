package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.databinding.FragmentLecturePlayBinding
import com.example.second_project.viewmodel.OwnedLectureDetailViewModel

private const val TAG = "LecturePlayFragment_야옹"
class LecturePlayFragment: Fragment() {

    private var _binding: FragmentLecturePlayBinding? = null
    private val binding get() = _binding!!

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
        val lectureId = args?.lectureId ?: 0
        val userId = args?.userId ?: 0
//        val subLectureId = args?.subLectureId ?: 0
         val subLectureId = 6 //임시!!! 입니다. 현재 DB가 안정화되지 않아 기입합니다.

        Log.d(TAG, "onViewCreated: $lectureId, $userId, $subLectureId")

        viewModel.fetchLectureDetail(lectureId, userId)

        binding.playLectureName.isSelected = true

        viewModel.lectureDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                val subLecture = it.data.subLectures?.find { sub -> sub.subLectureId == subLectureId }

                // subLectures가 null이 아닌지 확인하고, subLecture가 null인 경우를 처리
                Log.d(TAG, "sublectures: ${it.data.subLectures}")
                Log.d(TAG, "찾으려는 subLectureId: $subLectureId")
                Log.d(TAG, "subLecture: $subLecture")

                if (it.data.lectureId == lectureId) {
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
            }
        }

    }

}