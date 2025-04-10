package com.example.second_project.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.second_project.R
import com.example.second_project.data.model.dto.response.SubLecture
import com.example.second_project.databinding.ItemOwnedSublectureBinding
import com.example.second_project.utils.YoutubeUtil

private const val TAG = "OwnedLectureDetailAdapter_야옹"
class OwnedLectureDetailAdapter(
    private var subLectureList: List<SubLecture>,
    private val onItemClick: (SubLecture) -> Unit
) : RecyclerView.Adapter<OwnedLectureDetailAdapter.LectureViewHolder>() {

    // subLectureList 업데이트 메서드 추가
    fun updateSubLectureList(newList: List<SubLecture>) {
        subLectureList = newList
        notifyDataSetChanged()
    }

    inner class LectureViewHolder(private val binding: ItemOwnedSublectureBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(subLecture: SubLecture, position: Int) {
            binding.eachNum.text = "${position +1}강"
            binding.eachTitle.text = subLecture.subLectureTitle
            binding.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

            // 썸네일 설정
            val videoId = if (subLecture.lectureUrl.contains("youtube.com") || subLecture.lectureUrl.contains("youtu.be")) {
                YoutubeUtil.getThumbnailUrl(subLecture.lectureUrl)
            } else {
                subLecture.lectureUrl  // 이미 비디오 ID인 경우
            }

            if (videoId != null) {
                val thumbnailUrl = YoutubeUtil.getThumbnailUrl(videoId, YoutubeUtil.ThumbnailQuality.MEDIUM)
                Glide.with(binding.root.context)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.white)
                    .into(binding.eachThumnail)
            } else {
                Log.e(TAG, "유효한 유튜브 URL이 아님: ${subLecture.lectureUrl}")
            }

            // 강의 상태에 따라 버튼 텍스트 설정
            updateButtonText(subLecture)

            binding.root.setOnClickListener {
                onItemClick(subLecture)
            }
        }
        
        // 강의 상태에 따라 버튼 텍스트 업데이트
        private fun updateButtonText(subLecture: SubLecture) {
            if(subLecture.endFlag == true) {
                // 완강한 경우
                binding.eachWatchBtn.text = "다시보기"
            } else if (subLecture.continueWatching > 0) {
                // 0초 넘게 시청한 경우
                binding.eachWatchBtn.text = "이어보기"
            } else {
                // 시청하지 않은 경우
                binding.eachWatchBtn.text = "수강하기"
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