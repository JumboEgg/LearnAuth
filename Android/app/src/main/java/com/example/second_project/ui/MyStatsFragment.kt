package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.second_project.databinding.FragmentMyStatsBinding
import com.example.second_project.viewmodel.MyStatsViewModel

class MyStatsFragment : Fragment() {

    private var _binding: FragmentMyStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MyStatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lectureName = arguments?.getString("lectureName")
        viewModel = ViewModelProvider(this)[MyStatsViewModel::class.java]
        viewModel.initData(lectureName)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.myShareRate.observe(viewLifecycleOwner, Observer { binding.tvMyShareRate.text = it })
        viewModel.myTotalIncome.observe(viewLifecycleOwner, Observer { binding.tvMyTotalIncome.text = it })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
