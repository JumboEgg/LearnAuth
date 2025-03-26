package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.MyParticiItem

class ParticipatedLectureViewModel : ViewModel() {

    private val _participatedLectures = MutableLiveData<List<MyParticiItem>>()
    val participatedLectures: LiveData<List<MyParticiItem>> get() = _participatedLectures

    init {
        // 예시 데이터
        _participatedLectures.value = listOf(
            MyParticiItem(
                lectureNumber = 1,
                subject = "법률",
                title = "쉽고 유용한 근로기...",
                true,
                myName = "눈감차"
            ),
            MyParticiItem(
                lectureNumber = 2,
                subject = "체육",
                title = "밥테일의 눈빛 보내기",
                false,
                myName = "눈감차"
            ),
        )
    }
}
