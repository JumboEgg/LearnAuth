package com.example.second_project.adapter

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.google.android.material.button.MaterialButton

class CategoryAdapter(
    private val categories: List<String>,
    private val onCategorySelected: (Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedCategory = 0;

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

        updateButton(holder.button, position == selectedCategory)

        holder.button.setOnClickListener {
            val adapterPosition = holder.adapterPosition

            if (adapterPosition != RecyclerView.NO_POSITION) {
                if (selectedCategory != adapterPosition) {
                    val oldCategory = selectedCategory
                    selectedCategory = adapterPosition

                    notifyItemChanged(oldCategory)
                    notifyItemChanged(adapterPosition)

                    onCategorySelected(adapterPosition)
                }
            }

        }

    }

    override fun getItemCount(): Int = categories.size

    private fun updateButton(button: MaterialButton, isSeletecd: Boolean) {
        if (isSeletecd) {
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
}
