package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.databinding.ItemRegisterSearchParticipantsBinding

class RegisterSearchParticipantsAdapter(
    private val userList: List<String>, // 더미 사용자 리스트
    private val onItemClick: (String) -> Unit // 클릭 시 사용자 이름 전달
) : RecyclerView.Adapter<RegisterSearchParticipantsAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ItemRegisterSearchParticipantsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(name: String) {
            binding.textUserEmail.text = name
            binding.root.setOnClickListener {
                onItemClick(name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegisterSearchParticipantsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size
}
