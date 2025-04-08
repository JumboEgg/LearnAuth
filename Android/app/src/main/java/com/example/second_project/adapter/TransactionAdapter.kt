package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.data.TransactionItem
import java.text.DecimalFormat

private const val TAG = "TransactionAdapter_야옹"

class TransactionAdapter(private val transactionList: List<TransactionItem>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        // 거래 내역 설정
        holder.lectureTitle.text = item.title  // 강의 제목
        holder.lectureDate.text = item.date       // 날짜

        val decimal = DecimalFormat("#,###")
        val price = decimal.format(item.amount)

        // "토큰 충전"이 아닐 경우 "-" 접두사 추가 및 red 컬러 설정
        if (item.title != "토큰 충전") {
            holder.lecturePrice.text = "-${price} CAT"

        } else {
            holder.lecturePrice.text = "${price} CAT"

        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}
