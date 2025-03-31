package com.example.second_project.data.model.dto.request

data class LectureRequest(
    val title: String,
    val categoryName: String,
    val goal: String,
    val description: String,
    val price: Int,
    val ratios: List<Ratio>,
    val subLectures: List<SubLecture>,
    val quizzes: List<Quiz>
)