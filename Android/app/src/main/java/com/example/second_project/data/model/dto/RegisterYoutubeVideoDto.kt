package com.example.second_project.data.model.dto

data class RegisterYoutubeVideoResponse(
    val items: List<YoutubeVideoItem>
)

data class YoutubeVideoItem(
    val id: String,
    val snippet: Snippet,
    val contentDetails: ContentDetails
)

data class Snippet(
    val title: String
)

data class ContentDetails(
    val duration: String
)