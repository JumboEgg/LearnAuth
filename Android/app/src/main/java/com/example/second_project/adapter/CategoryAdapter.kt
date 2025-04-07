package com.example.second_project.adapter

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.google.android.material.button.MaterialButton

class CategoryAdapter(
    private val categories: List<String>,
    private val onCategorySelected: (Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedCategory = 0

    class CategoryViewHolder(val button: MaterialButton) : RecyclerView.ViewHolder(button)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val button = MaterialButton(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.text_white_blue))
            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.button_white_blue)
            )
            cornerRadius = 40
        }
        return CategoryViewHolder(button)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.button.text = categories[position]

        // 선택 상태에 따라 스타일 변경
        updateButton(holder.button, position == selectedCategory)

        holder.button.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                // == (1) 항상 콜백 호출: 같은 카테고리여도 재검색/재로드 가능
                onCategorySelected(adapterPosition)

                // == (2) '선택 색' 업데이트는 바뀐 경우에만
                if (selectedCategory != adapterPosition) {
                    val oldCategory = selectedCategory
                    selectedCategory = adapterPosition
                    notifyItemChanged(oldCategory)
                    notifyItemChanged(adapterPosition)
                }
            }
        }
    }

    override fun getItemCount(): Int = categories.size

    private fun updateButton(button: MaterialButton, isSelected: Boolean) {
        if (isSelected) {
            button.setTextColor(ContextCompat.getColor(button.context, R.color.white))
            button.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(button.context, R.color.primary_color)
            )
        } else {
            button.setTextColor(ContextCompat.getColor(button.context, R.color.text_white_blue))
            button.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(button.context, R.color.button_white_blue)
            )
        }
    }

    fun setSelectedPosition(position: Int) {
        if (position in 0 until categories.size && position != selectedCategory) {
            val oldPosition = selectedCategory
            selectedCategory = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(position)
        }
    }
}
