package com.example.second_project.network

import com.example.second_project.UserSession
import com.example.second_project.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        var response = chain.proceed(originalRequest)

        if (response.code == 401 || response.code == 400) {
            response.close()
            // 401 에러 발생 시 토큰 갱신 시도
            val tokenRefreshed = TokenManager.refreshToken()
            if (tokenRefreshed) {
                // 토큰 갱신 성공 후, 새 토큰을 포함하여 원래 요청 재시도
                val newAccessToken = UserSession.accessToken
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
                response = chain.proceed(newRequest)
            }
        }
        return response
    }
}
