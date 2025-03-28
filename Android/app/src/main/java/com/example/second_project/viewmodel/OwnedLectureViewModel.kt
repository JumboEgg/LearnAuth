package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.MyLectureItem


class OwnedLectureViewModel : ViewModel() {

    private val _ownedLectures = MutableLiveData<List<MyLectureItem>>()
    val ownedLectures: LiveData<List<MyLectureItem>> get() = _ownedLectures

    init {
        // 예시 데이터
        _ownedLectures.value = listOf(
            MyLectureItem(
                lectureNumber = 1,
                subject = "데이터",
                title = "8시간 완성 SQLD",
                progress = 30,
                buttoText = "이어서 보기"),
            MyLectureItem(
                lectureNumber = 2,               
                subject = "수학",
                title = "4차 산업혁명과 수학",
                progress = 0,
                buttoText = "수강하기"),
            MyLectureItem(
                lectureNumber = 3,                
                subject = "법률",
                title = "쉬고 유용한 근로기...",
                progress = 100,
                buttoText = "다시보기")
        )
    }
}
