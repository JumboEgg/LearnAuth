package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.second_project.databinding.FragmentParticipantsStatsBinding
import com.example.second_project.viewmodel.ParticipantsStatsViewModel

class ParticipantsStatsFragment : Fragment() {

    private var _binding: FragmentParticipantsStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ParticipantsStatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lectureName = arguments?.getString("lectureName")
        viewModel = ViewModelProvider(this)[ParticipantsStatsViewModel::class.java]
        viewModel.initData(lectureName)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentParticipantsStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.participantRate1.observe(viewLifecycleOwner, Observer { binding.tvParticipantRate1.text = it })
        viewModel.participantRate2.observe(viewLifecycleOwner, Observer { binding.tvParticipantRate2.text = it })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
