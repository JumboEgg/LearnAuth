package com.example.second_project.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.databinding.ItemRegisterParticipantsBinding

class RegisterParticipantsAdapter(
    private val participantNames: MutableList<String>,
    private val onDeleteClick: (Int) -> Unit,
    private val onNameClick: (Int) -> Unit,
    private val onLecturerToggle: (Int) -> Unit,
    private val isLecturerFlags: MutableList<Boolean>,
)  : RecyclerView.Adapter<RegisterParticipantsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemRegisterParticipantsBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(name: String, position: Int) {
                binding.editTextNameParticipants.editText?.apply {
                    isFocusable = false
                    isClickable = true
                }

                binding.editTextNameParticipants.editText?.setText(name)

                binding.editRegisterUser.setOnClickListener{
                    onNameClick(position)
                }

                binding.delete.setOnClickListener {
                    onDeleteClick(position)
                }

                // 강의자 체크 이미지 토글

                // 초기 체크 상태 UI 설정
                val isChecked = isLecturerFlags[position]
                binding.isLecturer.getChildAt(0)?.let { imageView ->
                    if (imageView is ImageView) {
                        imageView.setImageResource(
                            if (isChecked) R.drawable.ic_process_checked else R.drawable.ic_process_unchecked
                        )
                    }
                }

                // 체크 클릭 시 → 하나만 체크되도록
                binding.isLecturer.setOnClickListener {
                    // 전체 false로 초기화
                    for (i in isLecturerFlags.indices) {
                        isLecturerFlags[i] = false
                    }
                    // 현재 것만 true
                    isLecturerFlags[position] = true
                    notifyDataSetChanged() // 전체 UI 갱신

                    onLecturerToggle(position)
                }

            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegisterParticipantsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(participantNames[position], position)
    }

    override fun getItemCount(): Int = participantNames.size

}