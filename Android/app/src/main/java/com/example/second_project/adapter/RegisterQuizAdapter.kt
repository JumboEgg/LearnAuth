package com.example.second_project.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.data.model.dto.RegisterTempQuiz
import com.example.second_project.databinding.ItemRegisterQuizDetailBinding
import com.example.second_project.utils.KeyboardUtils
import com.example.second_project.utils.disableEmojis

class RegisterQuizAdapter(
    private val onDeleteClick: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<RegisterQuizAdapter.ViewHolder>() {
    private val isExpandedList = mutableListOf<Boolean>()
    private val quizList = mutableListOf<RegisterTempQuiz>()

    // TextWatcher 맵 캐싱 (재사용 목적)
    private val textWatcherMap = mutableMapOf<Int, TextWatcher>()

    inner class ViewHolder(private val binding: ItemRegisterQuizDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val quiz = quizList[position]

            // 퀴즈 제목 업데이트
            binding.textQuizIndex.text = "퀴즈 ${position + 1}"

            // 펼침/접힘 상태 설정
            val isExpanded = isExpandedList[position]
            binding.linearToggleArea.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.btnToggleSubLecture.setImageResource(
                if (isExpanded) R.drawable.keyboard_arrow_up_24px
                else R.drawable.keyboard_arrow_down_24px
            )

            // 펼침/접힘 토글 설정
            binding.linearLayoutQuiz.setOnClickListener {
                // 중복 클릭 방지
                binding.linearLayoutQuiz.isClickable = false

                // 현재 포커스된 에디트텍스트의 내용 저장
                saveCurrentFocusedEditText(position)

                // 상태 토글
                val newExpanded = !isExpandedList[position]
                isExpandedList[position] = newExpanded

                // UI 업데이트
                binding.linearToggleArea.visibility = if (newExpanded) View.VISIBLE else View.GONE
                binding.btnToggleSubLecture.setImageResource(
                    if (newExpanded) R.drawable.keyboard_arrow_up_24px
                    else R.drawable.keyboard_arrow_down_24px
                )

                // 약간의 딜레이 후 다시 클릭 가능하게 설정
                binding.linearLayoutQuiz.postDelayed({
                    binding.linearLayoutQuiz.isClickable = true
                }, 100)
            }

            // 모든 EditText 입력 성능 최적화
            optimizeEditTexts(binding)

            // TextWatcher 설정 전 기존 리스너 제거
            removeAndSetupTextWatchers(binding, position)

            // 텍스트 설정
            binding.textInputEditText1.setText(quiz.question)
            binding.textInputEditText2.setText(quiz.options[0])
            binding.textInputEditText3.setText(quiz.options[1])
            binding.textInputEditText4.setText(quiz.options[2])

            // 정답 선택 표시 업데이트
            updateAnswerImages(binding, quiz.correctAnswerIndex)

            // 정답 선택 클릭 리스너 설정
            setupAnswerSelectionListeners(binding, position)

            // 삭제 버튼 - 3개 이상일 때만 표시
            binding.btnDeleteSubLecture.visibility =
                if (quizList.size > 3) View.VISIBLE else View.GONE

            binding.btnDeleteSubLecture.setOnClickListener {
                if (quizList.size > 3) {
                    onDeleteClick?.invoke(position) ?: run {
                        removeQuiz(position)
                    }
                }
            }
        }

        // EditText 입력 성능 최적화
        private fun optimizeEditTexts(binding: ItemRegisterQuizDetailBinding) {
            // 입력 지연 최소화
            binding.textInputEditText1.setTextIsSelectable(true)
            binding.textInputEditText2.setTextIsSelectable(true)
            binding.textInputEditText3.setTextIsSelectable(true)
            binding.textInputEditText4.setTextIsSelectable(true)

            // 이모지 비활성화
            binding.textInputEditText1.disableEmojis()
            binding.textInputEditText2.disableEmojis()
            binding.textInputEditText3.disableEmojis()
            binding.textInputEditText4.disableEmojis()

            // 힌트 애니메이션 비활성화 (성능 향상)
            binding.textInputEditText1.freezesText = false
            binding.textInputEditText2.freezesText = false
            binding.textInputEditText3.freezesText = false
            binding.textInputEditText4.freezesText = false
        }

        // TextWatcher 제거 및 설정
        private fun removeAndSetupTextWatchers(
            binding: ItemRegisterQuizDetailBinding,
            position: Int
        ) {
            // 기존 리스너 제거
            binding.textInputEditText1.removeTextChangedListener(textWatcherMap[position * 10])
            binding.textInputEditText2.removeTextChangedListener(textWatcherMap[position * 10 + 1])
            binding.textInputEditText3.removeTextChangedListener(textWatcherMap[position * 10 + 2])
            binding.textInputEditText4.removeTextChangedListener(textWatcherMap[position * 10 + 3])

            // 새 리스너 생성 및 설정
            setupTextWatcher(binding.textInputEditText1, position * 10, position) { text ->
                quizList[position].question = text
            }

            setupTextWatcher(binding.textInputEditText2, position * 10 + 1, position) { text ->
                quizList[position].options[0] = text
            }

            setupTextWatcher(binding.textInputEditText3, position * 10 + 2, position) { text ->
                quizList[position].options[1] = text
            }

            setupTextWatcher(binding.textInputEditText4, position * 10 + 3, position) { text ->
                quizList[position].options[2] = text
            }
        }

        // TextWatcher 설정 메서드
        private fun setupTextWatcher(
            editText: com.google.android.material.textfield.TextInputEditText,
            key: Int,
            position: Int,
            onTextChanged: (String) -> Unit
        ) {
            // 기존 리스너 제거
            val existingWatcher = textWatcherMap[key]
            if (existingWatcher != null) {
                editText.removeTextChangedListener(existingWatcher)
            }

            // 새 리스너 생성
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (position < quizList.size) {
                        onTextChanged(s?.toString() ?: "")
                    }
                }
            }

            // 맵에 저장 및 EditText에 리스너 설정
            textWatcherMap[key] = watcher
            editText.addTextChangedListener(watcher)
        }

        // 현재 포커스된 EditText의 내용 저장
        private fun saveCurrentFocusedEditText(position: Int) {
            val focusedView = binding.root.findFocus()
            if (focusedView is com.google.android.material.textfield.TextInputEditText) {
                val text = focusedView.text.toString()

                // 어떤 필드인지 확인하고 데이터 저장
                when (focusedView.id) {
                    R.id.textInputEditText1 -> quizList[position].question = text
                    R.id.textInputEditText2 -> quizList[position].options[0] = text
                    R.id.textInputEditText3 -> quizList[position].options[1] = text
                    R.id.textInputEditText4 -> quizList[position].options[2] = text
                }

                // 강제로 텍스트 커밋 처리
                KeyboardUtils.forceCommitText(focusedView)

                // 포커스 해제 및 키보드 숨김
                KeyboardUtils.clearFocusAndHideKeyboard(focusedView)
            }
        }

        // 정답 선택 리스너 설정
        private fun setupAnswerSelectionListeners(
            binding: ItemRegisterQuizDetailBinding,
            position: Int
        ) {
            val answerContainers = listOf(binding.isAnswer1, binding.isAnswer2, binding.isAnswer3)

            answerContainers.forEachIndexed { index, container ->
                container.setOnClickListener {
                    // 중복 클릭 방지
                    container.isClickable = false

                    // 현재 포커스된 에디트텍스트의 내용 저장
                    saveCurrentFocusedEditText(position)

                    // 데이터 업데이트
                    quizList[position].correctAnswerIndex = index

                    // 이미지만 업데이트하여 스크롤 점프 방지
                    updateAnswerImages(binding, index)

                    // 딜레이 후 클릭 가능하게
                    container.postDelayed({ container.isClickable = true }, 100)
                }
            }
        }
    }

    // 이미지 업데이트 메서드
    private fun updateAnswerImages(binding: ItemRegisterQuizDetailBinding, selectedIndex: Int) {
        val imageViews = listOf(
            binding.isAnswer1.getChildAt(0) as? ImageView,
            binding.isAnswer2.getChildAt(0) as? ImageView,
            binding.isAnswer3.getChildAt(0) as? ImageView
        )

        imageViews.forEachIndexed { index, imageView ->
            imageView?.setImageResource(
                if (index == selectedIndex) R.drawable.ic_process_checked
                else R.drawable.ic_process_unchecked
            )
        }
    }

    // RecyclerView 기본 메서드들
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegisterQuizDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = quizList.size

    // 퀴즈 추가 메서드
    fun addQuiz() {
        quizList.add(RegisterTempQuiz())
        isExpandedList.add(true)
        notifyItemInserted(quizList.size - 1)
    }

    // 퀴즈 삭제 메서드
    fun removeQuiz(position: Int) {
        if (position in 0 until quizList.size && quizList.size > 3) {
            // TextWatcher 정리
            for (i in 0..3) {
                textWatcherMap.remove(position * 10 + i)
            }

            quizList.removeAt(position)
            isExpandedList.removeAt(position)
            notifyItemRemoved(position)

            // 남은 아이템 인덱스 업데이트
            notifyItemRangeChanged(position, quizList.size - position)
        }
    }

    // 내부 데이터 설정 메서드
    fun setItems(tempList: List<RegisterTempQuiz>) {
        quizList.clear()
        quizList.addAll(tempList)
        isExpandedList.clear()
        isExpandedList.addAll(List(tempList.size) { true })

        // TextWatcher 맵 초기화
        textWatcherMap.clear()

        notifyDataSetChanged()
    }

    fun getItems(): List<RegisterTempQuiz> = quizList.toList()

    fun getItemAt(position: Int): RegisterTempQuiz? {
        return if (position in quizList.indices) quizList[position] else null
    }

    fun updateItem(position: Int, item: RegisterTempQuiz) {
        if (position in quizList.indices) {
            quizList[position] = item
            notifyItemChanged(position)
        }
    }

    fun removeAll() {
        quizList.clear()
        isExpandedList.clear()
        textWatcherMap.clear()
        notifyDataSetChanged()
    }
}