package com.example.second_project.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.network.ApiClient
import com.example.second_project.network.LectureApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LectureDetailRepository {

    private val lectureApiService = ApiClient.retrofit.create(LectureApiService::class.java)

    fun fetchLectureDetail(lectureId: Int, userId: Int): LiveData<LectureDetailResponse?> {
        val lectureDetailLiveData = MutableLiveData<LectureDetailResponse?>()
        lectureApiService.getLectureDetail(lectureId, userId).enqueue(object :
            Callback<LectureDetailResponse> {
            override fun onResponse(call: Call<LectureDetailResponse>, response: Response<LectureDetailResponse>) {
                if (response.isSuccessful) {
                    lectureDetailLiveData.postValue(response.body())
                } else {
                    lectureDetailLiveData.postValue(null)
                }
            }

            override fun onFailure(call: Call<LectureDetailResponse>, t: Throwable) {
                lectureDetailLiveData.postValue(null)
            }
        })
        return lectureDetailLiveData
    }

}