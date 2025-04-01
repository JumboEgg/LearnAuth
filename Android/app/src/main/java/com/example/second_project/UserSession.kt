package com.example.second_project

import android.content.Context
import android.content.SharedPreferences

object UserSession {
    private const val PREFS_NAME = "user_session_prefs"
    private const val KEY_USER_ID = "user_id"

    private lateinit var preferences: SharedPreferences

    // 앱 시작 시 Application.onCreate()에서 호출하여 초기화합니다.
    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var userId: Int
        get() = preferences.getInt(KEY_USER_ID, 0)
        set(value) {
            preferences.edit().putInt(KEY_USER_ID, value).apply()
        }

    // 로그아웃 등 세션 종료 시 모든 데이터를 초기화합니다.
    fun clear() {
        preferences.edit().clear().apply()
    }
}