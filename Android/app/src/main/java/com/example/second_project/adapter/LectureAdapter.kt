package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.databinding.ItemLectureBinding

class LectureAdapter(private val onItemClick: ((Int) -> Unit)? = null) : RecyclerView.Adapter<LectureAdapter.LectureViewHolder>() {

    private val items = mutableListOf<Int>()  // 임시로 데이터는 Integer로 하드코딩

    // 데이터 세팅
    fun submitList(data: List<Int>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectureViewHolder {
        val binding = ItemLectureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LectureViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: LectureViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class LectureViewHolder(private val binding: ItemLectureBinding,
                            private val onItemClick: ((Int) -> Unit)?
    )                            : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Int) {
            // 예시로, 'item'을 그냥 아이템 번호로 표시 (추가적인 정보는 필요에 따라 수정)
            binding.lectureTitle.text = "강의 $item"

            binding.root.setOnClickListener{
                onItemClick?.invoke(item)
            }
        }
    }
}
