package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.data.model.dto.response.ParticipatedLecture
import com.example.second_project.databinding.ItemParticipatedLectureBinding

class ParticipatedLectureAdapter(
    private val onItemClick: ((ParticipatedLecture) -> Unit)? = null
) : ListAdapter<ParticipatedLecture, ParticipatedLectureAdapter.ParticipatedLectureViewHolder>(
    DIFF_CALLBACK
) {

    inner class ParticipatedLectureViewHolder(
        private val binding: ItemParticipatedLectureBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ParticipatedLecture) {
            // 강의 제목
            binding.lectureTitle.text = item.title
            // 카테고리
            binding.category.text = item.categoryName
            // 강의 참여 타입 (예시: 강의자로 참여 또는 수강자로 참여)
            binding.joinText.text = if (item.isLecturer) "강의자로 참여" else " "
            // 닉네임 또는 강의자 이름 (필요에 따라 수정)
            binding.myName.text = item.lecturer

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipatedLectureViewHolder {
        val binding = ItemParticipatedLectureBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParticipatedLectureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipatedLectureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ParticipatedLecture>() {
            override fun areItemsTheSame(oldItem: ParticipatedLecture, newItem: ParticipatedLecture): Boolean =
                oldItem.lectureId == newItem.lectureId

            override fun areContentsTheSame(oldItem: ParticipatedLecture, newItem: ParticipatedLecture): Boolean =
                oldItem == newItem
        }
    }
}
