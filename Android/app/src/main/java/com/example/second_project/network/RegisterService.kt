package com.example.second_project.network

import com.example.second_project.data.model.dto.response.CategoryResponse
import com.example.second_project.data.model.dto.response.common.ApiResponse
import retrofit2.Response

import retrofit2.http.GET

interface RegisterService {
    @GET("api/category")
    suspend fun getCategories(): Response<ApiResponse<List<CategoryResponse>>>
}