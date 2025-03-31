package com.example.second_project.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.second_project.data.model.dto.request.Lecture
import com.example.second_project.data.model.dto.response.LectureResponse
import com.example.second_project.network.ApiClient
import com.example.second_project.network.LectureApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LectureRepository {
    private val lectureApiService =
        ApiClient.retrofit.create(LectureApiService::class.java)

    fun fetchLectures(category: String, page: Int): LiveData<List<Lecture>> {
        val lecturesLiveData = MutableLiveData<List<Lecture>>()
        lectureApiService.getLectures(category, page).enqueue(object : Callback<LectureResponse> {
            override fun onResponse(
                call: Call<LectureResponse>,
                response: Response<LectureResponse>
            ) {
                if (response.isSuccessful) {
                    lecturesLiveData.postValue(response.body()?.data)
                } else {
                    // 에러 발생 시 빈 리스트를 반환하거나 별도 에러 처리 가능
                    lecturesLiveData.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<LectureResponse>, t: Throwable) {
                // 네트워크 오류 등 발생 시 빈 리스트를 반환
                lecturesLiveData.postValue(emptyList())
            }
        })
        return lecturesLiveData
    }
}