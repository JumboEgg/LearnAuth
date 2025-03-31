package com.example.second_project.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.second_project.data.model.dto.request.Quiz
import com.example.second_project.data.model.dto.request.Ratio
import com.example.second_project.data.model.dto.request.RegisterLectureRequest
import com.example.second_project.data.model.dto.request.SubLecture
import com.example.second_project.data.model.dto.response.CategoryResponse
import com.example.second_project.network.ApiClient.registerService
import kotlinx.coroutines.launch
import retrofit2.Response


class RegisterViewModel : ViewModel(){

    // 등록 강의 카테고리 드롭다운 메뉴
    private val _categoryList = MutableLiveData<List<CategoryResponse>>()
    val categoryList: LiveData<List<CategoryResponse>> = _categoryList

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

    fun isValid(): Boolean {
        return title.isNotBlank()
                && categoryName.isNotBlank()
                && goal.isNotBlank()
                && description.isNotBlank()
                && price > 0
                && ratios.isNotEmpty()
                && ratios.any { it.lecturer }
                && subLectures.isNotEmpty()
                && quizzes.size >= 3
                && quizzes.all { quiz ->
            quiz.quizOptions.size == 3 && quiz.quizOptions.count { it.isCorrect } == 1
        }
    }


}