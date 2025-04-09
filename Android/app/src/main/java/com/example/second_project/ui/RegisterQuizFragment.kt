package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.adapter.RegisterQuizAdapter
import com.example.second_project.data.model.dto.RegisterTempQuiz
import com.example.second_project.databinding.FragmentRegisterQuizBinding
import com.example.second_project.interfaces.RegisterStepSavable
import com.example.second_project.utils.ApiKeyProvider
import com.example.second_project.utils.KeyboardUtils
import com.example.second_project.viewmodel.IpfsUploadState
import com.example.second_project.viewmodel.RegisterViewModel

class RegisterQuizFragment : Fragment(), RegisterStepSavable {
    private var _binding: FragmentRegisterQuizBinding? = null
    private val binding get() = _binding!!
    private lateinit var quizAdapter: RegisterQuizAdapter
    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 리사이클러뷰 세팅 - 주요 개선 부분
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.isSmoothScrollbarEnabled = true

        // 리사이클러뷰 스크롤 개선 설정
        binding.recyclerQuiz.apply {
            this.layoutManager = layoutManager
            itemAnimator = null // 애니메이션 비활성화로 스크롤 점프 방지
            setHasFixedSize(false) // 동적 크기 변화 허용
            isNestedScrollingEnabled = false // 중첩 스크롤 비활성화

            // 스크롤 리스너 추가 - 포커스 변경 시 키보드 숨김
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        // 스크롤 시작 시 포커스 해제 및 키보드 숨김
                        recyclerView.findFocus()?.let { focusedView ->
                            KeyboardUtils.clearFocusAndHideKeyboard(focusedView)
                        }
                    }
                }
            })
        }

        // 어댑터 초기화 및 삭제 콜백 개선
        quizAdapter = RegisterQuizAdapter(
            onDeleteClick = { position ->
                if (quizAdapter.getItems().size > 3) {
                    quizAdapter.removeQuiz(position)
                } else {
                    Toast.makeText(requireContext(), "퀴즈는 최소 3개 이상 등록해야 합니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )
        binding.recyclerQuiz.adapter = quizAdapter

        // ✅ 항상 3개는 유지, 작성한 내용 있으면 유지
        val tempList = viewModel.tempQuizzes.toMutableList()
        if (tempList.isEmpty()) {
            // 처음 진입 시 기본 3개 퀴즈 추가
            repeat(3) {
                tempList.add(RegisterTempQuiz())
            }
        } else {
            // 기존 데이터가 3개 미만이면 추가
            repeat(3 - tempList.size) {
                tempList.add(RegisterTempQuiz())
            }
        }
        quizAdapter.setItems(tempList)

        // 리사이클러뷰 표시
        binding.recyclerQuiz.visibility = View.VISIBLE

        binding.btnDone.setOnClickListener {
            // 키보드 숨기기
            requireActivity().currentFocus?.let { focusView ->
                KeyboardUtils.clearFocusAndHideKeyboard(focusView)
            }

            if (!saveDataToViewModel()) return@setOnClickListener

            // ✅ 최종 변환
            viewModel.convertTempToFinalSubLectures()
            viewModel.convertTempToFinalQuizzes()

            // ✅ 유효성 체크
            if (!viewModel.isValid()) {
                Toast.makeText(requireContext(), "모든 항목을 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 파일이 선택되었는지 확인
            if (viewModel.selectedLectureFileUri == null) {
                Toast.makeText(requireContext(), "강의 자료 파일을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // IPFS 업로드 진행
            uploadFileToIpfs()
        }

        // IPFS 업로드 상태 관찰
        viewModel.ipfsUploadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is IpfsUploadState.Loading -> {
                    binding.btnDone.isEnabled = false
                    binding.btnDone.text = "업로드 중..."
                    (parentFragment as? RegisterMainFragment)?.also {
                        Log.d("RegisterQuizFragment", "✅ showGlobalLoading 호출됨")
                        it.showGlobalLoading()
                    }
                }

                is IpfsUploadState.Success -> {
                    binding.btnDone.isEnabled = true
                    binding.btnDone.text = "강의 등록 완료하기"
                    // IPFS 업로드 성공 후 강의 등록 진행
                    registerLecture()
                }

                is IpfsUploadState.Error -> {
                    binding.btnDone.isEnabled = true
                    binding.btnDone.text = "강의 등록 완료하기"
                    (parentFragment as? RegisterMainFragment)?.hideGlobalLoading()
                    Toast.makeText(
                        requireContext(),
                        "파일 업로드 실패: ${state.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * IPFS에 파일을 업로드합니다.
     */
    private fun uploadFileToIpfs() {
        val pinataApiKey = ApiKeyProvider.getPinataApiKey()
        if (pinataApiKey.isBlank()) {
            Toast.makeText(requireContext(), "API 키를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.uploadFileToIpfs(
            context = requireContext(),
            apiKey = pinataApiKey,
            onSuccess = { /* 성공 콜백 */ },
            onError = { /* 에러 콜백 */ }
        )
    }

    /**
     * 강의를 등록합니다.
     */
    private fun registerLecture() {
        viewModel.registerLecture(
            onSuccess = {
                if (isAdded) {
                    Toast.makeText(requireContext(), "강의가 성공적으로 등록되었습니다!", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.reset()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            },
            onError = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                (parentFragment as? RegisterMainFragment)?.hideGlobalLoading()
            }
        )
    }

    override fun saveDataToViewModel(): Boolean {
        val tempQuizzes = quizAdapter.getItems()

        // 👇 유효성 검사
        if (tempQuizzes.size < 3) {
            Toast.makeText(requireContext(), "퀴즈는 최소 3개 이상 등록해야 합니다.", Toast.LENGTH_SHORT).show()
            return false
        }

        var isValid = true
        tempQuizzes.forEachIndexed { index, quiz ->
            val question = quiz.question.trim()
            val options = quiz.options.map { it.trim() }

            if (question.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "${index + 1}번째 퀴즈의 문제를 입력해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
                isValid = false
                return@forEachIndexed
            }

            if (options.any { it.isBlank() }) {
                Toast.makeText(
                    requireContext(),
                    "${index + 1}번째 퀴즈의 보기 항목을 모두 입력해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
                isValid = false
                return@forEachIndexed
            }

            if (quiz.correctAnswerIndex !in 0..2) {
                Toast.makeText(
                    requireContext(),
                    "${index + 1}번째 퀴즈의 정답을 선택해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
                isValid = false
                return@forEachIndexed
            }

            // trim 적용 후 저장
            quiz.question = question
            quiz.options = options.toMutableList()
        }

        if (!isValid) return false

        viewModel.tempQuizzes.clear()
        viewModel.tempQuizzes.addAll(quizAdapter.getItems())
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}