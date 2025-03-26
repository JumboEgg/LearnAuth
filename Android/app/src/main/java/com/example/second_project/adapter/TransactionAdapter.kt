package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R

class TransactionAdapter(private val transactionList: List<List<String>>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        val lectureTitle: TextView = itemView.findViewById(R.id.transactionLectureTitle)
        val lectureDate: TextView = itemView.findViewById(R.id.transactionLectureData)
        val lecturePrice: TextView = itemView.findViewById(R.id.transactionLecturePrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = transactionList[position]

        // 카테고리 값에 따른 아이콘 설정
        val category = item[0].toInt()
        val iconRes = when (category) {
            1 -> R.drawable.dataicon
            2 -> R.drawable.lifeicon
            3 -> R.drawable.lawicon
            4 -> R.drawable.sporticon
            5 -> R.drawable.marketingicon
            6 -> R.drawable.mathicon
            else -> R.drawable.dataicon
        }
        holder.categoryIcon.setImageResource(iconRes)

        // 거래 내역 설정
        holder.lectureTitle.text = item[1]  // 강의 제목
        holder.lectureDate.text = item[2]   // 날짜
        holder.lecturePrice.text = item[3]  // 가격
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}
