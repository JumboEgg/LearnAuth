package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.second_project.databinding.FragmentRegisterPaymentBinding

class RegisterPaymentFragment: Fragment() {

    private var _binding: FragmentRegisterPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnToSubLecture.setOnClickListener {
            (parentFragment as? RegisterMainFragment)?.moveToStep(3)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}