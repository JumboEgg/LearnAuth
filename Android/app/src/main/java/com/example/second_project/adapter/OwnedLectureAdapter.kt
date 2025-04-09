package com.example.second_project.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.second_project.R
import com.example.second_project.data.model.dto.response.OwnedLecture
import com.example.second_project.databinding.IteLectureBinding
import com.example.second_project.utils.YoutubeUtil

private const val TAG = "OwnedLectureAdapter_야옹"

class OwnedLectureAdapter(
    private val onItemClick: ((OwnedLecture) -> Unit)? = null
) : ListAdapter<OwnedLecture, OwnedLectureAdapter.OwnedLectureViewHolder>(DiffCallback()) {

    inner class OwnedLectureViewHolder(
        private val binding: IteLectureBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OwnedLecture) {
            // "과목"은 categoryName로 표시합니다.
            binding.subject.text = item.categoryName
            // 강의 제목
            binding.lectureTitle.text = item.title
            // 학습률: learningRate가 Double이라면, 백분율로 변환하여 표시
            val percentage = String.format("%.0f", item.learningRate * 100)
//            binding.progressBar.text = "학습률 ${percentage}%"
            // 이어보기 버튼: isLecturer에 따라 텍스트를 다르게 설정합니다.
            binding.lectureButton.text = if (item.recentId != null) "이어서 보기" else "수강하기"
            Log.d(TAG, "bind: 야옹야옹 ${item.title}, ${item.recentId}")
            
            // 썸네일 로딩
            item.lectureUrl?.let { videoId ->
                Log.d(TAG, "bind: videoId = $videoId")
                val thumbnailUrl = YoutubeUtil.getThumbnailUrl(videoId, YoutubeUtil.ThumbnailQuality.MEDIUM)
                
                Glide.with(binding.root.context)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.white)
                    .into(binding.lectureImage)
            } ?: run {
                Log.d(TAG, "bind: lectureUrl이 null임")
            }
            
            // 아이템 클릭 이벤트: OwnedLecture 객체 전체를 전달
            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnedLectureViewHolder {
        val binding = IteLectureBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OwnedLectureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OwnedLectureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<OwnedLecture>() {
        override fun areItemsTheSame(oldItem: OwnedLecture, newItem: OwnedLecture): Boolean {
            return oldItem.lectureId == newItem.lectureId
        }

        override fun areContentsTheSame(oldItem: OwnedLecture, newItem: OwnedLecture): Boolean {
            return oldItem == newItem
        }
    }
}
