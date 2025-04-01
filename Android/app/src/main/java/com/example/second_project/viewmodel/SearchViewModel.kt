package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.request.Lecture
import com.example.second_project.data.repository.LectureRepository

class SearchViewModel : ViewModel() {

    private val repository = LectureRepository()

    private val _lectures = MutableLiveData<List<Lecture>>()
    val lectures: LiveData<List<Lecture>> = _lectures

    private val _text = MutableLiveData("검색화면입니당")
    val text: LiveData<String> = _text

    // API 호출하여 강의 목록을 가져옵니다.
    fun loadLectures(categoryId: Int, page: Int) {
        repository.fetchLectures(categoryId, page).observeForever { lectureList ->
            _lectures.value = lectureList
        }
    }

    // observeForever 사용 시 ViewModel 소멸 시 리소스 해제 고려 필요
    override fun onCleared() {
        super.onCleared()
        // 필요하다면 Repository에서 등록한 Observer를 제거하는 로직 추가
    }
}
