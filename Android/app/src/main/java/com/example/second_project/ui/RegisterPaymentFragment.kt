package com.example.second_project.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.adapter.RegisterParticipantsAdapter
import com.example.second_project.adapter.RegisterSearchParticipantsAdapter
import com.example.second_project.databinding.DialogRegisterSearchParticipantsBinding
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

        binding.recyclerParticipants.visibility = View.VISIBLE
        binding.recyclerParticipants.layoutManager = LinearLayoutManager(requireContext())

        binding.btnToSubLecture.setOnClickListener {
            (parentFragment as? RegisterMainFragment)?.moveToStep(3)
        }

        val participantNames = mutableListOf<String>()
        val isLecturerFlags = mutableListOf<Boolean>()

        adapter = RegisterParticipantsAdapter(
            participantNames,
            onLecturerToggle = { position ->
                // 여기에 "강의자 바뀜"에 대한 동작 작성
            },
            onDeleteClick = { position ->
                participantNames.removeAt(position)
                isLecturerFlags.removeAt(position)
                adapter.notifyDataSetChanged()
            },
            onNameClick = { position ->

                val dialogBinding = DialogRegisterSearchParticipantsBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogBinding.root)
                    .create()

                val dummyUsers = listOf("ssafy@naver.com", "hello@world.com", "test@domain.com")
                var selectedEmail: String? = null  // 선택된 이메일 저장 변수

                val dialogAdapter = RegisterSearchParticipantsAdapter(dummyUsers) { email ->
                    dialogBinding.editSearchParticipants.editText?.setText(email)
                    selectedEmail = email
                }

                dialogBinding.recyclerUserList.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = dialogAdapter
                    visibility = View.VISIBLE
                }

                dialogBinding.btnRegisterParticipants.setOnClickListener {
                    selectedEmail?.let {
                        participantNames[position] = it
                        adapter.notifyItemChanged(position)
                    }
                    dialog.dismiss()
                }
                dialog.show()
            },
            isLecturerFlags = isLecturerFlags
        )

        binding.recyclerParticipants.adapter = adapter

        binding.btnAddParticipants.setOnClickListener {
            participantNames.add("") // 빈 문자열
            isLecturerFlags.add(false)
            adapter.notifyItemInserted(participantNames.size - 1)
        }




    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}