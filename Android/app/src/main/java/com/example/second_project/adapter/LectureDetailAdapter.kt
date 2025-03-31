package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.data.model.dto.response.SubLecture
import com.example.second_project.databinding.ItemSubLectureBinding

class LectureDetailAdapter(private val subLectureList: List<SubLecture>) :
    RecyclerView.Adapter<LectureDetailAdapter.LectureViewHolder>() {

    inner class LectureViewHolder(private val binding: ItemSubLectureBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(subLecture: SubLecture) {
            binding.sublectureCount.text = "${position +1}강"
            binding.subLectureTitle.text = subLecture.subLectureTitle
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectureViewHolder {
        val binding = ItemSubLectureBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LectureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LectureViewHolder, position: Int) {
        holder.bind(subLectureList[position])
    }

    override fun getItemCount(): Int = subLectureList.size
}
