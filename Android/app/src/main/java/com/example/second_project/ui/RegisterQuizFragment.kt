package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.adapter.RegisterQuizAdapter
import com.example.second_project.data.model.dto.RegisterTempQuiz
import com.example.second_project.databinding.FragmentRegisterQuizBinding
import com.example.second_project.interfaces.RegisterStepSavable
import com.example.second_project.viewmodel.RegisterViewModel

class RegisterQuizFragment: Fragment(), RegisterStepSavable {

    private var _binding: FragmentRegisterQuizBinding? = null
    private val binding get() = _binding!!
    private lateinit var quizAdapter: RegisterQuizAdapter
    private val viewModel: RegisterViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 리사이클러뷰 세팅
        binding.recyclerQuiz.layoutManager = LinearLayoutManager(requireContext())
        quizAdapter = RegisterQuizAdapter()
        binding.recyclerQuiz.adapter = quizAdapter
        binding.recyclerQuiz.visibility = View.VISIBLE

        // ✅ 항상 3개는 유지, 작성한 내용 있으면 유지
        val tempList = viewModel.tempQuizzes.toMutableList()
        repeat(3 - tempList.size) {
            tempList.add(RegisterTempQuiz())
        }
        quizAdapter.setItems(tempList)


        binding.btnDone.setOnClickListener {
            viewModel.tempQuizzes.clear()
            viewModel.tempQuizzes.addAll(quizAdapter.getItems())

            // ✅ 최종 변환
            viewModel.convertTempToFinalSubLectures()
            viewModel.convertTempToFinalQuizzes()

            // ✅ 유효성 체크
            if (!viewModel.isValid()) {
                Toast.makeText(requireContext(), "모든 항목을 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 등록 요청
            viewModel.registerLecture(
                onSuccess = {
                    Toast.makeText(requireContext(), "강의가 성공적으로 등록되었습니다!", Toast.LENGTH_SHORT).show()
                    viewModel.reset()
                    requireActivity().supportFragmentManager.popBackStack()
                },
                onError = { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun saveDataToViewModel(): Boolean {
        viewModel.tempQuizzes.clear()
        viewModel.tempQuizzes.addAll(quizAdapter.getItems())
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}