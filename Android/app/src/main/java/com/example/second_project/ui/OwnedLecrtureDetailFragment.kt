package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.R
import com.example.second_project.adapter.LectureItem
import com.example.second_project.adapter.OwnedLectureAdapter
import com.example.second_project.databinding.FragmentOwnedLectureDetailBinding

class OwnedLecrtureDetailFragment : Fragment() {

    private var _binding: FragmentOwnedLectureDetailBinding? = null
    private val binding get() = _binding!!

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

        // RecyclerView 설정
        val lectureList = listOf(
            LectureItem("1강", "Kotlin 기초", R.drawable.sample_plzdelete),
            LectureItem("2강", "Android Studio 활용", R.drawable.sample_plzdelete),
            LectureItem("3강", "Jetpack Compose 소개", R.drawable.sample_plzdelete),
            LectureItem("4강", "RecyclerView 사용법", R.drawable.sample_plzdelete),
            LectureItem("5강", "Coroutine 기초", R.drawable.sample_plzdelete)
        )

        val adapter = OwnedLectureAdapter(lectureList)
        binding.myLectureDetailList.adapter = adapter
        binding.myLectureDetailList.layoutManager = object : LinearLayoutManager(requireContext()) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }


    }

}