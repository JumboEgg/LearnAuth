package com.example.second_project.utils

object YoutubeUtil {
    /** 유튜브 링크에서 영상 ID 추출 */
    //    사용 예시
    //    val videoId = YoutubeUtil.extractVideoId("https://www.youtube.com/watch?v=8ugFEReNVxU")
    fun extractVideoId(url: String): String? {
        val regex = Regex(
            "(?:https?://)?(?:www\\.)?(?:youtube\\.com/(?:watch\\?v=|embed/|v/)|youtu\\.be/)([\\w-]{11})"
        )
        return regex.find(url)?.groups?.get(1)?.value
    }


    /** 썸네일 화질 타입 */
    enum class ThumbnailQuality(val value: String) {
        DEFAULT("default"),       // 120x90
        MEDIUM("mqdefault"),      // 320x180
        HIGH("hqdefault"),        // 480x360
        STANDARD("sddefault"),    // 640x480
        MAX("maxresdefault")      // 1280x720
    }

    /** 영상 ID로 썸네일 URL 생성 */
    //    사용 예시
    //    val thumbnailUrl = YoutubeUtil.getThumbnailUrl(videoId ?: "", YoutubeUtil.ThumbnailQuality.DEFAULT)
    //    videoId 부분에 아이디를 넣으면 됩니다
    fun getThumbnailUrl(videoId: String, quality: ThumbnailQuality = ThumbnailQuality.HIGH): String {
        return "https://i.ytimg.com/vi/$videoId/${quality.value}.jpg"
    }

    // 재생 길이 파싱
    fun parseDuration(isoDuration: String): Int {
        val pattern = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
        val match = pattern.find(isoDuration)

        val hours = match?.groups?.get(1)?.value?.toIntOrNull() ?: 0
        val minutes = match?.groups?.get(2)?.value?.toIntOrNull() ?: 0
        val seconds = match?.groups?.get(3)?.value?.toIntOrNull() ?: 0

        return hours * 3600 + minutes * 60 + seconds
    }



}