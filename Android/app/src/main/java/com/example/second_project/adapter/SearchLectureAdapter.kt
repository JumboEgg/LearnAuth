package com.example.second_project.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.second_project.R
import com.example.second_project.UserSession.userId
import com.example.second_project.data.model.dto.request.Lecture
import com.example.second_project.databinding.ItemSearchLectureBinding
import com.example.second_project.utils.YoutubeUtil

private const val TAG = "SearchLectureAdapter_야옹"
class SearchLectureAdapter(
    private val onItemClick: (lectureId: Int, userId: Int) -> Unit
) : ListAdapter<Lecture, SearchLectureAdapter.SearchLectureViewHolder>(DIFF_CALLBACK) {

    inner class SearchLectureViewHolder(private val binding: ItemSearchLectureBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(lecture: Lecture) {
            // 강의 제목
            binding.lectureTitle.text = lecture.title
            binding.lectureTitle.isSelected = true
            
            // 카테고리 (XML에서 TextView에 id="lectureCategory" 추가 권장)
            binding.lectureCategory.text = lecture.categoryName
            // 강의자 (null인 경우 대체 텍스트)
            binding.lectureTeacherName.text = lecture.lecturer ?: "강의자 미정"
            // 강의 가격
            binding.lecturePrice.text = "${lecture.price}원"

            // 썸네일 설정
            Log.d(TAG, "bind: lecture = $lecture")
            
            lecture.lectureUrl?.let { videoId ->
                Log.d(TAG, "bind: videoId = $videoId")
                val thumbnailUrl = YoutubeUtil.getThumbnailUrl(videoId, YoutubeUtil.ThumbnailQuality.MEDIUM)
                Log.d(TAG, "bind: thumbnailUrl = $thumbnailUrl")
                Glide.with(binding.root.context)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.white)
                    .into(binding.lectureImg)
            } ?: run {
                Log.d(TAG, "bind: lectureUrl이 null임")
            }

            binding.root.setOnClickListener {
                onItemClick(lecture.lectureId, userId)
                Log.d(TAG, "bind: ${lecture.lectureId}, ${lecture.title}")
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
