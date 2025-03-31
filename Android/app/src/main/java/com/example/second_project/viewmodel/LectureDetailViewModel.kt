package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.data.repository.LectureRepository

class LectureDetailViewModel(private val repository: LectureDetailRepository) : ViewModel() {

    private val _lectureDetail = MutableLiveData<LectureDetailResponse>()
    val lectureDetail: LiveData<LectureDetailResponse> get() = _lectureDetail

    fun fetchLectureDetail(lectureId: Int, userId: Int) {
        repository.fetchLectureDetail(lectureId, userId).observeForever {
            _lectureDetail.value = it
        }
    }
}
