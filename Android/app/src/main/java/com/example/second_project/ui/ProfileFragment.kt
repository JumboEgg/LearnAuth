package com.example.second_project.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.second_project.LoginActivity
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.data.TransactionCache
import com.example.second_project.data.TransactionItem
import com.example.second_project.databinding.FragmentProfileBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.AuthApiService
import com.example.second_project.network.CertificateApiService
import com.example.second_project.viewmodel.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val TAG = "ProfileFragment"

    // 데이터 사전 로드 관련 변수
    private var isPreloadingTransactions = false
    private var lastTransactionPreloadTime = 0L
    private val PRELOAD_INTERVAL = 5 * 60 * 1000L // 5분 간격으로 미리 로드

    // 잔액 갱신 관련 변수 추가
    private var isBalanceLoading = false
    private var lastBalanceUpdateTime = 0L
    private val BALANCE_UPDATE_INTERVAL = 5000L // 5초마다 갱신

    private val userId: BigInteger
        get() = BigInteger.valueOf(UserSession.userId.toLong())

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 1. 사용자 이름 등 즉시 표시 (UI 먼저 보여줌)
        binding.textName.text = "${UserSession.nickname}님,"
        // 2. 캐시된 잔액 즉시 표시 (if available)
        UserSession.lastKnownBalance?.let {
            updateBalanceDisplay(it)
        } ?: run {
            binding.moneyCount.text = "로딩 중..."
        }
        // 3. 메뉴 버튼 리스너 등은 즉시 설정
        setupMenuListeners()
        // 4. 병렬 처리를 위한 코루틴 시작 - 최적화된 방식으로 호출 순서 변경
        viewLifecycleOwner.lifecycleScope.launch {
            // 잔액 로드를 최우선으로 실행 - 가장 중요한 정보이므로
            loadBalanceOptimized()

            // 이 작업들은 병렬로 실행해도 괜찮음 (동시에 시작)
            launch { loadCertificatesAndSetProfileImage() }

            // 지갑 파일 처리는 백그라운드에서 진행
            launch(Dispatchers.IO) {
                handleWalletFile()
            }

            // 트랜잭션 데이터는 우선순위가 낮으므로 마지막에 로드
            launch {
                delay(500) // 잔액 로드 후 약간 지연시켜 실행
                preloadTransactionData()
            }
        }
    }

    // 메뉴 버튼 클릭 리스너 설정
    private fun setupMenuListeners() {
        // profileMenu1 -> MyWalletFragment 이동
        // (최적화: 이동하기 전에 트랜잭션 데이터 미리 로드)
        binding.profileMenu1.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myWalletFragment)
        }
        // profileMenu2 -> MyLectureFragment 이동
        binding.profileMenu2.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myLectureFragment)
        }
        // profileMenu3 -> DeclarationFragment 이동
        binding.profileMenu3.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_declarationFragment)
        }
        // 충전하기 -> ChargeFragment 이동
        binding.chargeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_chargeFragment)
        }
        // 로그아웃: API 호출 후 세션 클리어 및 LoginActivity로 이동
        binding.profileMenu4.setOnClickListener {
            logout()
        }
    }

    // 지갑 파일 처리 (주소나 파일 경로 기반으로 유효한 지갑 확인)
    private fun handleWalletFile() {
        val path = UserSession.walletFilePath
        val address = UserSession.walletAddress
        Log.d(TAG, "지갑 정보 검증 시작: 파일 경로=$path, 주소=$address")
        // 주소가 있지만 파일 경로가 없는 경우 (DB에는 주소만 있는 경우)
        if (!address.isNullOrEmpty() && (path.isNullOrEmpty() || path == address)) {
            Log.d(TAG, "이더리움 주소가 있지만 파일 경로가 없습니다: $address")
            // 지갑 파일 찾기
            val walletFiles = requireContext().filesDir.listFiles { file ->
                file.name.startsWith("UTC--") && file.name.endsWith(".json")
            }
            if (walletFiles != null && walletFiles.isNotEmpty()) {
                Log.d(TAG, "총 ${walletFiles.size}개의 지갑 파일을 찾았습니다.")
                // 주소와 일치하는 지갑 파일 찾기
                for (walletFile in walletFiles) {
                    try {
                        val credentials = WalletUtils.loadCredentials(
                            UserSession.walletPassword,
                            walletFile
                        )
                        if (credentials.address.equals(address, ignoreCase = true)) {
                            Log.d(TAG, "✅ 일치하는 지갑 파일 발견: ${walletFile.name}")
                            UserSession.walletFilePath = walletFile.name
                            return
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "지갑 파일 검증 실패: ${walletFile.name}")
                    }
                }
                // 일치하는 지갑을 찾지 못하면 첫 번째 유효한 지갑 사용
                Log.w(TAG, "⚠️ 일치하는 지갑 파일을 찾지 못했습니다. 첫 번째 유효한 지갑 시도")
                for (walletFile in walletFiles) {
                    try {
                        val credentials = WalletUtils.loadCredentials(
                            UserSession.walletPassword,
                            walletFile
                        )
                        // 파일 경로 업데이트
                        UserSession.walletFilePath = walletFile.name
                        // 주소도 업데이트
                        UserSession.walletAddress = credentials.address
                        Log.d(
                            TAG,
                            "✅ 대체 지갑으로 업데이트: ${walletFile.name}, 주소=${credentials.address}"
                        )
                        return
                    } catch (e: Exception) {
                        Log.d(TAG, "대체 지갑 검증 실패: ${walletFile.name}")
                    }
                }
                Log.e(TAG, "⚠️ 사용 가능한 지갑 파일을 찾지 못했습니다")
            } else {
                Log.e(TAG, "⚠️ 지갑 파일이 없습니다")
            }
        }
        // 파일 경로가 있는 경우 (일반 파일 경로)
        else if (!path.isNullOrEmpty() && !path.startsWith("0x")) {
            Log.d(TAG, "지갑 파일 경로가 있습니다: $path")
            val walletFile = File(requireContext().filesDir, path)
            if (walletFile.exists()) {
                try {
                    val credentials = WalletUtils.loadCredentials(
                        UserSession.walletPassword,
                        walletFile
                    )
                    // 주소 업데이트
                    UserSession.walletAddress = credentials.address
                    Log.d(TAG, "✅ 지갑 파일 검증 성공: 주소=${credentials.address}")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ 지갑 파일 검증 실패: ${e.message}")
                    // 검증 실패 시 다른 지갑 파일 시도
                    findAlternativeWallet()
                }
            } else {
                Log.w(TAG, "⚠️ 지갑 파일이 존재하지 않습니다: $path")
                // 파일이 없으면 다른 지갑 파일 시도
                findAlternativeWallet()
            }
        }
    }

    // 대체 지갑 찾기 (기존 지갑이 유효하지 않을 때 호출)
    private fun findAlternativeWallet() {
        val walletFiles = requireContext().filesDir.listFiles { file ->
            file.name.startsWith("UTC--") && file.name.endsWith(".json")
        }
        if (walletFiles != null && walletFiles.isNotEmpty()) {
            for (walletFile in walletFiles) {
                try {
                    val credentials = WalletUtils.loadCredentials(
                        UserSession.walletPassword,
                        walletFile
                    )
                    UserSession.walletFilePath = walletFile.name
                    UserSession.walletAddress = credentials.address
                    Log.d(
                        TAG,
                        "✅ 대체 지갑으로 업데이트: ${walletFile.name}, 주소=${credentials.address}"
                    )
                    return
                } catch (e: Exception) {
                    Log.d(TAG, "대체 지갑 검증 실패: ${walletFile.name}")
                }
            }
        }
    }

    // 최적화된 잔액 로딩 메소드
    private suspend fun loadBalanceOptimized() {
        if (isBalanceLoading) return // 이미 로딩 중이면 중복 실행 방지

        isBalanceLoading = true

        try {
            if (!isAdded) return
            val context = context ?: return

            // 1. BlockChainManager 가져오기
            val manager = UserSession.getBlockchainManagerIfAvailable(context)
            if (manager == null) {
                Log.e(TAG, "BlockChainManager를 초기화할 수 없습니다")
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        Toast.makeText(context, "지갑 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                    }
                }
                isBalanceLoading = false
                return
            }

            // 2. 비동기 잔액 조회 (최적화된 메소드 사용)
            try {
                // 개선: 비동기 코루틴 방식 사용 (타임아웃 및 캐싱 적용)
                val balanceInWei = withContext(Dispatchers.IO) {
                    manager.getMyCatTokenBalance() // 캐시 적용된 최적화 버전 사용
                }

                // 3. UserSession에 잔액 저장
                UserSession.lastKnownBalance = balanceInWei
                lastBalanceUpdateTime = System.currentTimeMillis()

                // 4. UI 업데이트
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        updateBalanceDisplay(balanceInWei)
                    }
                }

                Log.d(TAG, "💰 잔액 갱신 성공: $balanceInWei")
            } catch (e: Exception) {
                Log.e(TAG, "💰 잔액 조회 실패: ${e.message}")

                // 5. 오류 발생 시 UI 처리
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        // 이전 잔액이 있으면 표시 유지, 없으면 오류 메시지
                        if (UserSession.lastKnownBalance == null) {
                            binding.moneyCount.text = "잔액 조회 실패"
                            Toast.makeText(context, "잔액 조회 실패: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        } finally {
            isBalanceLoading = false
        }
    }

    // 기존 잔액 로드 함수 (백업으로 유지)
    private suspend fun loadBalanceAsync() = withContext(Dispatchers.IO) {
        if (!isAdded) return@withContext
        val context = context ?: return@withContext
        val manager = UserSession.getBlockchainManagerIfAvailable(context)
        if (manager == null) {
            Log.w(TAG, "지갑 정보가 없습니다. 로그인 다시 해주세요")
            withContext(Dispatchers.Main) {
                if (isAdded && _binding != null) {
                    Toast.makeText(context, "지갑 정보가 없습니다. 로그인을 다시 해주세요", Toast.LENGTH_SHORT).show()
                }
            }
            return@withContext
        }
        try {
            // 백그라운드에서 잔액 조회 - 코드 최적화
            val address = manager.getMyWalletAddress()
            Log.d(TAG, "📍 내 지갑 주소: $address")
            if (UserSession.walletAddress.isNullOrEmpty()) {
                UserSession.walletAddress = address
            }
            // 잔액 조회 작업 시작
            var balanceInWei: BigInteger? = null
            var retryCount = 0
            var success = false
            // 최대 2회까지 빠르게 재시도
            while (retryCount < 2 && !success) {
                try {
                    balanceInWei = manager.getMyCatTokenBalance()
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "잔액 조회 시도 ${retryCount + 1} 실패: ${e.message}")
                    retryCount++
                    delay(100) // 딜레이를 짧게 (100ms)로 설정
                }
            }
            if (success && balanceInWei != null) {
                Log.d(TAG, "💰 CATToken 잔액(wei): $balanceInWei")
                // 잔액 변화 감지 로직
                val previousBalance = UserSession.lastKnownBalance
                if (previousBalance != null) {
                    val diff = balanceInWei.subtract(previousBalance)
                    if (diff > BigInteger.ZERO) {
                        Log.d(TAG, "💰 잔액 증가 감지: +${diff} wei")
                    } else if (diff < BigInteger.ZERO) {
                        Log.d(TAG, "💰 잔액 감소 감지: ${diff} wei")
                    } else {
                        Log.d(TAG, "💰 잔액 변화 없음")
                    }
                }
                // 마지막 잔액 저장
                UserSession.lastKnownBalance = balanceInWei
                // UI 업데이트를 메인 스레드에서 안전하게 수행
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        updateBalanceDisplay(balanceInWei)
                    }
                }
            } else {
                Log.e(TAG, "잔액 조회 실패")
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        Toast.makeText(context, "잔액 조회에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "잔액 조회 실패", e)
            withContext(Dispatchers.Main) {
                if (isAdded && _binding != null) {
                    Toast.makeText(context, "잔액 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 잔액 표시 업데이트
    private fun updateBalanceDisplay(balanceInWei: java.math.BigInteger) {
        val TOKEN_UNIT = java.math.BigInteger.TEN.pow(18)
        // 18자리 정밀도로 나눈 후 BigDecimal로 변환
        val balanceDecimal = balanceInWei.toBigDecimal()
            .divide(TOKEN_UNIT.toBigDecimal(), 18, java.math.RoundingMode.HALF_UP)
        // 천 단위 콤마 + 소수점 둘째 자리까지 고정
        val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
            roundingMode = java.math.RoundingMode.HALF_UP
        }
        val formattedBalance = numberFormat.format(balanceDecimal)
        Log.d(TAG, "💰 표시용 CATToken 잔액: $formattedBalance")
        binding.moneyCount.text = "$formattedBalance CAT"
    }

    // 추가된 함수: 트랜잭션 데이터를 미리 로드
    private suspend fun preloadTransactionData(forceLoad: Boolean = false) =
        withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            // 이미 로드 중이거나 충분히 최근에 로드한 경우 스킵
            if (isPreloadingTransactions && !forceLoad) {
                Log.d(TAG, "이미 트랜잭션 데이터 로드 중입니다.")
                return@withContext
            }
            // 캐시가 충분히 최신인지 확인 (향상된 TransactionCache 사용)
            if (!forceLoad && TransactionCache.isFresh(PRELOAD_INTERVAL)) {
                Log.d(
                    TAG,
                    "최근에 트랜잭션 데이터를 로드했습니다. (${(currentTime - TransactionCache.lastUpdateTime) / 1000}초 전)"
                )
                return@withContext
            }
            if (!isAdded) {
                Log.d(TAG, "Fragment is not attached to context, cancelling transaction preload")
                return@withContext
            }
            val context = context ?: return@withContext
            val manager = UserSession.getBlockchainManagerIfAvailable(context)
            if (manager == null) {
                Log.e(TAG, "BlockChainManager 초기화 실패, 트랜잭션 미리 로드 불가")
                return@withContext
            }
            // 로드 시작 상태 업데이트
            isPreloadingTransactions = true
            try {
                Log.d(TAG, "트랜잭션 데이터 미리 로드 시작...")
                // 최적화: 로그 조회를 위한 시작 블록 줄이기
                val startBlock = DefaultBlockParameter.valueOf(BigInteger.valueOf(12345678L))
                val endBlock = DefaultBlockParameterName.LATEST
                // 세 이벤트 카테고리를 가져와서 ParsedEvent로 변환 후 합칩니다.
                val depositedEvents = manager.lectureEventMonitor.getEventLogs(
                    "TokenDeposited", userId, manager.web3j,
                    manager.lectureEventMonitor.contractAddress, startBlock, endBlock
                ).map { evt ->
                    TransactionItem("토큰 충전", evt.date, evt.amount, evt.timestamp)
                }
                val withdrawnEvents = manager.lectureEventMonitor.getEventLogs(
                    "TokenWithdrawn", userId, manager.web3j,
                    manager.lectureEventMonitor.contractAddress, startBlock, endBlock
                ).map { evt ->
                    TransactionItem("토큰 출금", evt.date, evt.amount, evt.timestamp)
                }
                val purchasedEvents = manager.lectureEventMonitor.getEventLogs(
                    "LecturePurchased", userId, manager.web3j,
                    manager.lectureEventMonitor.contractAddress, startBlock, endBlock
                ).map { evt ->
                    TransactionItem(evt.title, evt.date, evt.amount, evt.timestamp)
                }
                // 전체 이벤트를 하나로 합친 후 타임스탬프 기준 내림차순 정렬합니다.
                val events = (depositedEvents + withdrawnEvents + purchasedEvents)
                    .sortedByDescending { it.timestamp }
                if (events.isNotEmpty()) {
                    // 향상된 TransactionCache 사용
                    TransactionCache.updateTransactions(events)
                    Log.d(TAG, "트랜잭션 데이터 미리 로드 완료: ${events.size}개의 거래 찾음")
                } else {
                    Log.d(TAG, "트랜잭션 데이터 미리 로드 완료: 거래가 없습니다")
                }
                // 타임스탬프 업데이트
                lastTransactionPreloadTime = currentTime
            } catch (e: Exception) {
                Log.e(TAG, "트랜잭션 데이터 미리 로드 실패: ${e.message}")
            } finally {
                isPreloadingTransactions = false
            }
        }

    // 로그아웃 처리
    private fun logout() {
        val refreshToken = UserSession.refreshToken
        if (refreshToken.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Refresh token is missing", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d(TAG, "Stored refreshToken: $refreshToken")
        val authApiService = ApiClient.retrofit.create(AuthApiService::class.java)
        authApiService.logout(refreshToken).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 로그아웃 성공: 세션 초기화 후 LoginActivity로 이동
                    UserSession.clear()
                    TransactionCache.clear() // 추가: 캐시도 함께 초기화
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    Log.e("Logout", "Logout failed. Code: ${response.code()}, Error: $errorBody")
                    Toast.makeText(
                        requireContext(),
                        "로그아웃 실패: 코드 ${response.code()}, 에러: $errorBody",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Logout", "Logout error", t)
                Toast.makeText(requireContext(), "로그아웃 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 수료증 데이터 로드 및 프로필 이미지 설정
    private suspend fun loadCertificatesAndSetProfileImage() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "수료증 데이터 로드 시작")
            val certificateApiService = ApiClient.retrofit.create(CertificateApiService::class.java)
            val response = certificateApiService.getCertificates(UserSession.userId).execute()
            if (response.isSuccessful && response.body() != null) {
                val certificateResponse = response.body()!!
                // certificate 값이 0보다 큰 항목의 수 계산 (수료증이 발급된 항목)
                val certificateCount = certificateResponse.data.count { it.certificateDate != null }
                Log.d(TAG, "수료증 개수: $certificateCount")
                // 수료증 개수에 따라 프로필 이미지 설정
                withContext(Dispatchers.Main) {
                    val profileImageResId = when {
                        certificateCount < 3 -> R.drawable.profile1
                        certificateCount < 6 -> R.drawable.profile2
                        else -> R.drawable.profile3
                    }
                    binding.profileImg.setImageResource(profileImageResId)
                    Log.d(TAG, "프로필 이미지 설정: $profileImageResId (수료증 ${certificateCount}개)")
                }
            } else {
                Log.e(TAG, "수료증 데이터 로드 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "수료증 데이터 로드 중 오류 발생", e)
        }
    }

    // 최적화된 onResume - 화면에 다시 돌아올 때 잔액 갱신
    override fun onResume() {
        super.onResume()

        // Fragment가 활성 상태인 경우에만 실행
        if (isAdded && view != null) {
            // 1. 즉시 UserSession의 캐시된 잔액을 표시 (있는 경우)
            UserSession.lastKnownBalance?.let {
                if (_binding != null) {
                    updateBalanceDisplay(it)
                }
            }

            // 2. 마지막 갱신 시간 확인하여 필요한 경우에만 갱신
            val currentTime = System.currentTimeMillis()
            val needsUpdate = (currentTime - lastBalanceUpdateTime > BALANCE_UPDATE_INTERVAL)

            // 3. 충분한 시간이 지났거나 잔액이 없는 경우에만 새로 조회
            if (needsUpdate || UserSession.lastKnownBalance == null) {
                Log.d(
                    TAG,
                    "onResume: 잔액 갱신 시작 (마지막 갱신 후 ${(currentTime - lastBalanceUpdateTime) / 1000}초 경과)"
                )

                // 4. 비동기로 잔액 조회 - UI 블로킹 없이 진행
                viewLifecycleOwner.lifecycleScope.launch {
                    loadBalanceOptimized()

                    // 5. 잔액 조회 완료 후 필요한 경우 트랜잭션 데이터도 갱신
                    launch { preloadTransactionData(false) }
                }
            } else {
                Log.d(
                    TAG,
                    "onResume: 잔액 갱신 건너뜀 (마지막 갱신 후 ${(currentTime - lastBalanceUpdateTime) / 1000}초)"
                )
            }
        }
    }

    // 충전 후 잔액을 강제로 갱신하는 메소드 (다른 화면에서 호출 가능)
    // 충전 후 잔액을 강제로 갱신하는 메소드 (다른 화면에서 호출 가능)
    fun refreshBalanceAfterCharge() {
        if (!isAdded || _binding == null) return

        viewLifecycleOwner.lifecycleScope.launch {
            // 1. "갱신 중..." 표시 (선택적)
            binding.moneyCount.text = "갱신 중..."

            // 2. 강제 갱신 시도
            try {
                val context = context ?: return@launch
                val manager = UserSession.getBlockchainManagerIfAvailable(context)

                if (manager != null) {
                    // 3. 캐시를 무시하고 새로운 잔액 조회
                    // 참고: BlockChainManager에 forceRefreshBalance 메소드가 추가되어 있어야 함
                    // 없다면 아래 코드 사용:
                    withContext(Dispatchers.IO) {
                        // BlockChainManager에 강제 갱신 메소드가 없다면 다음과 같이 처리
                        val newBalance = manager.getMyCatTokenBalance()
                        UserSession.lastKnownBalance = newBalance
                        lastBalanceUpdateTime = System.currentTimeMillis()

                        // 4. UI 업데이트
                        withContext(Dispatchers.Main) {
                            if (isAdded && _binding != null) {
                                updateBalanceDisplay(newBalance)
                                Toast.makeText(context, "잔액이 갱신되었습니다", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "강제 잔액 갱신 실패", e)
                // 실패 시 이전 잔액으로 복원
                UserSession.lastKnownBalance?.let {
                    updateBalanceDisplay(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}