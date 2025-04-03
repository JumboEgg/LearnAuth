package com.example.second_project.data.model.dto.response

data class LectureDetailResponse(
    val timeStamp: String,
    val code: Int,
    val status: String?,
    val data: LectureData
)

data class LectureData(
    val lectureId: Int,
    val userLectureId: Int,
    val title: String,
    val categoryName: String,
    val goal: String,
    val description: String,
    val price: Int,
    val lecturer: String?,
    val lectureUrl: String,
    val recentLectureId: Int,
    val studentCount: Int,
    val owned: Boolean,
    val certificate: Boolean,
    val subLectures: List<SubLecture>
)

data class SubLecture(
    val subLectureId: Int,
    val subLectureTitle: String,
    val lectureUrl: String,
    val lectureLength: Int,
    val lectureOrder: Int,
    val continueWatching: Int,
    val endFlag: Boolean
)
