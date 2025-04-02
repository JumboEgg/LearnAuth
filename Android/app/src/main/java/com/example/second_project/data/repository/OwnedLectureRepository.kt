package com.example.second_project.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.second_project.data.model.dto.response.OwnedLectureResponse
import com.example.second_project.data.model.dto.response.OwnedLecture
import com.example.second_project.network.ApiClient
import com.example.second_project.network.LectureApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object OwnedLectureRepository {

    private val apiService = ApiClient.retrofit.create(LectureApiService::class.java)

    fun fetchOwnedLectures(userId: Int): LiveData<List<OwnedLecture>> {
        val data = MutableLiveData<List<OwnedLecture>>()
        apiService.getOwnedLectures(userId).enqueue(object : Callback<OwnedLectureResponse> {
            override fun onResponse(
                call: Call<OwnedLectureResponse>,
                response: Response<OwnedLectureResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    data.value = response.body()!!.data
                } else {
                    data.value = emptyList()
                }
            }

            override fun onFailure(call: Call<OwnedLectureResponse>, t: Throwable) {
                data.value = emptyList()
            }
        })
        return data
    }
}
