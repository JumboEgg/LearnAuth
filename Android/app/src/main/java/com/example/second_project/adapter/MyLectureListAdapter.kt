package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.data.MyLectureItem
import com.example.second_project.databinding.IteLectureBinding

class MyLectureListAdapter(
    private val onItemClick: ((Int, String) -> Unit)? = null
) : ListAdapter<MyLectureItem, MyLectureListAdapter.MyLectureViewHolder>(MyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyLectureViewHolder {
        val binding = IteLectureBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyLectureViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MyLectureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MyLectureViewHolder(
        private val binding: IteLectureBinding,
        private val onItemClick: ((Int, String) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MyLectureItem) {
            // 과목
            binding.subject.text = item.subject

            // 강의 제목
            binding.lectureTitle.text = item.title

            // 학습률 (TextView이므로 문자열로 표시)
            // 예: "학습률 30%"
            binding.progressBar.text = "학습률 ${item.progress}%"

            // 이어보기 버튼
            binding.lectureButton.text = item.buttoText

            // 아이템 클릭 시 콜백
            binding.root.setOnClickListener {
                onItemClick?.invoke(item.lectureNumber, item.title)
            }
        }
    }

    private class MyDiffCallback : DiffUtil.ItemCallback<MyLectureItem>() {
        override fun areItemsTheSame(oldItem: MyLectureItem, newItem: MyLectureItem): Boolean {
            // 보통 id나 고유번호를 비교
            return oldItem.lectureNumber == newItem.lectureNumber
        }

        override fun areContentsTheSame(oldItem: MyLectureItem, newItem: MyLectureItem): Boolean {
            return oldItem == newItem
        }
    }
}
