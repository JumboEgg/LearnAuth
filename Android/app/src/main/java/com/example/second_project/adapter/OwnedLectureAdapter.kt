package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.databinding.ItemOwnedlectureDetailBinding

class OwnedLectureAdapter(private val items: List<LectureItem>) :
    RecyclerView.Adapter<OwnedLectureAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemOwnedlectureDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LectureItem) {
            binding.eachNum.text = item.lectureNum
            binding.eachTitle.text = item.lectureTitle
            binding.eachThumnail.setImageResource(item.thumbnailResId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOwnedlectureDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

data class LectureItem(val lectureNum: String, val lectureTitle: String, val thumbnailResId: Int)
