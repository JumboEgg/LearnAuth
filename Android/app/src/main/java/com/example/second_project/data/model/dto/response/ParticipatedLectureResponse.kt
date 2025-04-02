package com.example.second_project.data.model.dto.response

data class ParticipatedLectureResponse(
    val timeStamp: String,
    val code: Int,
    val status: String,
    val data: List<ParticipatedLecture>
)

data class ParticipatedLecture(
    val lectureId: Int,
    val categoryName: String,
    val title: String,
    val lecturer: String,
    val isLecturer: Boolean,
    val recentId: Int?,
    val learningRate: Double,
    val lectureUrl: String       // lectureUrl 추가 (필요한 경우)
)
