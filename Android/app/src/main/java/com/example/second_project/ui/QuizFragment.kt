package com.example.second_project.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import com.example.second_project.R
import com.example.second_project.data.QuizQuestion
import com.example.second_project.databinding.DialogQuizResultBinding
import com.example.second_project.databinding.FragmentQuizBinding

class QuizFragment : Fragment() {
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    // navArgs로 lectureId, lectureTitle을 함께 받음
    private val args: QuizFragmentArgs by navArgs()
    private val lectureId by lazy { args.lectureId }
    private val lectureTitle by lazy { args.lectureTitle }

    private var timer: CountDownTimer? = null
    private val timerDuration = 30000L // 30초

    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var userHasAnswered = false

    private var selectedOption: Int? =null

    private val questions = listOf(
        QuizQuestion("1번 문제", "눈을 뜰 때 적절한 눈동자의 각도는?", "37도", "79도", "183도"),
        QuizQuestion("2번 문제", "두 번째 문제 내용은 무엇일까요?", "옵션 A", "옵션 B", "옵션 C"),
        QuizQuestion("3번 문제", "세 번째 문제의 정답은?", "옵션 X", "옵션 Y", "옵션 Z")
    )

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

        showQuestion()
        startTimer()

        // 옵션 클릭 리스너
        binding.option1.setOnClickListener {
            if (selectedOption == null) {
                selectedOption = 1
                binding.optionsGroup.check(binding.option1.id)
            }
        }
        binding.option2.setOnClickListener {
            if (selectedOption == null) {
                selectedOption = 2
                binding.optionsGroup.check(binding.option2.id)
            }
        }
        binding.option3.setOnClickListener {
            if (selectedOption == null) {
                selectedOption = 3
                binding.optionsGroup.check(binding.option3.id)
            }
        }

        binding.nextButton.setOnClickListener {
            if (selectedOption == null) {
                Toast.makeText(requireContext(), "답을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userHasAnswered = true
            if (selectedOption == 1) {
                correctAnswers++
            }
            timer?.cancel()
            moveToNextOrResult()

        }
    }

    private fun moveToNextOrResult() {
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            userHasAnswered = false
            showQuestion()
            startTimer()
        } else {
            showResultDialog()
        }
    }

    private fun showQuestion() {
        val question = questions[currentQuestionIndex]
        binding.problemTitle.text = question.title
        binding.problemContent.text = question.content
        binding.option1.text = question.option1
        binding.option2.text = question.option2
        binding.option3.text = question.option3

        resetOptionSelection()

        binding.timerText.text = "30"
        binding.progressBar.max = 30
        binding.progressBar.progress = 30
    }

    private fun resetOptionSelection() {
        binding.optionsGroup.clearCheck()
        selectedOption = null
    }


    private fun startTimer() {

        timer = object : CountDownTimer(timerDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.timerText.text = "$secondsRemaining"
                binding.progressBar.progress = secondsRemaining.toInt()
            }

            override fun onFinish() {
                binding.timerText.text = "0"
                binding.progressBar.progress = 0
                // 아직 답변하지 않았다면 시간초과 dialog 표시
                if (!userHasAnswered) {
                    if (selectedOption != null) {
                        userHasAnswered = true
                        if (selectedOption == 1) {
                            correctAnswers++
                        }
                        if (currentQuestionIndex == questions.size -1){
                            showResultDialog()
                        }else {
                            moveToNextOrResult()
                        }
                    } else {
                        AlertDialog.Builder(requireContext())
                            .setTitle("시간초과입니다!")
                            .setMessage("시간 내에 답을 선택하지 않으셨습니다.")
                            .setPositiveButton("확인") { dialog, _ ->
                                dialog.dismiss()
                                moveToNextOrResult()
                            }
                            .show()
                    }
                }
            }
        }.start()
    }

    private fun showResultDialog() {
        // 1) 정답 여부에 따른 메시지와 아이콘 결정
        val isPass = (correctAnswers >= 2)
        val resultMessage = if (isPass) {
            "${questions.size}문제 중 ${correctAnswers}문제 통과\n축하합니다!"
        } else {
            "${questions.size}문제 중 ${correctAnswers}문제 통과\n다시 시도하세요!"
        }
        val resultIcon = if (isPass) R.drawable.pass_check else R.drawable.fail_check

        val dialogBinding = DialogQuizResultBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)

        dialogBinding.dialogTitle.text = "${lectureTitle}" // 예: lectureTitle 사용 가능
        dialogBinding.dialogImage.setImageResource(resultIcon)
        dialogBinding.dialogMessage.text = resultMessage

        val dialog = builder.create()
        dialogBinding.dialogButton.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(
                R.id.nav_profile,
                null,
                navOptions {
                    popUpTo(R.id.nav_graph) { inclusive = true }
                }
            )
        }

        // 5) 실제로 다이얼로그 표시
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}
