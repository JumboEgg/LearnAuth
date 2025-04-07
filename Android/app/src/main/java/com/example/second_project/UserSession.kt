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
// UserSession의 getBlockchainManagerIfAvailable 메서드 개선 제안
// (아래 코드는 UserSession 클래스에 있는 현재 함수를 대체할 수 있습니다)

    fun getBlockchainManagerIfAvailable(context: Context): BlockChainManager? {
        val path = walletFilePath
        val password = walletPassword

        if (path.isNullOrEmpty() || password.isNullOrEmpty()) {
            android.util.Log.e("UserSession", "지갑 경로 또는 비밀번호가 없습니다")
            return null
        }

        // 이더리움 주소 형식인지 확인 (0x로 시작하는지)
        if (path.startsWith("0x")) {
            android.util.Log.d("UserSession", "walletFilePath가 이더리움 주소 형식: $path")
            // 디렉토리에서 지갑 파일 찾기
            val walletFiles = context.filesDir.listFiles { file ->
                file.name.startsWith("UTC--") && file.name.endsWith(".json")
            }

            if (walletFiles != null && walletFiles.isNotEmpty()) {
                // 발견된 모든 지갑 파일에 대해 검증
                for (walletFile in walletFiles) {
                    try {
                        // 비밀번호로 지갑 검증 시도
                        val credentials = org.web3j.crypto.WalletUtils.loadCredentials(
                            password,
                            walletFile
                        )
                        val walletAddress = credentials.address

                        // 주소가 저장된 주소와 일치하는지 확인
                        if (walletAddress.equals(path, ignoreCase = true)) {
                            android.util.Log.d(
                                "UserSession",
                                "✅ 일치하는 지갑 파일을 발견: ${walletFile.name}"
                            )
                            // walletFilePath 업데이트 - 이 부분이 중요합니다!
                            walletFilePath = walletFile.name
                            try {
                                val manager = BlockChainManager(password, walletFile)
                                android.util.Log.d("UserSession", "BlockChainManager 초기화 성공")
                                return manager
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "UserSession",
                                    "BlockChainManager 초기화 실패: ${e.message}"
                                )
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.d(
                            "UserSession",
                            "지갑 파일 검증 실패: ${walletFile.name}, 오류: ${e.message}"
                        )
                    }
                }

                // 일치하는 지갑을 찾지 못했지만, 사용 가능한 첫 번째 지갑 시도
                android.util.Log.w("UserSession", "⚠️ DB 주소($path)와 일치하는 지갑 파일이 없습니다. 첫 번째 지갑 시도.")
                try {
                    val manager = BlockChainManager(password, walletFiles[0])
                    // walletFilePath 업데이트 - 사용 가능한 파일로!
                    walletFilePath = walletFiles[0].name
                    android.util.Log.d("UserSession", "대체 지갑으로 BlockChainManager 초기화 성공")
                    return manager
                } catch (e: Exception) {
                    android.util.Log.e(
                        "UserSession",
                        "대체 지갑으로 BlockChainManager 초기화 실패: ${e.message}"
                    )
                    e.printStackTrace()
                }
            } else {
                android.util.Log.e("UserSession", "지갑 주소($path)에 대응하는 지갑 파일을 찾을 수 없습니다")
            }
            return null
        }

        // 주어진 경로에 파일이 있는지 확인 (일반 파일 경로)
        val walletFile = File(context.filesDir, path)
        if (walletFile.exists()) {
            try {
                val manager = BlockChainManager(password, walletFile)
                android.util.Log.d("UserSession", "BlockChainManager 초기화 성공")
                return manager
            } catch (e: Exception) {
                android.util.Log.e("UserSession", "BlockChainManager 초기화 실패: ${e.message}")
                e.printStackTrace()

                // 실패하면 다른 지갑 파일 시도
                val walletFiles = context.filesDir.listFiles { file ->
                    file.name.startsWith("UTC--") && file.name.endsWith(".json") && file.name != path
                }

                if (walletFiles != null && walletFiles.isNotEmpty()) {
                    android.util.Log.d("UserSession", "다른 지갑 파일 시도: ${walletFiles[0].name}")
                    try {
                        val altManager = BlockChainManager(password, walletFiles[0])
                        // walletFilePath 업데이트 - 사용 가능한 파일로!
                        walletFilePath = walletFiles[0].name
                        android.util.Log.d("UserSession", "대체 지갑으로 BlockChainManager 초기화 성공")
                        return altManager
                    } catch (e2: Exception) {
                        android.util.Log.e(
                            "UserSession",
                            "대체 지갑으로 BlockChainManager 초기화 실패: ${e2.message}"
                        )
                        e2.printStackTrace()
                    }
                }
            }
        } else {
            android.util.Log.e("UserSession", "지갑 파일이 존재하지 않음: $path")
            // 파일이 없으면 다른 지갑 파일 찾기
            val walletFiles = context.filesDir.listFiles { file ->
                file.name.startsWith("UTC--") && file.name.endsWith(".json")
            }

            if (walletFiles != null && walletFiles.isNotEmpty()) {
                android.util.Log.d("UserSession", "대체 지갑 파일 찾음: ${walletFiles[0].name}")
                try {
                    val manager = BlockChainManager(password, walletFiles[0])
                    // walletFilePath 업데이트 - 사용 가능한 파일로!
                    walletFilePath = walletFiles[0].name
                    android.util.Log.d("UserSession", "대체 지갑으로 BlockChainManager 초기화 성공")
                    return manager
                } catch (e: Exception) {
                    android.util.Log.e(
                        "UserSession",
                        "대체 지갑으로 BlockChainManager 초기화 실패: ${e.message}"
                    )
                    e.printStackTrace()
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
