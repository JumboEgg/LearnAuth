package com.example.second_project.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object YoutubeApiClient {
    private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

    val youtubeService: YoutubeService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YoutubeService::class.java)
    }

}