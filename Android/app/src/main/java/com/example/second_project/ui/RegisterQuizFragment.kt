package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.adapter.RegisterQuizAdapter
import com.example.second_project.databinding.FragmentRegisterQuizBinding

class RegisterQuizFragment: Fragment() {

    private var _binding: FragmentRegisterQuizBinding? = null
    private val binding get() = _binding!!
    private lateinit var quizAdapter: RegisterQuizAdapter

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

        // 기본 퀴즈 3개 추가
        quizAdapter.addInitialQuizzes(3)

        // 완료 버튼 클릭
        binding.btnDone.setOnClickListener {
            requireActivity()
                .supportFragmentManager
                .popBackStack()
        }


        binding.btnDone.setOnClickListener {
            requireActivity()
                .supportFragmentManager
                .popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}