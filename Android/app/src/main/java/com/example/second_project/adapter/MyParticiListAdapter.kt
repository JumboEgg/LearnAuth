package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.data.MyParticiItem
import com.example.second_project.databinding.ItemParticipatedLectureBinding

class MyParticiListAdapter : ListAdapter<MyParticiItem, MyParticiListAdapter.ParticipatedLectureViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MyParticiItem>() {
            override fun areItemsTheSame(oldItem: MyParticiItem, newItem: MyParticiItem): Boolean {
                // lectureNumber를 아이템의 고유 식별자로 사용 (필요 시 다른 식별자 사용)
                return oldItem.lectureNumber == newItem.lectureNumber
            }

            override fun areContentsTheSame(oldItem: MyParticiItem, newItem: MyParticiItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ParticipatedLectureViewHolder(private val binding: ItemParticipatedLectureBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MyParticiItem) {
            // 카테고리는 예시로 고정(“법률”)이라 가정
            binding.category.text = item.subject

            // 강의 제목
            binding.lectureTitle.text = item.title

            // boolean 타입으로 바뀌어야함. 만약
            if(item.mainHuman){
                binding.joinText.text = "강의자로 참여"
            }else{
                binding.joinText.text = ""
            }
            // 닉네임
            binding.myName.text = item.myName

            // 이미지 설정(고양이 이미지).
            // staricon을 기본으로 사용 중이니, 필요에 따라 바꾸거나 Glide/Picasso 등으로 URL 로드 가능

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipatedLectureViewHolder {
        val binding = ItemParticipatedLectureBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ParticipatedLectureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipatedLectureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
