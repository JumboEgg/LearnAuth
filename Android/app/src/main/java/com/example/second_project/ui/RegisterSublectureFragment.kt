package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.second_project.databinding.FragmentRegisterSublectureBinding

class RegisterSublectureFragment: Fragment() {

    private var _binding: FragmentRegisterSublectureBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterSublectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnToQuiz.setOnClickListener {
            (parentFragment as? RegisterMainFragment)?.moveToStep(4)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}