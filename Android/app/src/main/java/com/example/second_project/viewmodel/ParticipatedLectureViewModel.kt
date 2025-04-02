package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.response.ParticipatedLecture
import com.example.second_project.data.repository.ParticipatedLectureRepository

class ParticipatedLectureViewModel : ViewModel() {

    private val _participatedLectures = MutableLiveData<List<ParticipatedLecture>>()
    val participatedLectures: LiveData<List<ParticipatedLecture>> = _participatedLectures

    fun loadParticipatedLectures(userId: Int) {
        ParticipatedLectureRepository.fetchParticipatedLectures(userId).observeForever { lectures ->
            _participatedLectures.value = lectures
        }
    }
}
