package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.databinding.ItemCertificationBinding

class CertificationAdapter(
    private val items: List<Pair<String, String>>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<CertificationAdapter.CertViewHolder>() {

    inner class CertViewHolder(private val binding: ItemCertificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<String, String>) {
            binding.textTitleMyCertification.text = item.first
            binding.textCategoryCertification.text = item.second

            // 클릭 시 onItemClick 호출
            binding.root.setOnClickListener {
                onItemClick(adapterPosition)
            }

            // 흘러가기 텍스트
//            binding.textTitleMyCertification.isSelected = true

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

}