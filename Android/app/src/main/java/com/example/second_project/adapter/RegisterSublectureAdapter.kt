package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.second_project.R
import com.example.second_project.data.model.dto.RegisterTempSubLecture
import com.example.second_project.data.model.dto.request.SubLecture
import com.example.second_project.databinding.ItemRegisterSublectureDetailBinding

class RegisterSublectureAdapter (
    private val subLectureCount: () -> Int,
    private val onDeleteClick: (Int) -> Unit,
    private val onLoadVideoClick: (position: Int, url: String) -> Unit
) : RecyclerView.Adapter<RegisterSublectureAdapter.ViewHolder>(){

    private val tempSubLectures = mutableListOf<RegisterTempSubLecture>()
    private val isExpandedList = mutableListOf<Boolean>()

    inner class ViewHolder(private val binding: ItemRegisterSublectureDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            val item = tempSubLectures[position]
            // 제목 업데이트 (ex. 개별 강의 1)
            binding.textSubLectureIndex.text = "개별 강의 ${position + 1}"

            // 초기 펼침 상태 설정
            binding.linearToggleArea.visibility = if (isExpandedList[position]) View.VISIBLE else View.GONE

            // 펼침/접힘 토글
            binding.btnToggleSubLecture.setOnClickListener {

                val expanded = !isExpandedList[position]
                isExpandedList[position] = expanded

                // 아이콘 변경
                val iconRes = if (expanded) {
                    R.drawable.keyboard_arrow_up_24px
                } else {
                    R.drawable.keyboard_arrow_down_24px
                }
                binding.btnToggleSubLecture.setImageResource(iconRes)

                notifyItemChanged(position)
            }

            // 삭제 버튼
            binding.btnDeleteSubLecture.setOnClickListener {
                isExpandedList.removeAt(position)
                onDeleteClick(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)
            }

            binding.editTextTitle.editText?.setText(item.title)
            binding.editURL.editText?.setText(item.inputUrl)

            // 불러온 제목 보여주기
            binding.textYoutubeTitle.text = item.videoTitle

            // 썸네일 보여주기 (Glide로 로딩)
            Glide.with(binding.imageViewThumbnail)
                .load(item.thumbnailUrl)
                .into(binding.imageViewThumbnail)


            // 제목 수정
            binding.editTextTitle.editText?.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    item.title = binding.editTextTitle.editText?.text.toString()
                }
            }

            // URL 수정
            binding.editURL.editText?.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    item.inputUrl = binding.editURL.editText?.text.toString()
                }
            }

            // 영상 불러오기 버튼 클릭
            binding.btnConfirm.setOnClickListener {
                val url = binding.editURL.editText?.text.toString()
                onLoadVideoClick(position, url)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegisterSublectureDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = isExpandedList.size

    fun addItem() {
        tempSubLectures.add(RegisterTempSubLecture())
        isExpandedList.add(true)
        notifyItemInserted(tempSubLectures.size - 1)
    }


    fun removeAll() {
        isExpandedList.clear()
        notifyDataSetChanged()
    }

    fun getTempSubLectures(): List<RegisterTempSubLecture> = tempSubLectures

    fun setItems(tempList: List<RegisterTempSubLecture>) {
        tempSubLectures.clear()
        tempSubLectures.addAll(tempList)
        isExpandedList.clear()
        isExpandedList.addAll(List(tempList.size) { true })
        notifyDataSetChanged()
    }
}