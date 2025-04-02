package com.example.second_project.utils

import android.util.Log
import com.example.second_project.UserSession
import com.example.second_project.network.ApiClient
import com.example.second_project.network.AuthApiService
import retrofit2.Call
import retrofit2.Response

object TokenManager {
    fun refreshToken(): Boolean {
        val refreshToken = UserSession.refreshToken ?: return false
        val authApiService = ApiClient.retrofit.create(AuthApiService::class.java)
        return try {
            // 동기적으로 refresh API 호출 (메인 스레드에서는 호출하지 말아야 함, 인터셉터는 백그라운드에서 실행됨)
            val call: Call<Void> = authApiService.refreshToken(refreshToken)
            val response: Response<Void> = call.execute()
            if (response.isSuccessful) {
                // 새로운 토큰은 헤더에 포함되어 있다고 가정
                val newAccessToken = response.headers()["access"]
                val newRefreshToken = response.headers()["refresh"]
                if (!newAccessToken.isNullOrEmpty() && !newRefreshToken.isNullOrEmpty()) {
                    UserSession.accessToken = newAccessToken
                    UserSession.refreshToken = newRefreshToken
                    Log.d("TokenManager", "Token refreshed: $newAccessToken")
                    true
                } else {
                    Log.e("TokenManager", "New tokens not found in headers")
                    false
                }
            } else {
                Log.e("TokenManager", "Refresh failed. Code: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("TokenManager", "Token refresh exception", e)
            false
        }
    }
}
