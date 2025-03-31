package com.example.second_project.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.data.repository.LectureDetailRepository

private const val TAG = "LectureDetailViewModel_야옹"
class LectureDetailViewModel(private val repository: LectureDetailRepository) : ViewModel() {

    private val _lectureDetail = MutableLiveData<LectureDetailResponse>()
    val lectureDetail: LiveData<LectureDetailResponse> get() = _lectureDetail

    fun fetchLectureDetail(lectureId: Int, userId: Int) {
        repository.fetchLectureDetail(lectureId, userId).observeForever {
            Log.d(TAG, "Response received: $lectureId, $userId")
            _lectureDetail.value = it
            Log.d(TAG, "fetchLectureDetail: ${_lectureDetail.value?.data?.title}")
        }
    }
}
