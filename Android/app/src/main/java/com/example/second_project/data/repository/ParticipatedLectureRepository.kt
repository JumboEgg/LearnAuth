package com.example.second_project.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.second_project.data.model.dto.response.ParticipatedLectureResponse
import com.example.second_project.data.model.dto.response.ParticipatedLecture
import com.example.second_project.network.ApiClient
import com.example.second_project.network.LectureApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ParticipatedLectureRepository {
    private val apiService = ApiClient.retrofit.create(LectureApiService::class.java)

    fun fetchParticipatedLectures(userId: Int): LiveData<List<ParticipatedLecture>> {
        val data = MutableLiveData<List<ParticipatedLecture>>()
        apiService.getParticipatedLectures(userId).enqueue(object : Callback<ParticipatedLectureResponse> {
            override fun onResponse(
                call: Call<ParticipatedLectureResponse>,
                response: Response<ParticipatedLectureResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    data.value = response.body()!!.data
                } else {
                    data.value = emptyList()
                }
            }

            override fun onFailure(call: Call<ParticipatedLectureResponse>, t: Throwable) {
                data.value = emptyList()
            }
        })
        return data
    }
}
