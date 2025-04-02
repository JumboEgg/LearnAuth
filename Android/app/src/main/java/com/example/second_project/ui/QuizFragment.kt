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
import com.example.second_project.data.model.dto.response.QuizData
import com.example.second_project.data.model.dto.response.QuizResponse
import com.example.second_project.databinding.DialogQuizResultBinding
import com.example.second_project.databinding.FragmentQuizBinding
import com.example.second_project.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuizFragment : Fragment() {
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    // navArgs로 lectureId, userId를 함께 받음
    private val args: QuizFragmentArgs by navArgs()
    private val lectureId by lazy { args.lectureId }
    private val userId by lazy { args.userId }

    private var timer: CountDownTimer? = null
    private val timerDuration = 30000L // 30초

    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var userHasAnswered = false
    private var selectedOption: Int? = null
    private var quizDataList: List<QuizData> = emptyList()

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

        // 뒤로가기 버튼 설정
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 퀴즈 데이터 가져오기
        fetchQuizData()

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
            checkAnswer()
            timer?.cancel()
            moveToNextOrResult()
        }
    }

    private fun fetchQuizData() {
        ApiClient.quizService.getQuiz(lectureId).enqueue(object : Callback<QuizResponse> {
            override fun onResponse(call: Call<QuizResponse>, response: Response<QuizResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { quizResponse ->
                        quizDataList = quizResponse.data
                        if (quizDataList.isNotEmpty()) {
                            showQuestion()
                            startTimer()
                        } else {
                            Toast.makeText(requireContext(), "퀴즈 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "퀴즈 데이터를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }

            override fun onFailure(call: Call<QuizResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        })
    }

    private fun showQuestion() {
        if (currentQuestionIndex >= quizDataList.size) return

        val quizData = quizDataList[currentQuestionIndex]
        binding.quizNum.text = "${currentQuestionIndex + 1}번 문제"
        binding.problemContent.text = quizData.question

        // 옵션 설정 (최대 3개까지만 표시)
        quizData.quizOptions.take(3).forEachIndexed { index, option ->
            when (index) {
                0 -> {
                    binding.option1.text = option.quizOption
                    binding.option1.visibility = View.VISIBLE
                }
                1 -> {
                    binding.option2.text = option.quizOption
                    binding.option2.visibility = View.VISIBLE
                }
                2 -> {
                    binding.option3.text = option.quizOption
                    binding.option3.visibility = View.VISIBLE
                }
            }
        }

        // 3개 미만인 경우 나머지 옵션 숨기기
        when (quizData.quizOptions.size) {
            1 -> {
                binding.option2.visibility = View.GONE
                binding.option3.visibility = View.GONE
            }
            2 -> {
                binding.option3.visibility = View.GONE
            }
        }

        resetOptionSelection()
        binding.timerText.text = "30"
        binding.progressBar.max = 30
        binding.progressBar.progress = 30
    }

    private fun resetOptionSelection() {
        binding.optionsGroup.clearCheck()
        selectedOption = null
    }

    private fun checkAnswer() {
        if (currentQuestionIndex >= quizDataList.size) return

        val quizData = quizDataList[currentQuestionIndex]
        val selectedOptionIndex = selectedOption?.minus(1) ?: return
        val selectedQuizOption = quizData.quizOptions.getOrNull(selectedOptionIndex)

        if (selectedQuizOption?.isCorrect == 1) {  // 1이 정답
            correctAnswers++
        }
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
                        if (currentQuestionIndex == quizDataList.size -1){
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

    private fun moveToNextOrResult() {
        if (currentQuestionIndex < quizDataList.size - 1) {
            currentQuestionIndex++
            userHasAnswered = false
            showQuestion()
            startTimer()
        } else {
            showResultDialog()
        }
    }

    private fun showResultDialog() {
        // 1) 정답 여부에 따른 메시지와 아이콘 결정
        val isPass = (correctAnswers >= quizDataList.size * 0.7)  // 70% 이상 맞으면 통과
        val resultMessage = if (isPass) {
            "${quizDataList.size}문제 중 ${correctAnswers}문제 통과\n축하합니다!"
        } else {
            "${quizDataList.size}문제 중 ${correctAnswers}문제 통과\n다시 시도하세요!"
        }
        val resultIcon = if (isPass) R.drawable.pass_check else R.drawable.fail_check

        val dialogBinding = DialogQuizResultBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)

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
