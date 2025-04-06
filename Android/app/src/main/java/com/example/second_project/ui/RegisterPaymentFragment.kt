package com.example.second_project.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.R
import com.example.second_project.adapter.RegisterParticipantsAdapter
import com.example.second_project.adapter.RegisterSearchParticipantsAdapter
import com.example.second_project.data.model.dto.request.Ratio
import com.example.second_project.databinding.DialogRegisterSearchParticipantsBinding
import com.example.second_project.databinding.FragmentRegisterPaymentBinding
import com.example.second_project.interfaces.RegisterStepSavable
import com.example.second_project.viewmodel.RegisterViewModel

class RegisterPaymentFragment : Fragment(), RegisterStepSavable {

    private var _binding: FragmentRegisterPaymentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RegisterParticipantsAdapter

    private val viewModel: RegisterViewModel by activityViewModels()

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

        // 어댑터 초기화
        adapter = RegisterParticipantsAdapter(
            onLecturerToggle = { /* 필요 시 구현 가능 */ },
            onDeleteClick = { position -> adapter.removeItem(position) },
            onNameClick = { position ->
                val dialogBinding = DialogRegisterSearchParticipantsBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogBinding.root)
                    .create()
                dialog.window?.apply {
                    setBackgroundDrawableResource(R.drawable.bg_radius_20)

                    val params = attributes
                    params.width =
                        (resources.displayMetrics.widthPixels * 0.6).toInt()
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT
                    attributes = params
                }

                val dummyUsers = listOf("user1@example.com", "user2@example.com", "user3@example.com")
                var selectedEmail: String? = null

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
                        adapter.updateParticipantName(position, it)
                    }
                    dialog.dismiss()
                }
                dialogBinding.btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        )

        binding.recyclerParticipants.adapter = adapter

        // 추가 버튼
        binding.btnAddParticipants.setOnClickListener {
            adapter.addItem()
        }


        // 가격 설정
        // binding.editTextPrice.editText?.setText(if (viewModel.price == 0) "" else viewModel.price.toString())
        binding.editTextPrice.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                val priceText = s.toString().trim()

                // 0으로 시작하면서 길이가 2 이상이면 잘못된 입력 (예: 01, 01234)
                if (priceText.length > 1 && priceText.startsWith("0")) {
                    Toast.makeText(requireContext(), "가격은 0으로 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    binding.editTextPrice.editText?.setText("")
                    return
                }

                // 입력이 없으면 내부적으로 0 저장
                if (priceText.isEmpty()) {
                    viewModel.price = 0
                    return
                }

                val price = priceText.toIntOrNull()
                if (price == null) {
                    Toast.makeText(requireContext(), "유효한 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return
                }

                // 백만원 초과 시 자동 수정
                if (price > 1000000) {
                    Toast.makeText(requireContext(), "가격은 최대 1,000,000원까지 입력 가능합니다.", Toast.LENGTH_SHORT).show()
                    binding.editTextPrice.editText?.setText("1000000")
                    binding.editTextPrice.editText?.setSelection(binding.editTextPrice.editText?.text?.length ?: 0)
                    viewModel.price = 1000000
                    return
                }

                viewModel.price = price
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })



        // 가격 복원 (ViewModel에 저장된 값이 있을 경우)
        if (viewModel.price >= 0) {
            binding.editTextPrice.editText?.setText(viewModel.price.toString())
        }

        // 기존 참여자 정보가 있을 경우 초기화
        if (viewModel.ratios.isNotEmpty()) {
            val names = viewModel.ratios.map { it.email }
            val lecturers = viewModel.ratios.map { it.lecturer }
            val ratios = viewModel.ratios.map { it.ratio }
            adapter.setItems(names, lecturers, ratios)
        }

        // 다음 버튼
        binding.btnToSubLecture.setOnClickListener {
            val saved = saveDataToViewModel()
            if (saved) {
                (parentFragment as? RegisterMainFragment)?.moveToStep(3)
            }
        }
    }

    // 인터페이스 구현
    override fun saveDataToViewModel(): Boolean  {
        // 가격 저장
        val priceText = binding.editTextPrice.editText?.text.toString()
        if (priceText.isBlank()) {
            Toast.makeText(requireContext(), "가격을 입력해주세요. 0원도 가능합니다.", Toast.LENGTH_SHORT).show()
            return false
        }

        val price = priceText.toIntOrNull()
        if (price == null || price < 0) {
            Toast.makeText(requireContext(), "올바른 금액을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        viewModel.price = price


        val participantData = adapter.getParticipantData()
        // ❗ 빈 이메일 존재 확인
        val hasInvalidEmail = participantData.any { it.first.isBlank() }
        if (hasInvalidEmail) {
            Toast.makeText(requireContext(), "참여자 이메일을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        // 참여자 정보 저장
        viewModel.ratios.clear()
        adapter.getParticipantData().forEach { (email, ratio, isLecturer) ->
            if (email.isNotBlank()) {
                viewModel.ratios.add(Ratio(email, ratio, isLecturer))
            }
        }

        val totalRatio = participantData.sumOf { it.second }
        if (totalRatio != 100) {
            Toast.makeText(requireContext(), "정산 비율의 총합은 반드시 100이어야 합니다.", Toast.LENGTH_SHORT).show()
            return false
        }


        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
