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
    private const val KEY_WALLET_ADDRESS = "wallet_address" // 새로 추가: 실제 이더리움 주소 저장
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
    // 이더리움 주소는 별도로 관리하면서 파일 경로 분리
    var walletAddress: String?
        get() = preferences.getString(KEY_WALLET_ADDRESS, null)
        set(value) {
            preferences.edit().putString(KEY_WALLET_ADDRESS, value).apply()
        }

    var walletFilePath: String?
        get() = preferences.getString(KEY_WALLET_PATH, null)
        set(value) {
            // 값이 0x로 시작하는 이더리움 주소면 주소로 저장
            if (value?.startsWith("0x") == true) {
                walletAddress = value
                // 기존 walletFilePath를 null로 설정하지 않고 유지
                // (파일이 있으면 그 파일을 계속 사용)
            } else {
                // 일반 파일 경로면 그대로 저장
                preferences.edit().putString(KEY_WALLET_PATH, value).apply()
            }
        }
    var walletPassword: String?
        get() = preferences.getString(KEY_WALLET_PASSWORD, null)
        set(value) {
            preferences.edit().putString(KEY_WALLET_PASSWORD, value).apply()
        }

    // ✅ 세션 기반으로 BlockChainManager 생성
    fun getBlockchainManagerIfAvailable(context: Context): BlockChainManager? {
        val password = walletPassword
        if (password.isNullOrEmpty()) {
            android.util.Log.e("UserSession", "지갑 비밀번호가 없습니다")
            return null
        }

        val path = walletFilePath
        val address = walletAddress

        // 먼저 지갑 파일 경로가 있는지 확인
        if (!path.isNullOrEmpty()) {
            // 일반 파일 경로인지 확인 (0x로 시작하지 않는지)
            if (!path.startsWith("0x")) {
                val walletFile = File(context.filesDir, path)
                if (walletFile.exists()) {
                    try {
                        // 지갑 파일이 유효한지 검증
                        val credentials = WalletUtils.loadCredentials(password, walletFile)
                        // 지갑 주소가 없거나 다르면 현재 로드된 주소로 업데이트
                        if (address == null || credentials.address != address) {
                            walletAddress = credentials.address
                        }
                        val manager = BlockChainManager(password, walletFile)
                        android.util.Log.d("UserSession", "BlockChainManager 초기화 성공: $path")
                        return manager
                    } catch (e: Exception) {
                        android.util.Log.e("UserSession", "BlockChainManager 초기화 실패: ${e.message}")
                        e.printStackTrace()
                        // 파일이 있지만 로드 실패하면 다른 지갑 시도
                    }
                }
            }
        }

        // 여기까지 왔다면 파일 경로가 없거나, 파일을 찾을 수 없거나, 로드에 실패한 경우
        // 이제 지갑 주소를 기준으로 시도
        if (!address.isNullOrEmpty()) {
            android.util.Log.d("UserSession", "지갑 주소로 시도: $address")
            // 디렉토리에서 모든 지갑 파일 찾기
            val walletFiles = context.filesDir.listFiles { file ->
                file.name.startsWith("UTC--") && file.name.endsWith(".json")
            }

            if (walletFiles != null && walletFiles.isNotEmpty()) {
                // 1. 주소가 일치하는 지갑 파일 찾기
                for (walletFile in walletFiles) {
                    try {
                        val credentials = WalletUtils.loadCredentials(password, walletFile)
                        if (credentials.address.equals(address, ignoreCase = true)) {
                            android.util.Log.d("UserSession", "✅ 일치하는 지갑 파일을 발견: ${walletFile.name}")
                            // 실제 파일 경로 업데이트
                            preferences.edit().putString(KEY_WALLET_PATH, walletFile.name).apply()
                            val manager = BlockChainManager(password, walletFile)
                            return manager
                        }
                    } catch (e: Exception) {
                        android.util.Log.d("UserSession", "지갑 파일 검증 실패: ${walletFile.name}")
                    }
                }

                // 2. 일치하는 것이 없으면 첫 번째 유효한 지갑 사용
                android.util.Log.w("UserSession", "⚠️ 주소($address)와 일치하는 지갑 파일이 없습니다. 첫 번째 지갑 시도.")
                for (walletFile in walletFiles) {
                    try {
                        val credentials = WalletUtils.loadCredentials(password, walletFile)
                        // 새로운 지갑으로 업데이트
                        preferences.edit().putString(KEY_WALLET_PATH, walletFile.name).apply()
                        // 주소도 업데이트
                        walletAddress = credentials.address
                        android.util.Log.d("UserSession", "대체 지갑으로 업데이트: 주소=${credentials.address}")
                        val manager = BlockChainManager(password, walletFile)
                        return manager
                    } catch (e: Exception) {
                        android.util.Log.d("UserSession", "대체 지갑 파일 검증 실패: ${walletFile.name}")
                    }
                }
            } else {
                android.util.Log.e("UserSession", "지갑 파일을 찾을 수 없습니다.")
            }
        }

        // 모든 시도가 실패한 경우
        android.util.Log.e("UserSession", "사용 가능한 지갑을 찾을 수 없습니다.")
        return null
    }

    // 로그아웃 등 세션 종료 시 모든 데이터를 초기화합니다.
    fun clear() {
        preferences.edit().clear().apply()
    }
}