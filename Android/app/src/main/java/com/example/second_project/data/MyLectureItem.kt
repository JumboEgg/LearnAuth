package com.example.second_project.data

data class MyLectureItem(
    val lectureNumber: Int,
    val subject : String,
    val title: String,
    val progress: Int = 0,
    val buttoText : String
)
