package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.data.model.dto.request.Lecture
import com.example.second_project.databinding.ItemSearchLectureBinding

class SearchLectureAdapter(
    private val onItemClick: (lectureId: Int, lectureTitle: String) -> Unit
) : ListAdapter<Lecture, SearchLectureAdapter.SearchLectureViewHolder>(DIFF_CALLBACK) {

    inner class SearchLectureViewHolder(private val binding: ItemSearchLectureBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(lecture: Lecture) {
            // 강의 제목
            binding.lectureTitle.text = lecture.title
            // 카테고리 (XML에서 TextView에 id="lectureCategory" 추가 권장)
            binding.lectureCategory.text = lecture.categoryName
            // 강의자 (null인 경우 대체 텍스트)
            binding.lectureTeacherName.text = lecture.lecturer ?: "강의자 미정"
            // 강의 가격
            binding.lecturePrice.text = "${lecture.price}원"
            // 이미지의 경우 XML에서 기본 샘플 이미지(@drawable/sample_plzdelete)가 지정되어 있으므로,
            // 별도의 이미지 로딩 라이브러리 사용 시 여기서 lecture의 이미지 URL을 처리하면 됨.
            binding.root.setOnClickListener {
                onItemClick(lecture.lectureId, lecture.title)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchLectureViewHolder {
        val binding = ItemSearchLectureBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SearchLectureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchLectureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Lecture>() {
            override fun areItemsTheSame(oldItem: Lecture, newItem: Lecture): Boolean =
                oldItem.lectureId == newItem.lectureId

            override fun areContentsTheSame(oldItem: Lecture, newItem: Lecture): Boolean =
                oldItem == newItem
        }
    }
}
