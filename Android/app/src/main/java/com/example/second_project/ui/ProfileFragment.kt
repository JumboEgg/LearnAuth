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
import com.example.second_project.databinding.FragmentProfileBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.AuthApiService
import com.example.second_project.network.CertificateApiService
import com.example.second_project.data.TransactionCache
import com.example.second_project.data.TransactionItem
import com.example.second_project.viewmodel.ProfileViewModel
import kotlinx.coroutines.Dispatchers
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
import java.text.SimpleDateFormat
import java.util.Date
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

        // moneyCount는 일단 "로딩 중..." 등의 문구
        binding.moneyCount.text = "Loading..."
        val safeContext = context ?: return

        // 2. 메뉴 버튼 리스너 등은 즉시 설정
        setupMenuListeners()

        // 3. 수료증 데이터 로드 및 프로필 이미지 설정 (우선 실행)
        viewLifecycleOwner.lifecycleScope.launch {
            loadCertificatesAndSetProfileImage()
        }

        // 4. 백그라운드에서 지갑 파일 처리 + 잔액 조회 (병렬 실행)
        viewLifecycleOwner.lifecycleScope.launch {
            // (a) 지갑 파일 처리 (handleWalletFile)도 오래 걸릴 수 있으니 Dispatchers.IO에서 처리
            withContext(Dispatchers.IO) {
                handleWalletFile() // 원래 함수 로직을 그대로 호출 (파일 스캔 등)
            }

            // (b) 블록체인 잔액 로드 (이미 분리된 메서드라면 그대로 호출)
            loadBalanceAsync()

            // 5. 추가: 트랜잭션 데이터 미리 로드 (주기적으로 확인)
            preloadTransactionData()
        }
    }

    // 메뉴 버튼 클릭 리스너 설정
    private fun setupMenuListeners() {
        // profileMenu1 -> MyWalletFragment 이동
        // (최적화: 이동하기 전에 트랜잭션 데이터 미리 로드)
        binding.profileMenu1.setOnClickListener {
            // 지갑 페이지로 이동하기 전에 데이터 로드 상태 확인
            if (TransactionCache.isEmpty() && !isPreloadingTransactions) {
                // 사용자가 명시적으로 클릭했을 때 빠른 로드 시작
                viewLifecycleOwner.lifecycleScope.launch {
                    preloadTransactionData(true)
                }
            }

            // 즉시 화면 이동 (백그라운드에서 데이터 로드 계속됨)
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

    // 잔액 로드
    private suspend fun loadBalanceAsync() = withContext(Dispatchers.IO) {
        // context가 더 이상 유효하지 않은지 먼저 확인
        if (!isAdded) {
            Log.d(
                TAG,
                "Fragment is not attached to context, cancelling balance update"
            )
            return@withContext
        }
        val context = context ?: return@withContext // null 체크 추가
        val manager = UserSession.getBlockchainManagerIfAvailable(context)
        if (manager != null) {
            try {
                // 지갑 주소 가져오기
                val address = withContext(Dispatchers.IO) { manager.getMyWalletAddress() }
                Log.d(TAG, "📍 내 지갑 주소: $address")
                // 주소 저장 (만약 아직 저장되지 않았다면)
                if (UserSession.walletAddress.isNullOrEmpty()) {
                    UserSession.walletAddress = address
                }
                // wei 단위의 토큰 잔액 가져오기
                val balanceInWei = withContext(Dispatchers.IO) { manager.getMyCatTokenBalance() }
                Log.d(TAG, "💰 CATToken 잔액(wei): $balanceInWei")
                // UserSession에 마지막 잔액 저장 (나중에 참조 가능)
                UserSession.lastKnownBalance = balanceInWei
                // UI 업데이트는 메인 스레드에서 안전하게 수행하되, Fragment가 아직 유효한지 확인
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        updateBalanceDisplay(balanceInWei)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "잔액 조회 실패", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(context, "잔액 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Log.w(TAG, "지갑 정보가 없습니다. 로그인 다시 해주세요")
            withContext(Dispatchers.Main) {
                if (isAdded && context != null) {
                    Toast.makeText(context, "지갑 정보가 없습니다. 로그인을 다시 해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 잔액 표시 업데이트
    private fun updateBalanceDisplay(balanceInWei: java.math.BigInteger) {
        // 10^18로 나누어 일반 단위로 변환
        val TOKEN_UNIT = java.math.BigInteger.TEN.pow(18)
        val displayBalance = balanceInWei.divide(TOKEN_UNIT)
        // 소수점 이하 처리 (필요한 경우)
        val remainder = balanceInWei.remainder(TOKEN_UNIT)
        val decimalPlaces = 2 // 소수점 이하 표시할 자릿수
        var decimalPart = ""
        if (remainder > java.math.BigInteger.ZERO) {
            // 소수점 이하 계산
            val remainderString = remainder.toString().padStart(18, '0')
            decimalPart = "." + remainderString.substring(
                0,
                Math.min(decimalPlaces, remainderString.length)
            ).trimEnd('0')
        }
        // 최종 표시 잔액
        val formattedBalance = "${displayBalance}${decimalPart}"
        Log.d(TAG, "💰 표시용 CATToken 잔액: $formattedBalance")
        // UI 업데이트
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
                val certificateCount = certificateResponse.data.count { it.certificate > 0 }

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

    override fun onResume() {
        super.onResume()
        // Fragment가 아직 활성 상태인 경우에만 코루틴 시작
        if (isAdded && view != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                // 백그라운드에서 잔액 가져오고 UI 업데이트
                loadBalanceAsync()

                // 백그라운드에서 트랜잭션 데이터 미리 로드 (지갑 화면으로 이동 전 준비)
                preloadTransactionData()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}