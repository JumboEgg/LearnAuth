package com.example.second_project.data.model.dto.response

data class QuizResponse(
    val timeStamp: String,
    val code: Int,
    val status: String?,
    val data: List<QuizData>
)

data class QuizData(
    val question: String,
    val quizOptions: List<QuizOptionData>
)

data class QuizOptionData(
    val quizOption: String,
    val isCorrect: Int  // 0 또는 1
) 