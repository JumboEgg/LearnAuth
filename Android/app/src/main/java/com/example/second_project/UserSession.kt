package com.example.second_project

import android.content.Context
import android.content.SharedPreferences
import com.example.second_project.blockchain.BlockChainManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
    private const val KEY_IS_CHARGING = "is_charging" // 충전 상태 저장용 키 추가
    private const val KEY_CHARGING_START_TIME = "charging_start_time" // 충전 시작 시간 저장용 키 추가

    // 강의 구매 관련 키 추가
    private const val KEY_PURCHASING_LECTURE_IDS = "purchasing_lecture_ids"
    private const val KEY_PURCHASE_START_TIME_PREFIX = "purchase_start_time_"

    private lateinit var preferences: SharedPreferences
    var lastBalanceUpdateTime: Long = 0
        private set

    // 마지막으로 알려진 잔액 (메모리에만 저장)
    var lastKnownBalance: BigInteger? = null
        set(value) {
            field = value
            // 값이 변경될 때마다 타임스탬프도 업데이트 (최신성 추적용)
            if (value != null) {
                lastBalanceUpdateTime = System.currentTimeMillis()
            }
        }

    fun isBalanceFresh(maxAgeMs: Long = 30000): Boolean { // 기본값은 30초
        return lastKnownBalance != null &&
                System.currentTimeMillis() - lastBalanceUpdateTime < maxAgeMs
    }

    // 충전 관련 상태 변수 추가 (앱 재시작 시에는 초기화되지만 화면 전환 시에는 유지)
    var isCharging: Boolean = false
        get() = field || preferences.getBoolean(KEY_IS_CHARGING, false)
        set(value) {
            field = value
            preferences.edit().putBoolean(KEY_IS_CHARGING, value).apply()
        }

    var chargingStartTime: Long? = null
        get() = if (field != null) field else preferences.getLong(KEY_CHARGING_START_TIME, 0)
            .let { if (it > 0) it else null }
        set(value) {
            field = value
            if (value != null) {
                preferences.edit().putLong(KEY_CHARGING_START_TIME, value).apply()
            } else {
                preferences.edit().remove(KEY_CHARGING_START_TIME).apply()
            }
        }

    private const val KEY_PENDING_CHARGE_AMOUNT = "pending_charge_amount"
    private const val KEY_PENDING_CHARGE_AMOUNT_BASE = "pending_charge_amount_base"

    // 충전 금액 정보 (메모리에만 저장)
    var pendingChargeAmount: BigInteger? = null
        get() {
            if (field != null) return field
            val amountString = preferences.getString(KEY_PENDING_CHARGE_AMOUNT, null)
            return if (amountString != null) BigInteger(amountString) else null
        }
        set(value) {
            field = value
            if (value != null) {
                preferences.edit().putString(KEY_PENDING_CHARGE_AMOUNT, value.toString()).apply()
            } else {
                preferences.edit().remove(KEY_PENDING_CHARGE_AMOUNT).apply()
            }
        }

    var pendingChargeAmountBase: Int? = null
        get() {
            if (field != null) return field
            val hasValue = preferences.contains(KEY_PENDING_CHARGE_AMOUNT_BASE)
            return if (hasValue) preferences.getInt(KEY_PENDING_CHARGE_AMOUNT_BASE, 0) else null
        }
        set(value) {
            field = value
            if (value != null) {
                preferences.edit().putInt(KEY_PENDING_CHARGE_AMOUNT_BASE, value).apply()
            } else {
                preferences.edit().remove(KEY_PENDING_CHARGE_AMOUNT_BASE).apply()
            }
        }

    // 앱 전체 스코프 (화면 전환과 독립적인 코루틴)
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 앱 컨텍스트 (Toast 메시지 등에 사용)
    lateinit var applicationContext: Context

    // BlockChainManager를 인메모리로 캐싱
    private var _blockchainManager: BlockChainManager? = null

    // 구매 중인 강의 ID 집합 추가
    private val purchasingLectureIds = mutableSetOf<Int>()

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        applicationContext = context.applicationContext

        // 저장된 구매 중인 강의 ID 목록 불러오기
        val purchasingIds =
            preferences.getStringSet(KEY_PURCHASING_LECTURE_IDS, emptySet()) ?: emptySet()
        synchronized(purchasingLectureIds) {
            purchasingLectureIds.clear()
            purchasingLectureIds.addAll(purchasingIds.map { it.toInt() })
        }
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
     * 강의 구매 상태를 설정합니다.
     * @param lectureId 구매 중인 강의 ID
     * @param isPurchasing 구매 중 여부
     */
    fun setLecturePurchasing(lectureId: Int, isPurchasing: Boolean) {
        synchronized(purchasingLectureIds) {
            if (isPurchasing) {
                purchasingLectureIds.add(lectureId)
                // 구매 시작 시간 기록
                preferences.edit().putLong(
                    "$KEY_PURCHASE_START_TIME_PREFIX$lectureId",
                    System.currentTimeMillis()
                ).apply()
            } else {
                purchasingLectureIds.remove(lectureId)
                // 구매 시작 시간 삭제
                preferences.edit().remove("$KEY_PURCHASE_START_TIME_PREFIX$lectureId").apply()
            }

            // 구매 중인 강의 ID 목록 저장
            preferences.edit().putStringSet(
                KEY_PURCHASING_LECTURE_IDS,
                purchasingLectureIds.map { it.toString() }.toSet()
            ).apply()
        }
        android.util.Log.d(
            "UserSession",
            "강의 구매 상태 변경: lectureId=$lectureId, isPurchasing=$isPurchasing"
        )
    }

    /**
     * 특정 강의의 구매 중 여부를 확인합니다.
     * @param lectureId 확인할 강의 ID
     * @return 구매 중이면 true, 아니면 false
     */
    fun isLecturePurchasing(lectureId: Int): Boolean {
        synchronized(purchasingLectureIds) {
            return purchasingLectureIds.contains(lectureId)
        }
    }

    /**
     * 특정 강의의 구매 시작 시간을 반환합니다.
     * @param lectureId 강의 ID
     * @return 구매 시작 시간(밀리초), 없으면 null
     */
    fun getLecturePurchaseStartTime(lectureId: Int): Long? {
        val time = preferences.getLong("$KEY_PURCHASE_START_TIME_PREFIX$lectureId", 0)
        return if (time > 0) time else null
    }

    /**
     * 지갑 정보를 로드하고 BlockChainManager 인스턴스를 캐싱하여 반환합니다.
     * 이미 캐시되어 있다면 재사용합니다.
     */
    fun getBlockchainManagerIfAvailable(context: Context): BlockChainManager? {
        // 이미 생성된 블록체인 매니저가 있다면 바로 반환 (캐싱)
        _blockchainManager?.let { return it }

        val password = walletPassword
        if (password.isNullOrEmpty()) {
            android.util.Log.e("UserSession", "지갑 비밀번호가 없습니다")
            return null
        }

        val path = walletFilePath
        val address = walletAddress
        var manager: BlockChainManager? = null

        // 1. 파일 경로를 통한 로드 시도 (최적화: 빠른 실패 경로 추가)
        if (!path.isNullOrEmpty() && !path.startsWith("0x")) {
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
                }
            }
        }

        // 2. 저장된 지갑 주소를 기준으로 로드 (파일 경로가 없거나 실패한 경우)
        if (manager == null && !address.isNullOrEmpty()) {
            android.util.Log.d("UserSession", "지갑 주소로 시도: $address")
            val walletFiles = context.filesDir.listFiles { file ->
                file.name.startsWith("UTC--") && file.name.endsWith(".json")
            }

            if (walletFiles != null && walletFiles.isNotEmpty()) {
                // 2-1. 주소가 일치하는 지갑 파일 찾기 (최적화: 병렬 검색 구현 가능)
                for (walletFile in walletFiles) {
                    try {
                        val credentials = WalletUtils.loadCredentials(password, walletFile)
                        if (credentials.address.equals(address, ignoreCase = true)) {
                            android.util.Log.d("UserSession", "✅ 일치하는 지갑 파일 발견: ${walletFile.name}")
                            walletFilePath = walletFile.name
                            manager = BlockChainManager(password, walletFile)
                            break
                        }
                    } catch (e: Exception) {
                        android.util.Log.d("UserSession", "지갑 파일 검증 실패: ${walletFile.name}")
                    }
                }

                // 2-2. 일치하는 파일이 없다면 첫 번째 유효한 지갑 사용
                if (manager == null) {
                    for (walletFile in walletFiles) {
                        try {
                            val credentials = WalletUtils.loadCredentials(password, walletFile)
                            walletFilePath = walletFile.name
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
            }
        }

        // 성공적으로 생성된 매니저를 캐시에 저장
        if (manager != null) {
            _blockchainManager = manager
            return manager
        } else {
            android.util.Log.e("UserSession", "사용 가능한 지갑을 찾을 수 없습니다.")
            return null
        }
    }

    fun preInitializeBlockchainManager(context: Context) {
        applicationScope.launch(Dispatchers.IO) {
            try {
                val manager = getBlockchainManagerIfAvailable(context)
                if (manager != null) {
                    // 지갑 주소 미리 가져오기
                    val address = manager.getMyWalletAddress()
                    walletAddress = address
                    android.util.Log.d("UserSession", "BlockChainManager 사전 초기화 성공: $address")
                    // 잔액 미리 가져오기
                    try {
                        val balance = manager.getMyCatTokenBalance()
                        lastKnownBalance = balance
                        android.util.Log.d("UserSession", "사전 잔액 로드 성공: $balance wei")
                    } catch (e: Exception) {
                        android.util.Log.e("UserSession", "사전 잔액 로드 실패: ${e.message}")
                    }
                } else {
                }
            } catch (e: Exception) {
                android.util.Log.e("UserSession", "BlockChainManager 사전 초기화 실패: ${e.message}")
            }
        }
    }

    /**
     * 로그아웃 등 세션 종료 시 모든 사용자 데이터를 초기화하며,
     * 캐시된 BlockChainManager도 초기화합니다.
     */
    fun clear() {
        preferences.edit().clear().apply()
        _blockchainManager = null
        // 메모리 상태 변수도 초기화
        lastKnownBalance = null
        isCharging = false
        pendingChargeAmount = null
        pendingChargeAmountBase = null
        chargingStartTime = null

        // 구매 중인 강의 ID 목록 초기화
        synchronized(purchasingLectureIds) {
            purchasingLectureIds.clear()
        }
    }
}