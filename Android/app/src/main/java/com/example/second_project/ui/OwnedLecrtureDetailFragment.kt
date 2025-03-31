package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.R
import com.example.second_project.adapter.LectureDetailAdapter
import com.example.second_project.adapter.LectureItem
import com.example.second_project.adapter.OwnedLectureAdapter
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.databinding.FragmentOwnedLectureDetailBinding
import com.example.second_project.viewmodel.LectureDetailViewModel
import com.example.second_project.viewmodel.OwnedLectureDetailViewModel

private const val TAG = "OwnedLecrtureDetailFrag_야옹"
class OwnedLecrtureDetailFragment : Fragment() {

    private var _binding: FragmentOwnedLectureDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OwnedLectureDetailViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(OwnedLectureDetailViewModel::class.java)) {
                    return OwnedLectureDetailViewModel(LectureDetailRepository()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnedLectureDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lectureDetailName = binding.lectureDetailName
        lectureDetailName.isSelected = true

//        // RecyclerView 설정
//        val lectureList = listOf(
//            LectureItem("1강", "Kotlin 기초", R.drawable.sample_plzdelete),
//            LectureItem("2강", "Android Studio 활용", R.drawable.sample_plzdelete),
//            LectureItem("3강", "Jetpack Compose 소개", R.drawable.sample_plzdelete),
//            LectureItem("4강", "RecyclerView 사용법", R.drawable.sample_plzdelete),
//            LectureItem("5강", "Coroutine 기초", R.drawable.sample_plzdelete)
//        )
//
//        val adapter = OwnedLectureAdapter(lectureList)
//        binding.myLectureDetailList.adapter = adapter
//        binding.myLectureDetailList.layoutManager = object : LinearLayoutManager(requireContext()) {
//            override fun canScrollVertically(): Boolean {
//                return false
//            }
//        }

        val lectureId = arguments?.getInt("lectureId") ?: return
//        val lectureId = 2 //임시..!!!!
        val userId = 1 //임시 고정값, 수정 필요

        viewModel.fetchLectureDetail(lectureId, userId)
        binding.loadingProgressBar.visibility = View.VISIBLE


        viewModel.lectureDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                // 로딩이 끝났으면 ProgressBar 숨기기
                binding.loadingProgressBar.visibility = View.GONE

                Log.d(TAG, "onViewCreated: $it")
                binding.lectureDetailName.text = it.data.title
                binding.lectureDetailCategory.text = it.data.categoryName
                binding.lectureDetailTeacher.text = it.data.lecturer ?: "강의자 미정"
                binding.lectureDetailGoal.text = it.data.goal

                val subLectures = it.data.subLectures ?: emptyList()
                val adapter = LectureDetailAdapter(subLectureList = subLectures)
            }
        }


    }



}