package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.second_project.R
import com.example.second_project.databinding.FragmentMainBinding
import com.example.second_project.databinding.FragmentRegisterMainBinding
import com.example.second_project.viewmodel.MainViewModel
import com.example.second_project.viewmodel.RegisterViewModel

class RegisterMainFragment: Fragment() {

    private var _binding: FragmentRegisterMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    private val stepIndicators by lazy {
        listOf(
            binding.step1, binding.step2, binding.step3, binding.step4, binding.step5
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStop.setOnClickListener {
            findNavController().popBackStack()
        }

        // 첫 진입 시 첫 번째 단계로 이동
        moveToStep(0)

        // 인디케이터 클릭으로 단계 전환
        stepIndicators.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                moveToStep(index)
            }
        }

    }

    fun moveToStep(index: Int) {
        val fragment = when (index) {
            0 -> RegisterLectureFragment()
            1 -> RegisterUploadFragment()
            2 -> RegisterPaymentFragment()
            3 -> RegisterSublectureFragment()
            4 -> RegisterQuizFragment()
            else -> return
        }

        // 내부 프래그먼트 교체
        childFragmentManager.beginTransaction()
            .replace(binding.registerFragmentContainer.id, fragment)
            .commit()

        // 동그라미 상태 업데이트
        stepIndicators.forEachIndexed { i, imageView ->
            imageView.setImageResource(
                if (i == index) R.drawable.ic_process_checked else R.drawable.ic_process_unchecked
            )
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

}