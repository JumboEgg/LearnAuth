package com.example.second_project.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.data.model.dto.request.Lecture
import com.example.second_project.databinding.ItemLectureBinding

class LectureAdapter(
    private val mainPage: Boolean,
    // onItemClick: 강의 클릭 시 lectureId와 title을 전달
    private val onItemClick: ((Int, String) -> Unit)? = null
) : RecyclerView.Adapter<LectureAdapter.LectureViewHolder>() {

    private val items = mutableListOf<Lecture>()

    // 강의 리스트 업데이트
    fun submitList(data: List<Lecture>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectureViewHolder {
        val binding = ItemLectureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LectureViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: LectureViewHolder, position: Int) {
        holder.bind(items[position])

        if (mainPage) { // 메인 페이지에서의 목록은 고정된 너비 적용
            val layoutParams = holder.itemView.layoutParams
            layoutParams.width = dpToPx(holder.itemView.context, 144)
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.itemView.layoutParams = layoutParams
        }
    }

    override fun getItemCount(): Int = items.size

    class LectureViewHolder(
        private val binding: ItemLectureBinding,
        private val onItemClick: ((Int, String) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Lecture) {
            // 강의 제목을 표시 (추가적인 정보도 필요하면 여기서 바인딩)
            binding.lectureTitle.text = item.title
            binding.lectureTitle.isSelected = true
            binding.lectureTeacherName.text = item.lecturer   // 강의자 정보 바인딩
            binding.lecturePrice.text = "${item.price}원"
            // 클릭 시 lectureId와 title 전달
            binding.root.setOnClickListener {
                onItemClick?.invoke(item.lectureId, item.title)
            }
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
