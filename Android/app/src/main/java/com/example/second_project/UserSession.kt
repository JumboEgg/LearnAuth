package com.example.second_project

import android.content.Context
import android.content.SharedPreferences
import com.example.second_project.blockchain.BlockChainManager
import java.io.File
import java.math.BigInteger

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

    // 마지막으로 알려진 잔액 (메모리에만 저장, 영구 저장은 불필요)
    var lastKnownBalance: BigInteger? = null

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
            // 주어진 경로에 파일이 있는지 확인
            val walletFile = File(context.filesDir, path)

            if (walletFile.exists()) {
                try {
                    val manager = BlockChainManager(password, walletFile)
                    // 초기화 성공 시 간단한 로그
                    android.util.Log.d("UserSession", "BlockChainManager 초기화 성공")
                    return manager
                } catch (e: Exception) {
                    android.util.Log.e("UserSession", "BlockChainManager 초기화 실패: ${e.message}")
                    e.printStackTrace()
                    return null
                }
            } else {
                android.util.Log.e("UserSession", "지갑 파일이 존재하지 않음: $path")

                // 이더리움 주소 형식인지 확인 (0x로 시작하는지)
                if (path.startsWith("0x")) {
                    android.util.Log.d("UserSession", "walletFilePath가 이더리움 주소 형식. 실제 파일 찾기 시도")

                    // 디렉토리에서 지갑 파일 찾기
                    val walletFiles = context.filesDir.listFiles { file ->
                        file.name.startsWith("UTC--") && file.name.endsWith(".json")
                    }

                    if (walletFiles != null && walletFiles.isNotEmpty()) {
                        // 찾은 파일 중 첫 번째 파일 사용
                        val walletFileName = walletFiles[0].name
                        android.util.Log.d("UserSession", "지갑 파일 찾음: $walletFileName")

                        // walletFilePath 업데이트
                        walletFilePath = walletFileName

                        // 업데이트된 경로로 다시 시도
                        return getBlockchainManagerIfAvailable(context)
                    }
                }
            }
        }
        return null
    }


    // 로그아웃 등 세션 종료 시 모든 데이터를 초기화합니다.
    fun clear() {
        preferences.edit().clear().apply()
    }
}
