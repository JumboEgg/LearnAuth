package com.example.second_project.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.second_project.databinding.FragmentRegisterLectureBinding
import com.example.second_project.interfaces.RegisterStepSavable
import com.example.second_project.utils.KeyboardUtils
import com.example.second_project.utils.disableEmojis
import com.example.second_project.utils.setEnterLimit
import com.example.second_project.viewmodel.RegisterViewModel
import com.google.android.material.textfield.TextInputEditText

class RegisterLectureFragment : Fragment(), RegisterStepSavable {
    private var _binding: FragmentRegisterLectureBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by activityViewModels()
    private var ignoreTextChanges = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterLectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 초기화 전에 ViewModel 데이터 복원
        restoreViewModelData()

        // 기본 설정
        setupEditTexts()

        // 카테고리
        setupCategory()

        // 다음 단계로 이동하는 하단 버튼
        binding.btnToUploadFile.setOnClickListener {
            (parentFragment as? RegisterMainFragment)?.moveToStep(1)
        }
    }

    private fun setupEditTexts() {
        // TextInputEditText 레퍼런스
        val titleEditText = binding.editTextTitle.editText
        val goalEditText = binding.editTextGoal.editText
        val contentEditText = binding.editTextContent.editText

        // Emoji 비활성화
        titleEditText?.disableEmojis()
        goalEditText?.disableEmojis()
        contentEditText?.disableEmojis()

        // 줄바꿈 제한 설정
        contentEditText?.setEnterLimit(10)

        // 제목 EditText 설정
        titleEditText?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!ignoreTextChanges) viewModel.title = s.toString()
                }
            })

            setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    // 포커스를 잃을 때 강제로 텍스트 커밋
                    KeyboardUtils.forceCommitText(this)
                }
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    // 다음 필드로 이동 전에 현재 텍스트 커밋
                    KeyboardUtils.forceCommitText(this)
                    binding.autoCompleteCategory.requestFocus()
                    true
                } else false
            }
        }

        // 목표 EditText 설정
        goalEditText?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!ignoreTextChanges) viewModel.goal = s.toString()
                }
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    // 포커스를 잃을 때 강제로 텍스트 커밋
                    KeyboardUtils.forceCommitText(this)
                }
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    // 다음 필드로 이동 전에 현재 텍스트 커밋
                    KeyboardUtils.forceCommitText(this)
                    contentEditText?.requestFocus()
                    true
                } else false
            }
        }

        // 내용 EditText 설정
        contentEditText?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!ignoreTextChanges) viewModel.description = s.toString()
                }
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    // 포커스를 잃을 때 강제로 텍스트 커밋
                    KeyboardUtils.forceCommitText(this)
                }
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    KeyboardUtils.forceCommitText(this)
                    KeyboardUtils.hideKeyboard(this, requireContext())
                    true
                } else false
            }
        }
    }

    private fun setupCategory() {
        viewModel.fetchCategories()
        viewModel.categoryList.observe(viewLifecycleOwner) { categories ->
            val categoryNames = categories.map { it.categoryName }
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categoryNames)
            binding.autoCompleteCategory.setAdapter(adapter)

            binding.autoCompleteCategory.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    // 현재 포커스가 있는 뷰에서 키보드를 먼저 숨깁니다.
                    val currentFocusView = requireActivity().currentFocus
                    if (currentFocusView != null) {
                        // 포커스를 잃기 전에 모든 텍스트 필드의 내용을 커밋
                        binding.editTextTitle.editText?.let { KeyboardUtils.forceCommitText(it) }
                        binding.editTextGoal.editText?.let { KeyboardUtils.forceCommitText(it) }
                        binding.editTextContent.editText?.let { KeyboardUtils.forceCommitText(it) }
                        KeyboardUtils.hideKeyboard(currentFocusView, requireContext())
                    }

                    // 혹시 이미 열려있는 드롭다운이 있다면 먼저 닫아줍니다.
                    binding.autoCompleteCategory.dismissDropDown()

                    // 즉시 요청하고 짧은 지연 후 표시 (지연시간 단축)
                    binding.autoCompleteCategory.requestFocus()
                    view.postDelayed({
                        binding.autoCompleteCategory.showDropDown()
                    }, 100) // 300ms에서 100ms로 단축

                    // 이벤트를 소비하여 기본 동작(자동 드롭다운)을 막습니다.
                    true
                } else {
                    false
                }
            }

            binding.autoCompleteCategory.setOnItemClickListener { _, _, position, _ ->
                val selected = categoryNames[position]
                viewModel.categoryName = selected
                Log.d("CategorySelect", "선택된 카테고리: $selected")
                KeyboardUtils.hideKeyboard(binding.autoCompleteCategory, requireContext())

                // 카테고리 선택 후 다음 필드로 이동
                binding.editTextGoal.editText?.requestFocus()
            }
        }
    }

    private fun restoreViewModelData() {
        try {
            // 텍스트 변경 감지 일시 중지
            ignoreTextChanges = true

            // 이전 데이터가 있으면 복원
            binding.editTextTitle.editText?.setText(viewModel.title)

            if (viewModel.categoryName.isNotBlank()) {
                binding.autoCompleteCategory.setText(viewModel.categoryName, false)
            }

            binding.editTextGoal.editText?.setText(viewModel.goal)
            binding.editTextContent.editText?.setText(viewModel.description)

            // 커서 위치 조정
            binding.editTextTitle.editText?.setSelection(viewModel.title.length)
            binding.editTextGoal.editText?.setSelection(viewModel.goal.length)
            binding.editTextContent.editText?.setSelection(viewModel.description.length)
        } finally {
            // 텍스트 변경 감지 재개
            ignoreTextChanges = false
        }
    }

    // 프래그먼트 전환 시 ViewModel에 데이터 저장 - 인터페이스로
    override fun saveDataToViewModel(): Boolean {
        // 현재 포커스가 있는 뷰가 EditText라면 강제로 커밋
        val currentFocus = activity?.currentFocus
        if (currentFocus is TextInputEditText) {
            KeyboardUtils.forceCommitText(currentFocus)
        }

        // 데이터 저장은 TextWatcher에서 실시간으로 하므로 다시 할 필요 없음
        // 다만 카테고리는 수동으로 저장
        viewModel.categoryName = binding.autoCompleteCategory.text.toString()

        // 모든 항목 입력 여부 확인
        if (viewModel.title.isBlank() || viewModel.categoryName.isBlank() || viewModel.goal.isBlank() || viewModel.description.isBlank()) {
            Toast.makeText(requireContext(), "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onDestroyView() {
        // 현재 포커스가 있는 EditText 처리
        val currentFocus = activity?.currentFocus
        if (currentFocus is TextInputEditText) {
            KeyboardUtils.forceCommitText(currentFocus)
        }

        super.onDestroyView()
        _binding = null
    }
}