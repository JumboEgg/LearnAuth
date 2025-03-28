package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.second_project.adapter.TransactionAdapter
import com.example.second_project.databinding.FragmentChargeBinding
import com.example.second_project.databinding.FragmentMywalletBinding

class ChargeFragment : Fragment() {
    private var _binding: FragmentChargeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChargeBinding.inflate(inflater, container, false)
        return binding.root
    }
}