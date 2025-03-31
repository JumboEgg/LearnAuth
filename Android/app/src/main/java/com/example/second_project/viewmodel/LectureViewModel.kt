package com.example.second_project.viewmodel

import androidx.lifecycle.*
import com.example.second_project.data.model.dto.request.LectureRequest
import com.example.second_project.data.model.dto.response.common.ApiResponse
import com.example.second_project.data.repository.LectureRepository
import kotlinx.coroutines.launch

class LectureViewModel : ViewModel() {
    private val repository = LectureRepository()

    private val _lectureResponse = MutableLiveData<ApiResponse<Boolean>?>()
    val lectureResponse: LiveData<ApiResponse<Boolean>?> get() = _lectureResponse

}
