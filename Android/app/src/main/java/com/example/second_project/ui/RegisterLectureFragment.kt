package com.example.second_project.ui

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.second_project.databinding.FragmentRegisterLectureBinding

class RegisterLectureFragment: Fragment() {

    private var _binding: FragmentRegisterLectureBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterLectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryList = listOf("체육", "데이터", "법률", "생명과학", "마케팅") // ← 이건 백엔드에서 받아온 값으로 바꿀 수 있음

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, categoryList)
        binding.autoCompleteCategory.setAdapter(adapter)

        // 선택 이벤트 (선택한 항목 로그 찍기 등)
        binding.autoCompleteCategory.setOnItemClickListener { _, _, position, _ ->
            val selected = categoryList[position]
            Log.d("CategorySelect", "선택된 카테고리: $selected")
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}