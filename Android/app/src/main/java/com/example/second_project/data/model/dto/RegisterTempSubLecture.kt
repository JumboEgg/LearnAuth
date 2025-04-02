package com.example.second_project.data.model.dto

data class RegisterTempSubLecture(
    var inputUrl: String = "",
    var videoId: String = "", // 실제 등록할 때는 이 값이 url로 들어감
    var videoTitle: String = "",
    var thumbnailUrl: String = "",
    var duration: Int = 0, // 초 단위
    var title: String = "", // 사용자가 작성한 개별 강의 제목
    var isLocked: Boolean = false // 썸네일, 제목이 불러와진 상태인지 여부
)
