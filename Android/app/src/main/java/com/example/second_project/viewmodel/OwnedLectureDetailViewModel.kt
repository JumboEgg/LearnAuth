package com.example.second_project.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.data.repository.LectureDetailRepository

private const val TAG = "OwnedLectureDetailViewM_야옹"
class OwnedLectureDetailViewModel(private val repository: LectureDetailRepository) : ViewModel() {

    private val _lectureDetail = MutableLiveData<LectureDetailResponse>()
    val lectureDetail: LiveData<LectureDetailResponse> get() = _lectureDetail

    fun fetchLectureDetail(lectureId: Int, userId: Int) {
        repository.fetchLectureDetail(lectureId, userId).observeForever {
            Log.d(TAG, "Response received: $lectureId, $userId")
            _lectureDetail.value = it
            Log.d(TAG, "fetchLectureDetail: ${_lectureDetail.value?.data?.title}")
        }
    }

    // 시청 시간 저장 결과
    private val _saveWatchTimeResult = MutableLiveData<Boolean>()
    val saveWatchTimeResult: LiveData<Boolean> get() = _saveWatchTimeResult

    // 마지막 시청 강의 업데이트 결과
    private val _updateLastViewedResult = MutableLiveData<Boolean>()
    val updateLastViewedResult: LiveData<Boolean> get() = _updateLastViewedResult

    // 시청 시간 저장 요청
    fun saveWatchTime(userLectureId: Int, subLectureId: Int, continueWatching: Int, endFlag: Boolean) {
        Log.d("saveWatchTime", "userLectureId: ${userLectureId}, subLectureId: ${subLectureId}, continueWatching: ${continueWatching}, endFlag: ${endFlag}")
        repository.saveWatchTime(userLectureId, subLectureId, continueWatching, endFlag).observeForever {
            _saveWatchTimeResult.postValue(it)
        }
    }

    // 마지막 시청 강의 업데이트 요청
    fun updateLastViewedLecture(userLectureId: Int, subLectureId: Int) {
        repository.updateLastViewedLecture(userLectureId, subLectureId).observeForever {
            _updateLastViewedResult.postValue(it)
        }
    }

}
