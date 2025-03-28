package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.second_project.databinding.FragmentDateStatsBinding
import com.example.second_project.viewmodel.DateStatsViewModel

class DateStatsFragment : Fragment() {

    private var _binding: FragmentDateStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DateStatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lectureName = arguments?.getString("lectureName")
        viewModel = ViewModelProvider(this)[DateStatsViewModel::class.java]
        viewModel.initData(lectureName)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDateStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // LiveData observe하여 UI 업데이트
        viewModel.dateSelection.observe(viewLifecycleOwner, Observer { binding.tvDateSelection.text = it })
        viewModel.studentCount.observe(viewLifecycleOwner, Observer { binding.tvStudentCount.text = it })
        viewModel.income.observe(viewLifecycleOwner, Observer { binding.tvIncome.text = it })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
