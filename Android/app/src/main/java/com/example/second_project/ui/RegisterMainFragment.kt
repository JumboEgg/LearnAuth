package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.second_project.R
import com.example.second_project.databinding.FragmentMainBinding
import com.example.second_project.databinding.FragmentRegisterMainBinding
import com.example.second_project.interfaces.RegisterStepSavable
import com.example.second_project.utils.LoadingOverlayHelper
import com.example.second_project.viewmodel.MainViewModel
import com.example.second_project.viewmodel.RegisterViewModel

class RegisterMainFragment: Fragment() {

    private lateinit var loadingHelperMain: LoadingOverlayHelper

    private var _binding: FragmentRegisterMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by activityViewModels()

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

        loadingHelperMain = LoadingOverlayHelper(
            activity = requireActivity(),
            overlayView = binding.loadingOverlay,
            catView = binding.catImageView
        )

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
        view?.clearFocus()

        val currentFragment = childFragmentManager.findFragmentById(binding.registerFragmentContainer.id)
        val shouldProceed = (currentFragment as? RegisterStepSavable)?.saveDataToViewModel() ?: true

        if (!shouldProceed) {
            // ❌ 저장 실패 시 전환하지 않음
            return
        }

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

        viewModel.reset()

    }

    fun showGlobalLoading() {
        Log.d("LoadingOverlayHelper", "Main: showGlobalLoading() 호출됨")
        loadingHelperMain.show()
    }

    fun hideGlobalLoading() {
        Log.d("LoadingOverlayHelper", "Main: hideGlobalLoading() 호출됨")
        loadingHelperMain.hide()
    }


}