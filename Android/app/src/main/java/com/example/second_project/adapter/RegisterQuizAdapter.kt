package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.databinding.ItemRegisterQuizDetailBinding

class RegisterQuizAdapter : RecyclerView.Adapter<RegisterQuizAdapter.ViewHolder>() {

    private val isExpandedList = mutableListOf<Boolean>()
    private var selectedAnswerIndexList = mutableListOf<Int>()  // 각 퀴즈별로 선택된 정답 인덱스 (0~2)

    inner class ViewHolder(private val binding: ItemRegisterQuizDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            // 퀴즈 제목: 퀴즈 1, 퀴즈 2, ...
            binding.textQuizIndex.text = "퀴즈 ${position + 1}"

            // 펼침/접힘 상태 설정
            binding.linearToggleArea.visibility = if (isExpandedList[position]) View.VISIBLE else View.GONE

            // 토글 아이콘 변경
            val iconRes = if (isExpandedList[position]) R.drawable.keyboard_arrow_up_24px
            else R.drawable.keyboard_arrow_down_24px
            binding.btnToggleSubLecture.setImageResource(iconRes)

            // 토글 버튼 클릭
            binding.btnToggleSubLecture.setOnClickListener {
                isExpandedList[position] = !isExpandedList[position]
                notifyItemChanged(position)
            }

            // 정답 선택 로직: 항상 하나만 체크되게 설정
            val answerViews = listOf(binding.isAnswer1, binding.isAnswer2, binding.isAnswer3)

            answerViews.forEachIndexed { index, container ->
                val imageView = container.getChildAt(0) as? ImageView
                val isChecked = selectedAnswerIndexList[position] == index
                imageView?.setImageResource(
                    if (isChecked) R.drawable.ic_process_checked else R.drawable.ic_process_unchecked
                )

                container.setOnClickListener {
                    selectedAnswerIndexList[position] = index
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

    override fun getItemCount(): Int = isExpandedList.size

    fun addQuiz() {
        isExpandedList.add(true)
        selectedAnswerIndexList.add(-1)  // 아직 정답 선택 안함
        notifyItemInserted(isExpandedList.size - 1)
    }

    fun addInitialQuizzes(count: Int) {
        repeat(count) {
            addQuiz()
        }
    }

    fun removeAll() {
        isExpandedList.clear()
        selectedAnswerIndexList.clear()
        notifyDataSetChanged()
    }
}