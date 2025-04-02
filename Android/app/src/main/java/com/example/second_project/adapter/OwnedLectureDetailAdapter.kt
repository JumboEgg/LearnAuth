package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.data.model.dto.response.SubLecture
import com.example.second_project.databinding.ItemOwnedSublectureBinding
import com.example.second_project.databinding.ItemSubLectureBinding

class OwnedLectureDetailAdapter(private val subLectureList: List<SubLecture>) :
    RecyclerView.Adapter<OwnedLectureDetailAdapter.LectureViewHolder>() {

    inner class LectureViewHolder(private val binding: ItemOwnedSublectureBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(subLecture: SubLecture, position: Int) {
            binding.eachNum.text = "${position +1}강"
            binding.eachTitle.text = subLecture.subLectureTitle
            binding.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

            if(subLecture.endFlag == false && subLecture.continueWatching == "00:00:00") {
                binding.eachWatchBtn.text = "수강하기"
            } else if (subLecture.endFlag == false && subLecture.continueWatching != "00:00:00") {
                binding.eachWatchBtn.text = "이어보기"
            } else if (subLecture.endFlag == true) {
                binding.eachWatchBtn.text = "다시보기"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectureViewHolder {
        val binding = ItemOwnedSublectureBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LectureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LectureViewHolder, position: Int) {
        holder.bind(subLectureList[position], position)
    }

    override fun getItemCount(): Int = subLectureList.size
}