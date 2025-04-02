package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.response.OwnedLecture
import com.example.second_project.data.repository.OwnedLectureRepository

class OwnedLectureViewModel : ViewModel() {

    private val _ownedLectures = MutableLiveData<List<OwnedLecture>>()
    val ownedLectures: LiveData<List<OwnedLecture>> get() = _ownedLectures

    fun loadOwnedLectures(userId: Int) {
        OwnedLectureRepository.fetchOwnedLectures(userId).observeForever { lectures ->
            _ownedLectures.value = lectures
        }
    }
}
