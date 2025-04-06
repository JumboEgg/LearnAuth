package com.example.second_project.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.data.model.dto.response.RegisterEmailResponse
import com.example.second_project.databinding.ItemRegisterSearchParticipantsBinding

class RegisterSearchParticipantsAdapter(
    private val onItemClick: (String) -> Unit // 클릭 시 사용자 이름 전달
) : RecyclerView.Adapter<RegisterSearchParticipantsAdapter.ViewHolder>() {

    private var userList: List<RegisterEmailResponse> = emptyList()

    inner class ViewHolder(
        private val binding: ItemRegisterSearchParticipantsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: RegisterEmailResponse) {
            binding.textUserEmail.text = user.email
            binding.root.setOnClickListener {
                onItemClick(user.email)
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

    fun updateData(newList: List<RegisterEmailResponse>) {
        Log.d("searchUsers", "adapter 업데이트된 리스트 사이즈: ${newList.size}")
        userList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = userList.size
}
