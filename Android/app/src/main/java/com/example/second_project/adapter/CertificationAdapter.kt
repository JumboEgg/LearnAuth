package com.example.second_project.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.data.model.dto.response.CertificateData
import com.example.second_project.databinding.ItemCertificationBinding

class CertificationAdapter(
    private var items: List<CertificateData>,
    private val onItemClick: (CertificateData) -> Unit
) : RecyclerView.Adapter<CertificationAdapter.CertViewHolder>() {

    inner class CertViewHolder(private val binding: ItemCertificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CertificateData) {
            binding.textTitleMyCertification.text = item.title
            binding.textCategoryCertification.text = item.categoryName

            val tintColor = getColorForCategory(item.categoryName)
            binding.root.background?.mutate()?.setTint(tintColor)

            // 카테고리 텍스트 색상 변경
            val categoryTextColor = getCategoryTextColor(item.categoryName)
            binding.textCategoryCertification.setTextColor(categoryTextColor)

            val titleTextColor = getTitleTextColor(item.categoryName)
            binding.textTitleMyCertification.setTextColor(titleTextColor)

            // "자세히 보기" 버튼 텍스트 색상 변경
            val buttonTextColor = getCategoryTextColor(item.categoryName)
            binding.buttonDetail.setTextColor(buttonTextColor)

            val borderColor = getBorderColorForCategory(item.categoryName)
            val drawable = binding.textCategoryCertification.background
            if (drawable is GradientDrawable) {
                val strokeWidth = binding.root.context.resources.getDimensionPixelSize(R.dimen.stroke_width)
                drawable.setStroke(strokeWidth, borderColor)
            }

            // 클릭 시 CertificateData 객체를 콜백으로 전달
            binding.root.setOnClickListener {
                onItemClick(item)
            }

            // "자세히 보기" 버튼 클릭 이벤트 추가
            binding.buttonDetail.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun getColorForCategory(category:String):Int{
            val context = binding.root.context
            return when (category) {
                "수학" -> ContextCompat.getColor(context, R.color.math)
                "생물학" -> ContextCompat.getColor(context, R.color.life)
                "법률" -> ContextCompat.getColor(context, R.color.law)
                "통계학" -> ContextCompat.getColor(context, R.color.data)
                "마케팅" -> ContextCompat.getColor(context, R.color.marketing)
                "체육" -> ContextCompat.getColor(context, R.color.sport)
                else -> ContextCompat.getColor(context, R.color.white)
            }
        }

        private fun getCategoryTextColor(category: String): Int {
            val context = binding.root.context
            return when (category) {
                "수학" -> ContextCompat.getColor(context, R.color.math_sub)
                "생물학" -> ContextCompat.getColor(context, R.color.life_sub)
                "법률" -> ContextCompat.getColor(context, R.color.law_sub)
                "통계학" -> ContextCompat.getColor(context, R.color.data_sub)
                "마케팅" -> ContextCompat.getColor(context, R.color.marketing_sub)
                "체육" -> ContextCompat.getColor(context, R.color.sport_sub)
                else -> ContextCompat.getColor(context, R.color.white)
            }
        }

        private fun getBorderColorForCategory(category: String): Int {
            val context = binding.root.context
            return when (category) {
                "수학" -> ContextCompat.getColor(context, R.color.math_sub)
                "생물학" -> ContextCompat.getColor(context, R.color.life_sub)
                "법률" -> ContextCompat.getColor(context, R.color.law_sub)
                "통계학" -> ContextCompat.getColor(context, R.color.data_sub)
                "마케팅" -> ContextCompat.getColor(context, R.color.marketing_sub)
                "체육" -> ContextCompat.getColor(context, R.color.sport_sub)
                else -> ContextCompat.getColor(context, R.color.white)
            }
        }

        private fun getTitleTextColor(category: String): Int {
            val context = binding.root.context
            return when (category) {
                "수학" -> ContextCompat.getColor(context, R.color.black)
                "생물학" -> ContextCompat.getColor(context, R.color.white)
                "법률" -> ContextCompat.getColor(context, R.color.white)
                "통계학" -> ContextCompat.getColor(context, R.color.black)
                "마케팅" -> ContextCompat.getColor(context, R.color.white)
                "체육" -> ContextCompat.getColor(context, R.color.black)
                else -> ContextCompat.getColor(context, R.color.white)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertViewHolder {
        val binding = ItemCertificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CertViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<CertificateData>) {
        items = newItems
        notifyDataSetChanged()
    }
}
