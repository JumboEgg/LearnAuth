package com.example.second_project.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.adapter.RegisterParticipantsAdapter
import com.example.second_project.adapter.RegisterSearchParticipantsAdapter
import com.example.second_project.data.model.dto.request.Ratio
import com.example.second_project.data.model.dto.response.RegisterEmailResponse
import com.example.second_project.databinding.DialogRegisterSearchParticipantsBinding
import com.example.second_project.databinding.FragmentRegisterPaymentBinding
import com.example.second_project.interfaces.RegisterStepSavable
import com.example.second_project.utils.KeyboardUtils.hideKeyboard
import com.example.second_project.viewmodel.RegisterViewModel
import com.example.second_project.utils.disableEmojis

class RegisterPaymentFragment : Fragment(), RegisterStepSavable {

    private var _binding: FragmentRegisterPaymentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RegisterParticipantsAdapter

    private val viewModel: RegisterViewModel by activityViewModels()

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val debounceDelay = 500L
    private var isLoading = false

    private var currentActionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.isFocusableInTouchMode = true

        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // ActionMode가 활성화된 상태라면 종료
                currentActionMode?.finish()

                // 포커스 클리어
                val currentFocus = activity?.currentFocus
                if (currentFocus != null && currentFocus !is EditText) {
                    currentFocus.clearFocus()
                }

                false // 이벤트를 소비하지 않고 계속 전파
            } else {
                false
            }
        }

        viewModel.clearSearchResults()

        // RecyclerView 설정
        binding.recyclerParticipants.apply {
            layoutManager = LinearLayoutManager(requireContext())
            visibility = View.VISIBLE
        }

        // 입력 필드 설정
        binding.editTextPrice.editText?.disableEmojis()

        // 어댑터 초기화
        adapter = RegisterParticipantsAdapter(
            onDeleteClick = { position ->
                // 강의자 체크는 어댑터 내부에서 처리됨
                if (!adapter.isLecturer(position)) {
                    // 삭제 작업
                    val removedEmail = adapter.getParticipantData().getOrNull(position)?.first
                    adapter.removeItem(position)

                    // viewModel에서도 함께 삭제
                    removedEmail?.let { email ->
                        viewModel.ratios.removeAll { it.email == email }
                    }
                }
            },
            onNameClick = { position ->
                openSearchDialog(position)
            },
            onLecturerToggle = { position ->
                binding.editTextPrice.editText?.clearFocus()
                hideKeyboard(binding.root, requireContext())
            }
        )

        // RecyclerView에 어댑터 연결
        binding.recyclerParticipants.adapter = adapter

        // 추가 버튼 설정
        binding.btnAddParticipants.setOnClickListener {
            binding.root.clearFocus()

            // 참여자 최대 10명 제한
            if (adapter.itemCount >= 10) {
                Toast.makeText(requireContext(), "참여자는 최대 10명까지 등록할 수 있습니다.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // 항목 추가
            adapter.addItem()
        }

        // 가격 입력 필드 설정
        setupPriceInput()

        // 기존 데이터 복원
        restoreViewModelData()

        // 다음 버튼 설정
        binding.btnToSubLecture.setOnClickListener {
            // 키보드 숨김
            binding.editTextPrice.editText?.clearFocus()
            hideKeyboard(binding.root, requireContext())

            // 저장 후 다음 단계로
            val saved = saveDataToViewModel()
            if (saved) {
                (parentFragment as? RegisterMainFragment)?.moveToStep(3)
            }
        }
    }

    // 가격 입력 필드 설정
    private fun setupPriceInput() {
        val priceTextWatcher = object : TextWatcher {
            private var isUpdating = false

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val priceText = s.toString().trim()

                // 입력이 없으면 내부적으로 0 저장
                if (priceText.isEmpty()) {
                    viewModel.price = 0
                    return
                }

                try {
                    isUpdating = true

                    val price = priceText.toIntOrNull()
                    if (price == null) {
                        return
                    }

                    // 백만원 초과 시 자동 수정
                    if (price > 1000000) {
                        Toast.makeText(
                            requireContext(),
                            "가격은 최대 1,000,000원까지 입력 가능합니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        s?.replace(0, s.length, "1000000")
                        binding.editTextPrice.editText?.setSelection(7) // 커서 위치 조정
                        viewModel.price = 1000000
                    } else {
                        viewModel.price = price
                    }
                } finally {
                    isUpdating = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        // 기존 TextWatcher 설정
        binding.editTextPrice.editText?.addTextChangedListener(priceTextWatcher)
    }

    // ViewModel 데이터 복원
    private fun restoreViewModelData() {
        // 가격 복원
        if (viewModel.price >= 0) {
            binding.editTextPrice.editText?.setText(viewModel.price.toString())
        }

        // 기존 참여자 정보 복원
        if (viewModel.ratios.isNotEmpty()) {
            val names = viewModel.ratios.map { it.email }
            val lecturers = viewModel.ratios.map { it.lecturer }
            val ratios = viewModel.ratios.map { it.ratio }
            adapter.setItems(names, lecturers, ratios)
        } else {
            // 기존 데이터가 없으면 기본 항목 추가
            adapter.addItem()
        }
    }

    // 검색 다이얼로그 열기
    private fun openSearchDialog(position: Int) {
        // 강의자인지 확인
        if (adapter.isLecturer(position) && adapter.getParticipantData()[position].first.isNotBlank()) {
            // 이미 이메일이 등록된 강의자의 경우 변경 불가
            Toast.makeText(requireContext(), "강의자로 지정된 사용자의 이메일은 변경할 수 없습니다.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        var currentKeyword = ""
        val dialogBinding = DialogRegisterSearchParticipantsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.searchInputText.text?.clear()
        dialogBinding.searchInputText.disableEmojis()
        dialogBinding.recyclerUserList.visibility = View.GONE
        dialogBinding.textNoResult.visibility = View.GONE
        dialogBinding.layoutSelectedUser.visibility = View.GONE

        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_radius_20)

            val params = attributes
            params.width = (resources.displayMetrics.widthPixels * 0.6).toInt()
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = params
        }

        var selectedEmail: String? = null
        var selectedNickname: String? = null
        var selectedName: String? = null

        val dialogAdapter = RegisterSearchParticipantsAdapter { email ->
            val user = viewModel.searchResults.value?.find { it.email == email }
            selectedEmail = email
            selectedNickname = user?.nickname
            selectedName = user?.name

            dialogBinding.recyclerUserList.visibility = View.GONE
            dialogBinding.layoutSelectedUser.visibility = View.VISIBLE
            dialogBinding.textSelectedUserName.text = user?.name ?: ""
            dialogBinding.textSelectedUserNickName.text = "(${user?.nickname ?: ""})"
        }

        dialogBinding.recyclerUserList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dialogAdapter
            visibility = View.VISIBLE
        }

        val observer = Observer<List<RegisterEmailResponse>> { results ->
            dialogAdapter.updateData(results)
            if (results.isEmpty()) {
                dialogBinding.recyclerUserList.visibility = View.GONE
                dialogBinding.textNoResult.visibility = View.VISIBLE
                dialogBinding.layoutSelectedUser.visibility = View.GONE
            } else {
                dialogBinding.recyclerUserList.visibility = View.VISIBLE
                dialogBinding.textNoResult.visibility = View.GONE
                dialogBinding.layoutSelectedUser.visibility = View.GONE
            }
        }
        viewModel.searchResults.observe(viewLifecycleOwner, observer)

        dialog.setOnDismissListener {
            binding.root.clearFocus()
            viewModel.searchResults.removeObserver(observer)
            viewModel.clearSearchResults()
        }

        dialogBinding.searchInputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()

                if (keyword.isEmpty()) {
                    viewModel.clearSearchResults()
                    dialogBinding.recyclerUserList.visibility = View.GONE
                    dialogBinding.textNoResult.visibility = View.GONE
                    dialogBinding.layoutSelectedUser.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        dialogBinding.clearBtn.setOnClickListener {
            dialogBinding.searchInputText.text?.clear()
            viewModel.clearSearchResults()
            dialogBinding.recyclerUserList.visibility = View.GONE
            dialogBinding.layoutSelectedUser.visibility = View.GONE
            dialogBinding.textNoResult.visibility = View.GONE
        }

        dialogBinding.btnRegisterParticipants.setOnClickListener {
            selectedEmail?.let { email ->
                // 현재 참가자 목록에서 체크
                val currentEmails = adapter.getParticipantData().map { it.first }
                val isAlreadyRegistered = currentEmails.contains(email)

                // 방금 삭제된 이메일인지 확인
                val wasRecentlyRemoved = adapter.wasRecentlyRemoved(email)

                if (isAlreadyRegistered && !wasRecentlyRemoved) {
                    Toast.makeText(requireContext(), "이미 등록된 참여자입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    // 방금 삭제된 이메일이라면 다시 등록 가능
                    if (wasRecentlyRemoved) {
                        adapter.clearRecentlyRemovedList()
                    }

                    adapter.updateParticipantName(position, email)
                    dialog.dismiss()
                }
            } ?: run {
                Toast.makeText(requireContext(), "사용자를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.searchBtn.setOnClickListener {
            val keyword = dialogBinding.searchInputText.text.toString().trim()
            Log.d("searchUsers", "검색어: $keyword")

            currentKeyword = keyword
            if (keyword.isNotEmpty()) {
                dialogBinding.recyclerUserList.visibility = View.VISIBLE
                dialogBinding.layoutSelectedUser.visibility = View.GONE
                viewModel.searchUsers(keyword)
            }
        }

        dialogBinding.recyclerUserList.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                val isLastItem = lastVisibleItem + 1 >= totalItemCount

                if (isLastItem && !isLoading) {
                    val total = viewModel.totalResults.value ?: return
                    val current = viewModel.searchResults.value?.size ?: 0
                    val nextPage = (viewModel.currentPage.value ?: 1) + 1

                    if (current < total) {
                        isLoading = true // 중복 방지 락
                        viewModel.searchUsers(currentKeyword, nextPage) {
                            isLoading = false // 호출 후 다시 풀기
                        }
                    }
                }
            }
        })

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialogBinding.clearBtn.performClick()
    }

    // 인터페이스 구현
    override fun saveDataToViewModel(): Boolean {
        // 현재 포커스가 있는 입력 필드가 있으면 강제로 커밋
        val currentFocus = activity?.currentFocus
        if (currentFocus != null) {
            currentFocus.clearFocus()
        }

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
        // 빈 이메일 존재 확인
        val hasInvalidEmail = participantData.any { it.first.isBlank() }
        if (hasInvalidEmail) {
            Toast.makeText(requireContext(), "참여자 이메일을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        val emails = participantData.map { it.first }
        if (emails.size != emails.toSet().size) {
            Toast.makeText(requireContext(), "같은 참여자를 두 번 등록할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return false
        }

        // 참여자 정보 저장
        viewModel.ratios.clear()
        adapter.getParticipantData().forEach { (email, ratio, isLecturer) ->
            if (email.isNotBlank()) {
                viewModel.ratios.add(Ratio(email, ratio, isLecturer))
            }
        }

        val hasZeroRatio = participantData.any { it.second == 0 }
        if (hasZeroRatio) {
            Toast.makeText(requireContext(), "정산 비율은 0이 될 수 없습니다.", Toast.LENGTH_SHORT).show()
            return false
        }

        val totalRatio = participantData.sumOf { it.second }
        if (totalRatio != 100) {
            Toast.makeText(requireContext(), "정산 비율의 총합은 반드시 100이어야 합니다.", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        // 강의자가 최소 한 명 이상인지 확인
        val hasLecturer = participantData.any { it.third }
        if (!hasLecturer) {
            Toast.makeText(requireContext(), "최소 한 명 이상의 강의자가 필요합니다.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onDestroyView() {
        // 메모리 누수 방지
        searchHandler.removeCallbacksAndMessages(null)
        searchRunnable = null

        super.onDestroyView()
        _binding = null
    }
}