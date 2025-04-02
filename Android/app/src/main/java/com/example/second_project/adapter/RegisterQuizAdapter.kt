package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.data.model.dto.RegisterTempQuiz
import com.example.second_project.databinding.ItemRegisterQuizDetailBinding

class RegisterQuizAdapter : RecyclerView.Adapter<RegisterQuizAdapter.ViewHolder>() {

    private val isExpandedList = mutableListOf<Boolean>()
    private val quizList = mutableListOf<RegisterTempQuiz>()

    inner class ViewHolder(private val binding: ItemRegisterQuizDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val quiz = quizList[position]

            // 퀴즈 제목
            binding.textQuizIndex.text = "퀴즈 ${position + 1}"

            // 펼침/접힘
            binding.linearToggleArea.visibility = if (isExpandedList[position]) View.VISIBLE else View.GONE
            val iconRes = if (isExpandedList[position]) R.drawable.keyboard_arrow_up_24px
            else R.drawable.keyboard_arrow_down_24px
            binding.btnToggleSubLecture.setImageResource(iconRes)

            binding.btnToggleSubLecture.setOnClickListener {
                isExpandedList[position] = !isExpandedList[position]
                notifyItemChanged(position)
            }

            // 문제 내용
            binding.editTextTitle.editText?.setText(quiz.question)
            binding.editTextTitle.editText?.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    quiz.question = binding.editTextTitle.editText?.text.toString()
                }
            }

            // 선택지 1
            binding.editAnswer1.editText?.setText(quiz.options[0])
            binding.editAnswer1.editText?.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    quiz.options[0] = binding.editAnswer1.editText?.text.toString()
                }
            }

            // 선택지 2
            binding.editAnswer2.editText?.setText(quiz.options[1])
            binding.editAnswer2.editText?.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    quiz.options[1] = binding.editAnswer2.editText?.text.toString()
                }
            }

            // 선택지 3
            binding.editAnswer3.editText?.setText(quiz.options[2])
            binding.editAnswer3.editText?.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    quiz.options[2] = binding.editAnswer3.editText?.text.toString()
                }
            }

            // 정답 선택: 1개만 체크되도록
            val answerViews = listOf(binding.isAnswer1, binding.isAnswer2, binding.isAnswer3)

            answerViews.forEachIndexed { index, container ->
                val imageView = container.getChildAt(0) as? ImageView
                val isChecked = quiz.correctAnswerIndex == index
                imageView?.setImageResource(
                    if (isChecked) R.drawable.ic_process_checked else R.drawable.ic_process_unchecked
                )

                container.setOnClickListener {
                    quiz.correctAnswerIndex = index
                    notifyItemChanged(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegisterQuizDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = quizList.size

    fun addQuiz() {
        quizList.add(RegisterTempQuiz())
        isExpandedList.add(true)
        notifyItemInserted(quizList.size - 1)
    }

    fun addInitialQuizzes(count: Int) {
        repeat(count) { addQuiz() }
    }

    fun setItems(tempList: List<RegisterTempQuiz>) {
        quizList.clear()
        quizList.addAll(tempList)
        isExpandedList.clear()
        isExpandedList.addAll(List(tempList.size) { true })
        notifyDataSetChanged()
    }

    fun getItems(): List<RegisterTempQuiz> = quizList

    fun removeAll() {
        quizList.clear()
        isExpandedList.clear()
        notifyDataSetChanged()
    }
}
