package com.example.second_project.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.second_project.data.model.dto.response.LectureSearchData
import com.example.second_project.data.model.dto.response.LectureSearchResponse
import com.example.second_project.network.ApiClient
import com.example.second_project.network.LectureApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object SearchRepository {
    private val apiService = ApiClient.retrofit.create(LectureApiService::class.java)

    fun searchLectures(keyword: String, page: Int): LiveData<LectureSearchData?> {
        val data = MutableLiveData<LectureSearchData?>()
        apiService.getSearchLectures(keyword, page).enqueue(object : Callback<LectureSearchResponse> {
            override fun onResponse(
                call: Call<LectureSearchResponse>,
                response: Response<LectureSearchResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    data.value = response.body()!!.data
                } else {
                    data.value = null
                }
            }

            override fun onFailure(call: Call<LectureSearchResponse>, t: Throwable) {
                data.value = null
            }
        })
        return data
    }
}