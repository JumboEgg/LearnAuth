package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

            // 클릭 시 CertificateData 객체를 콜백으로 전달
            binding.root.setOnClickListener {
                onItemClick(item)
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
