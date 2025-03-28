package com.example.second_project.adapter
//
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.data.ReportItem
import com.example.second_project.databinding.ItemReportBinding
class ReportAdapter(
    private val onItemClick: (ReportItem) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    private val items = mutableListOf<ReportItem>()

    // 외부에서 리스트 전체를 갱신하고 싶을 때 사용
    fun submitList(newList: List<ReportItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ReportViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReportItem) {
            binding.tvReportTitle.text = item.title
            binding.tvReportType.text = item.type

            // 아이템 클릭
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
