package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.databinding.ItemRegisterSublectureDetailBinding

class RegisterSublectureAdapter (
    private val subLectureCount: () -> Int,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<RegisterSublectureAdapter.ViewHolder>(){

    private val isExpandedList = mutableListOf<Boolean>()

    inner class ViewHolder(private val binding: ItemRegisterSublectureDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            // 제목 업데이트 (ex. 개별 강의 1)
            binding.textSubLectureIndex.text = "개별 강의 ${position + 1}"

            // 초기 펼침 상태 설정
            binding.linearToggleArea.visibility = if (isExpandedList[position]) View.VISIBLE else View.GONE

            // 펼침/접힘 토글
            binding.btnToggleSubLecture.setOnClickListener {

                val expanded = !isExpandedList[position]
                isExpandedList[position] = expanded

                // 아이콘 변경
                val iconRes = if (expanded) {
                    R.drawable.keyboard_arrow_up_24px
                } else {
                    R.drawable.keyboard_arrow_down_24px
                }
                binding.btnToggleSubLecture.setImageResource(iconRes)

                notifyItemChanged(position)
            }

            // 삭제 버튼
            binding.btnDeleteSubLecture.setOnClickListener {
                isExpandedList.removeAt(position)
                onDeleteClick(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegisterSublectureDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = isExpandedList.size

    fun addItem() {
        isExpandedList.add(true)
        notifyItemInserted(isExpandedList.size - 1)
    }

    fun removeAll() {
        isExpandedList.clear()
        notifyDataSetChanged()
    }

}