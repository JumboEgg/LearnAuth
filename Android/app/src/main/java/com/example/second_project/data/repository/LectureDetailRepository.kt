package com.example.second_project.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

}