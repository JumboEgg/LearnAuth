package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.second_project.databinding.DialogCertConfirmBinding
import com.example.second_project.databinding.FragmentCertDetailBinding

class CertDetailFragment: Fragment() {
    private var _binding: FragmentCertDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCertDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCertSave.setOnClickListener {
            showConfirmDialog()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showConfirmDialog() {
        val dialogBinding = DialogCertConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogBinding.root).create()

        dialog.show()

        dialogBinding.btnCloseCert.setOnClickListener{ dialog.dismiss()}
    }

}