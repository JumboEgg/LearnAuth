package com.example.second_project.viewmodel

import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.request.Quiz
import com.example.second_project.data.model.dto.request.Ratio
import com.example.second_project.data.model.dto.request.RegisterLectureRequest
import com.example.second_project.data.model.dto.request.SubLecture

class RegisterViewModel : ViewModel(){

    var title: String = ""
    var categoryName: String = ""
    var goal: String = ""
    var description: String = ""
    var price: Int = 0

    val ratios = mutableListOf<Ratio>() // 강의자/참여자
    val subLectures = mutableListOf<SubLecture>()
    val quizzes = mutableListOf<Quiz>()

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