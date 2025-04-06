package com.example.second_project

import android.content.Context
import android.content.SharedPreferences
import com.example.second_project.blockchain.BlockChainManager
import java.io.File

object UserSession {
    private const val PREFS_NAME = "user_session_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_USER_NAME = "name"
    private const val KEY_WALLET_PATH = "wallet_path"
    private const val KEY_WALLET_PASSWORD = "wallet_password"

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

    var accessToken: String?
        get() = preferences.getString(KEY_ACCESS_TOKEN, null)
        set(value) {
            preferences.edit().putString(KEY_ACCESS_TOKEN, value).apply()
        }

    var refreshToken: String?
        get() = preferences.getString(KEY_REFRESH_TOKEN, null)
        set(value) {
            preferences.edit().putString(KEY_REFRESH_TOKEN, value).apply()
        }

    var nickname: String?
        get() = preferences.getString(KEY_NICKNAME, "")
        set(value) {
            preferences.edit().putString(KEY_NICKNAME, value).apply()
        }

    var name: String?
        get() = preferences.getString(KEY_USER_NAME, "")
        set(value) {
            preferences.edit().putString(KEY_USER_NAME, value).apply()
        }

    var walletFilePath: String?
        get() = preferences.getString(KEY_WALLET_PATH, null)
        set(value) {
            preferences.edit().putString(KEY_WALLET_PATH, value).apply()
        }

    var walletPassword: String?
        get() = preferences.getString(KEY_WALLET_PASSWORD, null)
        set(value) {
            preferences.edit().putString(KEY_WALLET_PASSWORD, value).apply()
        }

    // ✅ 세션 기반으로 BlockChainManager 생성
    fun getBlockchainManagerIfAvailable(context: Context): BlockChainManager? {
        val path = walletFilePath
        val password = walletPassword
        if (!path.isNullOrEmpty() && !password.isNullOrEmpty()) {
            val walletFile = File(context.filesDir, path)
            return if (walletFile.exists()) {
                BlockChainManager(password, walletFile)
            } else {
                null // ⚠️ 파일이 없으면 null
            }
        }
        return null
    }



    // 로그아웃 등 세션 종료 시 모든 데이터를 초기화합니다.
    fun clear() {
        preferences.edit().clear().apply()
    }
}
