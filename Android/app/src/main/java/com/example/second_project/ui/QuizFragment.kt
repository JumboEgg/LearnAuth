package com.example.second_project.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.second_project.databinding.FragmentQuizBinding

class QuizFragment : Fragment() {
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val args: QuizFragmentArgs by navArgs()

    private var timer: CountDownTimer? = null
    private val timerDuration = 30000L // 30초

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lectureId = args.lectureId

        // 문제 설정 (예시)
        binding.problemTitle.text = "${lectureId}번 문제"
        binding.problemContent.text = "눈을 뜰 때 적절한 눈동자의 각도는?"

        // 옵션 설정 (예시)
        binding.option1.text = "37도"
        binding.option2.text = "79도"
        binding.option3.text = "183도"

        // 타이머 시작
        startTimer()

        // 버튼 클릭 이벤트 설정 (예시)
        binding.nextButton.setOnClickListener {
            // 다음 문제로 이동하는 로직 (필요시 구현)
        }
    }

    private fun startTimer() {
        binding.progressBar.max = 30
        binding.progressBar.progress = 30
        binding.timerText.text = "30"

        timer = object : CountDownTimer(timerDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.timerText.text = "$secondsRemaining"
                binding.progressBar.progress = secondsRemaining.toInt()
            }

            override fun onFinish() {
                binding.timerText.text = "0"
                binding.progressBar.progress = 0
                // 시간이 다 되었을 때 처리할 로직
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}