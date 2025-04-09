package com.example.second_project.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.second_project.R
import com.example.second_project.data.model.dto.request.Lecture
import com.example.second_project.databinding.ItemBannerBinding
import com.example.second_project.utils.YoutubeUtil

private const val TAG = "BannerAdapter_야옹"

class BannerAdapter(
    private val lectures: List<Lecture>,
    private val onItemClick: ((Lecture) -> Unit)? = null
) : ListAdapter<Lecture, BannerAdapter.BannerViewHolder>(BannerDiffCallback()) {

    // 기본 이미지 리소스 ID 리스트
    companion object {
        private val DEFAULT_BANNER_IMAGES = listOf(
            R.drawable.white,
            R.drawable.white,
            R.drawable.white
        )
    }

    // 실제 데이터 리스트 (빈 리스트인 경우 기본 이미지 사용)
    private val actualLectures: List<Lecture> = if (lectures.isEmpty()) {
        // 빈 리스트인 경우 더미 아이템 생성
        listOf(
            createDummyLecture(0),
            createDummyLecture(1),
            createDummyLecture(2)
        )
    } else {
        lectures
    }

    // 더미 강의 아이템 생성
    private fun createDummyLecture(index: Int): Lecture {
        return Lecture(
            lectureId = -1,
            title = "인기 강의",
            price = 0,
            lecturer = "러너스",
            lectureUrl = null,
            categoryName = "인기",
            subLectures = null
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(actualLectures[position])
    }

    override fun getItemCount(): Int = actualLectures.size

    inner class BannerViewHolder(private val binding: ItemBannerBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(lecture: Lecture) {
            // 강의 제목 설정
            binding.bannerTitle.text = lecture.title
            
            // 강의자 설정
            binding.bannerLecturer.text = lecture.lecturer ?: "강의자 정보 없음"
            Log.d(TAG, "강의자명: ${lecture.lecturer}")
            
            // 썸네일 설정
            if (lecture.lectureId == -1) {
                // 더미 아이템인 경우 기본 이미지 표시
                val imageIndex = actualLectures.indexOf(lecture) % DEFAULT_BANNER_IMAGES.size
                binding.bannerImage.setImageResource(DEFAULT_BANNER_IMAGES[imageIndex])
            } else {
                // 첫 번째 subLecture의 URL을 사용하거나, lectureUrl을 사용
                val videoId = if (!lecture.subLectures.isNullOrEmpty()) {
                    lecture.subLectures[0].lectureUrl
                } else {
                    lecture.lectureUrl
                }
                
                Log.d(TAG, "강의 ID: ${lecture.lectureId}")
                Log.d(TAG, "첫 번째 subLecture URL: ${lecture.subLectures?.firstOrNull()?.lectureUrl}")
                Log.d(TAG, "lectureUrl: ${lecture.lectureUrl}")
                Log.d(TAG, "videoId: $videoId")
                
                videoId?.let { id ->
                    val thumbnailUrl = YoutubeUtil.getThumbnailUrl(id, YoutubeUtil.ThumbnailQuality.HIGH)
                    Log.d(TAG, "썸네일 URL: $thumbnailUrl")
                    
                    Glide.with(binding.root.context)
                        .load(thumbnailUrl)
                        .placeholder(R.drawable.white)
                        .into(binding.bannerImage)
                } ?: run {
                    Log.d(TAG, "videoId가 null이어서 기본 이미지 사용")
                    binding.bannerImage.setImageResource(R.drawable.white)
                }
            }
            
            // 클릭 이벤트 설정 (더미 아이템이 아닌 경우에만)
            if (lecture.lectureId != -1) {
                binding.root.setOnClickListener {
                    onItemClick?.invoke(lecture)
                }
            } else {
                binding.root.setOnClickListener(null)
            }
        }
    }
}

class BannerDiffCallback : DiffUtil.ItemCallback<Lecture>() {
    override fun areItemsTheSame(oldItem: Lecture, newItem: Lecture): Boolean {
        return oldItem.lectureId == newItem.lectureId
    }

    override fun areContentsTheSame(oldItem: Lecture, newItem: Lecture): Boolean {
        return oldItem == newItem
    }
}
