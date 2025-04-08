package com.example.second_project

import android.content.Context
import android.content.SharedPreferences
import com.example.second_project.blockchain.BlockChainManager
import org.web3j.crypto.WalletUtils
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
    private const val KEY_WALLET_ADDRESS = "wallet_address"
    private const val KEY_WALLET_PASSWORD = "wallet_password"

    private lateinit var preferences: SharedPreferences

    // 마지막으로 알려진 잔액 (메모리에만 저장)
    var lastKnownBalance: BigInteger? = null

    // BlockChainManager를 인메모리로 캐싱
    private var _blockchainManager: BlockChainManager? = null

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
    var walletAddress: String?
        get() = preferences.getString(KEY_WALLET_ADDRESS, null)
        set(value) {
            preferences.edit().putString(KEY_WALLET_ADDRESS, value).apply()
        }
    var walletFilePath: String?
        get() = preferences.getString(KEY_WALLET_PATH, null)
        set(value) {
            if (value?.startsWith("0x") == true) {
                walletAddress = value
                // 파일 경로는 기존 값을 유지
            } else {
                preferences.edit().putString(KEY_WALLET_PATH, value).apply()
            }
        }
    var walletPassword: String?
        get() = preferences.getString(KEY_WALLET_PASSWORD, null)
        set(value) {
            preferences.edit().putString(KEY_WALLET_PASSWORD, value).apply()
        }

    /**
     * 지갑 정보를 로드하고 BlockChainManager 인스턴스를 캐싱하여 반환합니다.
     * 이미 캐시되어 있다면 재사용합니다.
     */
    fun getBlockchainManagerIfAvailable(context: Context): BlockChainManager? {
        // 이미 생성된 블록체인 매니저가 있다면 바로 반환
        _blockchainManager?.let { return it }

        val password = walletPassword
        if (password.isNullOrEmpty()) {
            android.util.Log.e("UserSession", "지갑 비밀번호가 없습니다")
            return null
        }

        val path = walletFilePath
        val address = walletAddress

        var manager: BlockChainManager? = null

        // 1. 파일 경로를 통한 로드 시도
        if (!path.isNullOrEmpty()) {
            if (!path.startsWith("0x")) {
                val walletFile = File(context.filesDir, path)
                if (walletFile.exists()) {
                    try {
                        // 지갑 파일 유효성 검증
                        val credentials = WalletUtils.loadCredentials(password, walletFile)
                        // 저장된 주소가 없거나 다른 경우 업데이트
                        if (address == null || credentials.address != address) {
                            walletAddress = credentials.address
                        }
                        manager = BlockChainManager(password, walletFile)
                        android.util.Log.d("UserSession", "BlockChainManager 초기화 성공: $path")
                    } catch (e: Exception) {
                        android.util.Log.e("UserSession", "BlockChainManager 초기화 실패: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        }

        // 2. 파일 경로가 없거나 실패한 경우, 저장된 지갑 주소를 기준으로 로드
        if (manager == null && !address.isNullOrEmpty()) {
            android.util.Log.d("UserSession", "지갑 주소로 시도: $address")
            val walletFiles = context.filesDir.listFiles { file ->
                file.name.startsWith("UTC--") && file.name.endsWith(".json")
            }

            if (walletFiles != null && walletFiles.isNotEmpty()) {
                // 2-1. 주소가 일치하는 지갑 파일 찾기
                for (walletFile in walletFiles) {
                    try {
                        val credentials = WalletUtils.loadCredentials(password, walletFile)
                        if (credentials.address.equals(address, ignoreCase = true)) {
                            android.util.Log.d("UserSession", "✅ 일치하는 지갑 파일 발견: ${walletFile.name}")
                            preferences.edit().putString(KEY_WALLET_PATH, walletFile.name).apply()
                            manager = BlockChainManager(password, walletFile)
                            break
                        }
                    } catch (e: Exception) {
                        android.util.Log.d("UserSession", "지갑 파일 검증 실패: ${walletFile.name}")
                    }
                }
                // 2-2. 일치하는 파일이 없다면 첫 번째 유효한 지갑 사용
                if (manager == null) {
                    android.util.Log.w(
                        "UserSession",
                        "⚠️ 주소($address)와 일치하는 지갑 파일이 없습니다. 첫 번째 지갑 사용."
                    )
                    for (walletFile in walletFiles) {
                        try {
                            val credentials = WalletUtils.loadCredentials(password, walletFile)
                            preferences.edit().putString(KEY_WALLET_PATH, walletFile.name).apply()
                            walletAddress = credentials.address
                            android.util.Log.d(
                                "UserSession",
                                "대체 지갑 업데이트: 주소=${credentials.address}"
                            )
                            manager = BlockChainManager(password, walletFile)
                            break
                        } catch (e: Exception) {
                            android.util.Log.d("UserSession", "대체 지갑 검증 실패: ${walletFile.name}")
                        }
                    }
                }
            } else {
                android.util.Log.e("UserSession", "지갑 파일을 찾을 수 없습니다.")
            }
        }

        if (manager != null) {
            // 성공적으로 로드한 경우 캐시에 저장 후 반환
            _blockchainManager = manager
            return manager
        } else {
            android.util.Log.e("UserSession", "사용 가능한 지갑을 찾을 수 없습니다.")
            return null
        }
    }

    /**
     * 로그아웃 등 세션 종료 시 모든 사용자 데이터를 초기화하며,
     * 캐시된 BlockChainManager도 초기화합니다.
     */
    fun clear() {
        preferences.edit().clear().apply()
        _blockchainManager = null
    }
}
