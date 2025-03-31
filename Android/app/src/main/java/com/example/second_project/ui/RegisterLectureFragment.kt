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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.second_project.databinding.FragmentRegisterLectureBinding
import com.example.second_project.interfaces.RegisterStepSavable
import com.example.second_project.viewmodel.RegisterViewModel

class RegisterLectureFragment: Fragment(), RegisterStepSavable {

    private var _binding: FragmentRegisterLectureBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterLectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 카테고리 더미 데이터
        val categoryList = listOf("체육", "데이터", "법률", "생명과학", "마케팅")

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, categoryList)
        binding.autoCompleteCategory.setAdapter(adapter)

        binding.autoCompleteCategory.setOnItemClickListener { _, _, position, _ ->
            val selected = categoryList[position]
            Log.d("CategorySelect", "선택된 카테고리: $selected")
        }

        binding.btnToUploadFile.setOnClickListener {
            (parentFragment as? RegisterMainFragment)?.moveToStep(1)
        }

    }

    // 인터페이스 구현!
    override fun saveDataToViewModel() {
        viewModel.title = binding.editTextTitle.editText?.text.toString()
        viewModel.categoryName = binding.autoCompleteCategory.text.toString()
        viewModel.goal = binding.editTextGoal.editText?.text.toString()
        viewModel.description = binding.editTextContent.editText?.text.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}