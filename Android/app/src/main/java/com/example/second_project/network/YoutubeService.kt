package com.example.second_project.network

import com.example.second_project.data.model.dto.RegisterYoutubeVideoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface YoutubeService {
    @GET("videos")
    suspend fun getVideoInfo(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("fields") fields: String = "items(snippet(title),contentDetails(duration))",
        @Query("id") videoId: String,
        @Query("key") apiKey: String
    ): Response<RegisterYoutubeVideoResponse>
}