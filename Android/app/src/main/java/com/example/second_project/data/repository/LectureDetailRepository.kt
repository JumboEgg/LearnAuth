package com.example.second_project.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.second_project.data.model.dto.request.SaveTimeRequest
import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.network.ApiClient
import com.example.second_project.network.LectureApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "LectureDetailRepository_야옹"
class LectureDetailRepository {

    private val lectureApiService = ApiClient.retrofit.create(LectureApiService::class.java)

    fun fetchLectureDetail(lectureId: Int, userId: Int): LiveData<LectureDetailResponse?> {
        Log.d(TAG, "fetchLectureDetail: $lectureId, $userId")
        val lectureDetailLiveData = MutableLiveData<LectureDetailResponse?>()
        lectureApiService.getLectureDetail(lectureId, userId).enqueue(object : Callback<LectureDetailResponse> {
            override fun onResponse(call: Call<LectureDetailResponse>, response: Response<LectureDetailResponse>) {
                Log.d(TAG, "Request URL: ${lectureApiService.getLectureDetail(lectureId, userId)}")

                if (response.isSuccessful) {
                    Log.d(TAG, "서버 응답: ${response.body()}")
                    lectureDetailLiveData.postValue(response.body())
                } else {
                    Log.e(TAG, "응답 실패: ${response.code()} - ${response.message()}")
                    lectureDetailLiveData.postValue(null) // 실패 시 null을 처리
                }
            }

            override fun onFailure(call: Call<LectureDetailResponse>, t: Throwable) {
                Log.e(TAG, "네트워크 오류: ${t.message}")
                lectureDetailLiveData.postValue(null) // 네트워크 오류 시 null 처리
            }
        })

        return lectureDetailLiveData
    }

    // 영상 시청 시간 저장
    fun saveWatchTime(
        userLectureId: Int,
        subLectureId: Int,
        continueWatching: Int,
        endFlag: Boolean
    ): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        val requestBody = SaveTimeRequest(continueWatching, endFlag)

        lectureApiService.saveWatchTime(userLectureId, subLectureId, requestBody)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("saveWatchTime", "isSuccessful: ${response.isSuccessful}, code: ${response.code()}")
                    result.postValue(response.isSuccessful)
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    result.postValue(false)
                    Log.d("saveWatchTime", "시청 시간 저장 실패: ${t.message}")
                }
            })
        return result
    }

    // 마지막으로 본 개별 강의 업데이트
    fun updateLastViewedLecture(
        userLectureId: Int,
        subLectureId: Int
    ): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        lectureApiService.updateLastViewedLecture(userLectureId, subLectureId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    result.postValue(response.isSuccessful)
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e(TAG, "마지막 시청 강의 업데이트 실패: ${t.message}")
                    result.postValue(false)
                }
            })
        return result
    }



}