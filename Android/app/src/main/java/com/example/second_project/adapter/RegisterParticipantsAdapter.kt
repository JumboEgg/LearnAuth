package com.example.second_project.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.databinding.ItemRegisterParticipantsBinding
import androidx.core.widget.addTextChangedListener
import com.example.second_project.utils.disableEmojis

class RegisterParticipantsAdapter(
    private val onDeleteClick: (Int) -> Unit,
    private val onNameClick: (Int) -> Unit,
    private val onLecturerToggle: (Int) -> Unit
)  : RecyclerView.Adapter<RegisterParticipantsAdapter.ViewHolder>() {
    private val participantNames = mutableListOf<String>()
    private val isLecturerFlags = mutableListOf<Boolean>()
    private val ratioList = mutableListOf<Int>()

    fun addItem(name: String = "", isLecturer: Boolean? = null, ratio: Int? = null) {
        participantNames.add(name)
        isLecturerFlags.add(isLecturer ?: (participantNames.size == 1)) // 첫 번째만 true
        ratioList.add(ratio ?: 0)
        notifyItemInserted(participantNames.size - 1)
    }


    fun removeItem(position: Int) {
        if (position in participantNames.indices) {
            participantNames.removeAt(position)
            isLecturerFlags.removeAt(position)
            ratioList.removeAt(position)

            // ✅ 삭제 후 강의자 없는 경우 → 첫 번째 사람을 강의자로 설정
            if (!isLecturerFlags.contains(true) && isLecturerFlags.isNotEmpty()) {
                isLecturerFlags[0] = true
            }

            notifyDataSetChanged()
        }
    }

    fun setItems(names: List<String>, lecturers: List<Boolean>, ratios: List<Int>? = null) {
        participantNames.clear()
        isLecturerFlags.clear()
        ratioList.clear()

        participantNames.addAll(names)
        isLecturerFlags.addAll(lecturers)
        ratioList.addAll(ratios ?: List(names.size) { 0 })

        notifyDataSetChanged()
    }

    fun getParticipantData(): List<Triple<String, Int, Boolean>> {
        return participantNames.indices.map { i ->
            Triple(participantNames[i], ratioList[i], isLecturerFlags[i])
        }
    }


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

                binding.editTextRatioParticipants.editText?.setText(ratioList.getOrNull(position)?.toString() ?: "")

                binding.editTextRatioParticipants.editText?.disableEmojis()
                binding.editTextRatioParticipants.editText?.addTextChangedListener {
                    val ratio = it.toString().toIntOrNull() ?: 0
                    if (adapterPosition in ratioList.indices) {
                        ratioList[adapterPosition] = ratio
                    }
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

    fun getRatioForPosition(position: Int): Int? {
        return ratioList.getOrNull(position)
    }
    fun updateParticipantName(position: Int, newName: String) {
        if (position in participantNames.indices) {
            participantNames[position] = newName
            notifyItemChanged(position)
        }
    }


}