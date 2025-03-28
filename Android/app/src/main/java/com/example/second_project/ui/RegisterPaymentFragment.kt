package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.second_project.adapter.RegisterParticipantsAdapter
import com.example.second_project.databinding.FragmentRegisterPaymentBinding

class RegisterPaymentFragment: Fragment() {

    private var _binding: FragmentRegisterPaymentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RegisterParticipantsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnToSubLecture.setOnClickListener {
            (parentFragment as? RegisterMainFragment)?.moveToStep(3)
        }

        val participantNames = mutableListOf("홍길동", "김싸피")
        val isLecturerFlags = mutableListOf(false, false)

        val adapter = RegisterParticipantsAdapter(
            participantNames,
            onLecturerToggle = { position ->
                // 여기에 "강의자 바뀜"에 대한 동작 작성
            },
            onDeleteClick = { position ->
                participantNames.removeAt(position)
                isLecturerFlags.removeAt(position)
                adapter.notifyItemRemoved(position)
            },
            onNameClick = { position ->
                // 다이얼로그 띄우기
            },
            isLecturerFlags = isLecturerFlags
        )

        binding.recyclerParticipants.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}