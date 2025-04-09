package com.example.second_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.databinding.ItemRegisterParticipantsBinding
import com.example.second_project.utils.disableEmojis

class RegisterParticipantsAdapter(
    private val onDeleteClick: (Int) -> Unit,
    private val onNameClick: (Int) -> Unit,
    private val onLecturerToggle: (Int) -> Unit
) : RecyclerView.Adapter<RegisterParticipantsAdapter.ViewHolder>() {

    private val participantNames = mutableListOf<String>()
    private val isLecturerFlags = mutableListOf<Boolean>()
    private val ratioList = mutableListOf<Int>()

    // 최근 삭제된 이메일 추적
    private val recentlyRemovedEmails = mutableListOf<String>()

    fun addItem(name: String = "", isLecturer: Boolean? = null, ratio: Int? = null) {
        participantNames.add(name)
        isLecturerFlags.add(isLecturer ?: (participantNames.size == 1)) // 첫 번째만 true
        ratioList.add(ratio ?: 0)

        // 아이템 추가 후 즉시 UI 갱신
        notifyItemInserted(participantNames.size - 1)
    }

    fun removeItem(position: Int) {
        if (position in participantNames.indices) {
            // 강의자로 지정된 사람은 삭제 불가능
            if (isLecturerFlags[position]) {
                return
            }

            // 삭제 전에 이메일을 추적 목록에 추가
            val emailToRemove = participantNames[position]
            if (emailToRemove.isNotBlank()) {
                recentlyRemovedEmails.add(emailToRemove)
                // 최대 20개 이메일만 추적하여 메모리 관리
                if (recentlyRemovedEmails.size > 20) {
                    recentlyRemovedEmails.removeAt(0)
                }
            }

            participantNames.removeAt(position)
            isLecturerFlags.removeAt(position)
            ratioList.removeAt(position)

            // ✅ 삭제 후 강의자 없는 경우 → 첫 번째 사람을 강의자로 설정
            if (!isLecturerFlags.contains(true) && isLecturerFlags.isNotEmpty()) {
                isLecturerFlags[0] = true
            }

            // 아이템 삭제 알림
            notifyItemRemoved(position)

            // 삭제 후 위치가 변경된 항목들 업데이트
            if (position < participantNames.size) {
                notifyItemRangeChanged(position, participantNames.size - position)
            }
        }
    }

    // 강의자인지 확인하는 메소드
    fun isLecturer(position: Int): Boolean {
        return position in isLecturerFlags.indices && isLecturerFlags[position]
    }

    fun setItems(names: List<String>, lecturers: List<Boolean>, ratios: List<Int>? = null) {
        participantNames.clear()
        isLecturerFlags.clear()
        ratioList.clear()

        participantNames.addAll(names)
        isLecturerFlags.addAll(lecturers)
        ratioList.addAll(ratios ?: List(names.size) { 0 })

        // 전체 데이터 변경 알림
        notifyDataSetChanged()
    }

    fun getParticipantData(): List<Triple<String, Int, Boolean>> {
        return participantNames.indices.map { i ->
            Triple(participantNames[i], ratioList[i], isLecturerFlags[i])
        }
    }

    // 최근 삭제된 이메일인지 확인
    fun wasRecentlyRemoved(email: String): Boolean {
        return recentlyRemovedEmails.contains(email)
    }

    // 최근 삭제된 이메일 목록 초기화
    fun clearRecentlyRemovedList() {
        recentlyRemovedEmails.clear()
    }

    inner class ViewHolder(private val binding: ItemRegisterParticipantsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(name: String, position: Int) {
            // 이메일 필드 설정
            binding.editTextNameParticipants.editText?.apply {
                isFocusable = false
                isClickable = true
                setText(name)
            }

            binding.editRegisterUser.setOnClickListener {
                onNameClick(adapterPosition)
            }

            // 삭제 버튼 설정 - 강의자 확인
            binding.delete.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    if (isLecturerFlags[currentPosition]) {
                        // 강의자는 삭제할 수 없다는 메시지 표시
                        val context = binding.root.context
                        Toast.makeText(context, "강의자로 지정된 사용자는 삭제할 수 없습니다.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        onDeleteClick(currentPosition)
                    }
                }
            }

            // 비율 필드 설정
            val currentRatio = ratioList.getOrNull(position) ?: 0
            binding.editTextRatioParticipants.editText?.setText(
                if (currentRatio > 0) currentRatio.toString() else ""
            )

            // 이모지 비활성화
            binding.editTextRatioParticipants.editText?.disableEmojis()

            // 비율 텍스트 변경 리스너 설정
            setupRatioTextChangeListener()

            // 강의자 체크 이미지 설정
            updateLecturerUI(isLecturerFlags[position])

            // 강의자 체크 클릭 이벤트
            binding.isLecturer.setOnClickListener {
                // 전체 false로 초기화
                for (i in isLecturerFlags.indices) {
                    isLecturerFlags[i] = false
                }

                // 현재 항목만 true로 설정
                isLecturerFlags[position] = true

                // UI 갱신
                notifyDataSetChanged()

                // 콜백 호출
                onLecturerToggle(position)
            }
        }

        private fun updateLecturerUI(isChecked: Boolean) {
            binding.isLecturer.getChildAt(0)?.let { imageView ->
                if (imageView is ImageView) {
                    imageView.setImageResource(
                        if (isChecked) R.drawable.ic_process_checked
                        else R.drawable.ic_process_unchecked
                    )
                }
            }
        }

        private fun setupRatioTextChangeListener() {
            // 비율 입력 텍스트 변경 감지
            binding.editTextRatioParticipants.editText?.addTextChangedListener {
                val ratio = it.toString().toIntOrNull() ?: 0
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && position < ratioList.size) {
                    ratioList[position] = ratio
                }
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