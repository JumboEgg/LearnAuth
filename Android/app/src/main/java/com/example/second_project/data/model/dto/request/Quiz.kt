package com.example.second_project.data.model.dto.request

data class Quiz(
    val question: String,
    val quizOptions: List<QuizOption>
)

data class QuizOption(
    val quizOption: String,
    val isCorrect: Boolean
)