package com.example.second_project.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.data.model.dto.response.SubLecture
import com.example.second_project.databinding.ItemSubLectureBinding

private const val TAG = "LectureDetailAdapter_야옹"
class LectureDetailAdapter(private val subLectureList: List<SubLecture>) :
    RecyclerView.Adapter<LectureDetailAdapter.LectureViewHolder>() {

    inner class LectureViewHolder(private val binding: ItemSubLectureBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(subLecture: SubLecture, position: Int) {
            Log.d(TAG, "bind: ${subLecture.subLectureTitle}")
            binding.sublectureCount.text = "${position +1}강"
            binding.subLectureTitle.text = subLecture.subLectureTitle

            // item_sub_lecture.xml 높이 조정
            binding.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectureViewHolder {
        val binding = ItemSubLectureBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return LectureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LectureViewHolder, position: Int) {
        holder.bind(subLectureList[position], position)
    }

    override fun getItemCount(): Int = subLectureList.size
}
