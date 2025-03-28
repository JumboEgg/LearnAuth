package com.example.second_project.ui

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.second_project.databinding.FragmentRegisterUploadBinding

class RegisterUploadFragment: Fragment() {

    private var _binding: FragmentRegisterUploadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnToPaymentInfo.setOnClickListener {
            (parentFragment as? RegisterMainFragment)?.moveToStep(2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

}