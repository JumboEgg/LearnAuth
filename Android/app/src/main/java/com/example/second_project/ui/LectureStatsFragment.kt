package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.second_project.databinding.FragmentLectureStatsBinding
import com.example.second_project.viewmodel.LectureStatsViewModel

class LectureStatsFragment : Fragment() {

    private var _binding: FragmentLectureStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LectureStatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lectureName = arguments?.getString("lectureName")
        viewModel = ViewModelProvider(this)[LectureStatsViewModel::class.java]
        viewModel.initData(lectureName)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLectureStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.lectureNameLive.observe(viewLifecycleOwner, Observer { binding.tvLectureName.text = it })
        viewModel.registerDate.observe(viewLifecycleOwner, Observer { binding.tvRegisterDate.text = it })
        viewModel.lecturer.observe(viewLifecycleOwner, Observer { binding.tvLecturer.text = it })
        viewModel.totalStudentCount.observe(viewLifecycleOwner, Observer { binding.tvTotalStudentCount.text = it })
        viewModel.totalIncome.observe(viewLifecycleOwner, Observer { binding.tvTotalIncome.text = it })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
