package com.example.second_project.data.model.dto
// 퀴즈 저장을 위해 임시 저장 데이터
data class RegisterTempQuiz(
    var question: String = "",
    var options: MutableList<String> = mutableListOf("", "", ""),
    var correctAnswerIndex: Int = 0 // 0, 1, 2 중 하나
)
