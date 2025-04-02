package com.example.second_project.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.second_project.data.model.dto.RegisterTempQuiz
import com.example.second_project.data.model.dto.RegisterTempSubLecture
import com.example.second_project.data.model.dto.request.Quiz
import com.example.second_project.data.model.dto.request.Ratio
import com.example.second_project.data.model.dto.request.RegisterLectureRequest
import com.example.second_project.data.model.dto.request.SubLecture
import com.example.second_project.data.model.dto.response.CategoryResponse
import com.example.second_project.network.ApiClient.registerService
import com.example.second_project.network.YoutubeApiClient
import com.example.second_project.utils.YoutubeUtil
import kotlinx.coroutines.launch
import com.example.second_project.data.model.dto.request.QuizOption



class RegisterViewModel : ViewModel(){

    // 등록 강의 카테고리 드롭다운 메뉴
    private val _categoryList = MutableLiveData<List<CategoryResponse>>()
    val categoryList: LiveData<List<CategoryResponse>> = _categoryList
    val tempSubLectures = mutableListOf<RegisterTempSubLecture>()

    fun fetchCategories() {
        viewModelScope.launch {
            runCatching {
                registerService.getCategories()
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    _categoryList.value = response.body()?.data ?: emptyList()
                } else {
                    Log.e("fetchCategories", "fetchCategories Error")
                }
            }.onFailure { throwable ->
                Log.e("fetchCategories", "❗예외 발생: ${throwable.message}")
            }
        }
    }


    var title: String = ""
    var categoryName: String = ""
    var goal: String = ""
    var description: String = ""
    var price: Int = 0

    val ratios = mutableListOf<Ratio>() // 강의자/참여자
    val subLectures = mutableListOf<SubLecture>()
    val quizzes = mutableListOf<Quiz>()

    // 파일 정보
    var selectedLectureFileName: String? = null
    var selectedLectureFileUri: Uri? = null

    fun reset() {
        title = ""
        categoryName = ""
        goal = ""
        description = ""
        price = 0
        ratios.clear()
        subLectures.clear()
        quizzes.clear()
        tempSubLectures.clear()
        tempQuizzes.clear()
    }

    fun toRequest(): RegisterLectureRequest {
        return RegisterLectureRequest(
            title = title,
            categoryName = categoryName,
            goal = goal,
            description = description,
            price = price,
            ratios = ratios,
            subLectures = subLectures,
            quizzes = quizzes
        )
    }

//    fun isValid(): Boolean {
//        return title.isNotBlank()
//                && categoryName.isNotBlank()
//                && goal.isNotBlank()
//                && description.isNotBlank()
//                && price >= 0
//                && ratios.isNotEmpty()
//                && ratios.any { it.lecturer }
//                && subLectures.isNotEmpty()
//                && quizzes.size >= 3
//                && quizzes.all { quiz ->
//            quiz.quizOptions.size == 3 && quiz.quizOptions.count { it.isCorrect } == 1
//        }
//    }

    fun isValid(): Boolean {
        if (title.isBlank()) {
            Log.e("isValid", "제목이 비어있음")
            return false
        }
        if (categoryName.isBlank()) {
            Log.e("isValid", "카테고리명이 비어있음")
            return false
        }
        if (goal.isBlank()) {
            Log.e("isValid", "목표가 비어있음")
            return false
        }
        if (description.isBlank()) {
            Log.e("isValid", "설명이 비어있음")
            return false
        }
        if (price < 0) {
            Log.e("isValid", "가격이 0 이하임")
            return false
        }
        if (ratios.isEmpty()) {
            Log.e("isValid", "참여자 비율 정보 없음")
            return false
        }
        if (!ratios.any { it.lecturer }) {
            Log.e("isValid", "강의자로 지정된 사람이 없음")
            return false
        }
        if (subLectures.isEmpty()) {
            Log.e("isValid", "개별 강의 정보 없음")
            return false
        }
        if (quizzes.size < 3) {
            Log.e("isValid", "퀴즈가 3개 미만임")
            return false
        }
        if (!quizzes.all { it.quizOptions.size == 3 }) {
            Log.e("isValid", "퀴즈 중 보기 3개가 아닌 항목 있음")
            return false
        }
        if (!quizzes.all { quiz -> quiz.quizOptions.count { it.isCorrect } == 1 }) {
            Log.e("isValid", "퀴즈 중 정답이 하나가 아닌 항목 있음")
            return false
        }

        return true
    }


    fun fetchYoutubeMetaData(
        videoId: String,
        apiKey: String,
        onResult: (title: String, durationSeconds: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                YoutubeApiClient.youtubeService.getVideoInfo(
                    videoId = videoId,
                    apiKey = apiKey,
                    fields = "items(id,snippet(title),contentDetails(duration))"
                )
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val video = response.body()?.items?.firstOrNull()
                    if (video != null) {
                        val title = video.snippet.title
                        val durationIso = video.contentDetails.duration
                        val durationSeconds = YoutubeUtil.parseDuration(durationIso)
                        onResult(title, durationSeconds)
                    } else {
                        onError("영상 정보를 찾을 수 없습니다.")
                    }
                } else {
                    onError("API 응답 오류: ${response.code()}")
                }
            }.onFailure { throwable ->
                onError("네트워크 요청 실패: ${throwable.message}")
            }
        }
    }

    fun convertTempToFinalSubLectures() {
        subLectures.clear()
        subLectures.addAll(
            tempSubLectures.map {
                SubLecture(
                    subLectureTitle = it.title,
                    subLectureUrl = it.videoId,
                    subLectureLength = it.duration
                )
            }
        )
    }

    val tempQuizzes = mutableListOf<RegisterTempQuiz>()

    fun convertTempToFinalQuizzes() {
        quizzes.clear()
        quizzes.addAll(
            tempQuizzes.map { temp ->
                Quiz(
                    question = temp.question,
                    quizOptions = temp.options.mapIndexed { index, optionText ->
                        QuizOption(
                            quizOption = optionText,
                            isCorrect = index == temp.correctAnswerIndex
                        )
                    }
                )
            }
        )
    }

    fun registerLecture(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val request = toRequest()

            // ✅ 요청 본문 로그 출력
            Log.d("registerLecture", "요청 데이터: $request")
            runCatching {
                registerService.registerLecture(toRequest())
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val msg = "등록 실패: ${response.code()} - ${response.message()}"
                    Log.e("registerLecture", msg)
                    onError(msg)
                }
            }.onFailure { throwable ->
                val msg = "네트워크 오류: ${throwable.message}"
                Log.e("registerLecture", msg, throwable)
                onError(msg)
            }
        }
    }









}