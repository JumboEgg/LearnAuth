package com.example.second_project.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.data.model.dto.request.DepositRequest
import com.example.second_project.databinding.FragmentChargeBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.PaymentApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

class ChargeFragment : Fragment() {
    private var _binding: FragmentChargeBinding? = null
    private val binding get() = _binding!!

    // 기본 금액(단위: CAT, 예: 5000)은 Int로 보관 (UI에 그대로 보여짐)
    private var selectedBaseAmount: Int = 5000

    // currentBalance는 이미 wei 단위로 관리 (10^18 단위)
    private var currentBalance: BigInteger = BigInteger.ZERO

    // 상태를 UserSession으로 이동하여 Fragment 생명주기와 독립적으로 유지
    companion object {
        // 정적 변수로 충전 상태 관리
        var isChargingInProgress: Boolean
            get() = UserSession.isCharging
            set(value) {
                UserSession.isCharging = value
            }
    }

    private var isOverlayVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChargeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ---------------------------------------------
    // onViewCreated
    // ---------------------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2) 기본 값 세팅
        selectedBaseAmount = getBaseAmountFromRadioButton()
        binding.chargeInput.setText(selectedBaseAmount.toString())

        Log.d("ChargeFragment", "onViewCreated 시작")
        Log.d(
            "ChargeFragment",
            "지갑: ${UserSession.walletFilePath}, pw=${UserSession.walletPassword != null}"
        )

        // 충전 상태에 따라 UI 복원
        restoreChargingState()

        binding.root.post {
            // 지갑 파일 처리는 Dispatchers.IO에서 실행하여 메인 스레드 차단 없이 진행
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                handleWalletFile()
            }

            // 잔액 조회도 코루틴을 통해 백그라운드에서 진행 (loadCurrentBalance() 내부도 이미 코루틴 사용)
            loadCurrentBalance()
        }

        // 라디오 버튼 변경 시
        binding.chargeOptions.setOnCheckedChangeListener { _, _ ->
            selectedBaseAmount = getBaseAmountFromRadioButton()
            binding.chargeInput.setText(selectedBaseAmount.toString())
            updateChargeOutput(selectedBaseAmount)
        }

        // 결제 버튼
        binding.purchaseBtn.setOnClickListener {
            showLoadingOverlay()
            handlePurchase()  // 결제 로직
        }

        // 닫기 버튼
        binding.purchaseCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    // 충전 상태 복원 메서드 추가
    private fun restoreChargingState() {
        if (isChargingInProgress) {
            // 충전 중이면 UI 상태 복원
            binding.purchaseBtn.isEnabled = false
            binding.purchaseBtn.text = "충전 중..."
            showLoadingOverlay()
        }
    }

    // ---------------------------------------------
    // (A) 오버레이 + 고양이 애니메이션
    // ---------------------------------------------
    private fun showLoadingOverlay() {
        isOverlayVisible = true
        // 애니메이션 시작 코드...
    }

    private fun hideLoadingOverlay() {
        isOverlayVisible = false
        // 애니메이션 정지
    }

    private fun handleWalletFile() {
        if (!UserSession.walletFilePath.isNullOrEmpty()) {
            Log.d("ChargeFragment", "현재 지갑 경로: ${UserSession.walletFilePath}")
            // 이더리움 주소 형식인지 확인 (0x로 시작하는지)
            if (UserSession.walletFilePath?.startsWith("0x") == true) {
                Log.d(
                    "ChargeFragment",
                    "walletFilePath가 이더리움 주소 형식입니다: ${UserSession.walletFilePath}"
                )
                val ethAddress = UserSession.walletFilePath
                // 지갑 파일 찾기
                val walletFiles = requireContext().filesDir.listFiles { file ->
                    file.name.startsWith("UTC--") && file.name.endsWith(".json")
                }
                if (walletFiles != null && walletFiles.isNotEmpty()) {
                    Log.d("ChargeFragment", "총 ${walletFiles.size}개의 지갑 파일을 찾았습니다.")
                    // 주소 검증을 위한 임시 변수들
                    var matchFound = false
                    var validWalletFound = false
                    // 발견된 모든 지갑 파일에 대해 검증
                    for (walletFile in walletFiles) {
                        try {
                            // 비밀번호로 지갑 검증 시도
                            val credentials = org.web3j.crypto.WalletUtils.loadCredentials(
                                UserSession.walletPassword,
                                walletFile
                            )
                            val walletAddress = credentials.address
                            // 주소가 DB 저장 주소와 일치하는지 확인
                            if (walletAddress.equals(ethAddress, ignoreCase = true)) {
                                Log.d(
                                    "ChargeFragment",
                                    "✅ 일치하는 지갑 파일을 발견: ${walletFile.name}, 주소: $walletAddress"
                                )
                                UserSession.walletFilePath = walletFile.name
                                matchFound = true
                                validWalletFound = true
                                break  // 일치하는 지갑을 찾았으므로 검색 종료
                            } else {
                                Log.d(
                                    "ChargeFragment",
                                    "주소가 일치하지 않는 지갑 파일: ${walletFile.name}, 주소: $walletAddress"
                                )
                                validWalletFound = true
                            }
                        } catch (e: Exception) {
                            // 이 지갑 파일은 비밀번호가 맞지 않거나 손상되었을 수 있음
                            Log.d(
                                "ChargeFragment",
                                "지갑 파일 검증 실패: ${walletFile.name}, 오류: ${e.message}"
                            )
                        }
                    }
                    // 검증 결과에 따른 처리
                    if (!matchFound) {
                        if (validWalletFound) {
                            // 검증 가능한 지갑은 있지만 주소가 일치하지 않음
                            Log.w(
                                "ChargeFragment",
                                "⚠️ DB 주소와 일치하는 지갑 파일이 없습니다. DB 주소를 계속 사용합니다: $ethAddress"
                            )
                            UserSession.walletFilePath = ethAddress  // DB의 이더리움 주소를 그대로 유지
                        } else {
                            // 모든 지갑 파일이 검증 불가능
                            Log.w(
                                "ChargeFragment",
                                "⚠️ 검증 가능한 지갑 파일이 없습니다. DB 주소를 계속 사용합니다: $ethAddress"
                            )
                            UserSession.walletFilePath = ethAddress  // DB의 이더리움 주소를 그대로 유지
                        }
                    }
                } else {
                    // 지갑 파일이 없는 경우
                    Log.d("ChargeFragment", "지갑 파일을 찾을 수 없습니다. DB 주소를 계속 사용합니다: $ethAddress")
                    // 이더리움 주소를 그대로 유지
                    UserSession.walletFilePath = ethAddress
                }
            } else {
                // 일반 파일 경로인 경우 (UTC--)
                val walletFile = File(requireContext().filesDir, UserSession.walletFilePath)
                if (!walletFile.exists()) {
                    Log.w("ChargeFragment", "⚠️ 지갑 파일을 찾을 수 없습니다: ${UserSession.walletFilePath}")
                    // 지갑 파일이 없는 경우 다른 지갑 파일 찾기 시도
                    val walletFiles = requireContext().filesDir.listFiles { file ->
                        file.name.startsWith("UTC--") && file.name.endsWith(".json")
                    }
                    if (walletFiles != null && walletFiles.isNotEmpty()) {
                        // 첫 번째 지갑 파일 사용
                        val walletFileName = walletFiles[0].name
                        Log.d("ChargeFragment", "✅ 대체 지갑 파일을 찾았습니다: $walletFileName")
                        UserSession.walletFilePath = walletFileName
                    } else {
                        Log.e("ChargeFragment", "⚠️ 사용 가능한 지갑 파일이 없습니다!")
                    }
                } else {
                    Log.d("ChargeFragment", "✅ 지갑 파일이 존재합니다: ${walletFile.absolutePath}")
                    // 지갑 파일 유효성 검증 (선택사항)
                    try {
                        val credentials = org.web3j.crypto.WalletUtils.loadCredentials(
                            UserSession.walletPassword,
                            walletFile
                        )
                        Log.d("ChargeFragment", "✅ 지갑 검증 성공, 주소: ${credentials.address}")
                    } catch (e: Exception) {
                        Log.w("ChargeFragment", "⚠️ 지갑 파일 검증 실패: ${e.message}")
                        // 비밀번호가 틀려도 경로는 유지
                    }
                }
            }
        } else {
            Log.e("ChargeFragment", "⚠️ 지갑 경로가 비어있습니다!")
        }
    }

    private fun loadCurrentBalance() {
        val manager = UserSession.getBlockchainManagerIfAvailable(requireContext())
        if (manager != null) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val address = manager.getMyWalletAddress()
                    Log.d("ChargeFragment", "충전 대상 지갑 주소: $address")
                    val balance = manager.getMyCatTokenBalance()
                    Log.d("ChargeFragment", "현재 잔액(wei): $balance")
                    currentBalance = balance
                    // UI 업데이트는 메인 스레드에서 처리
                    launch(Dispatchers.Main) {
                        updateChargeOutput(selectedBaseAmount)
                    }
                } catch (e: Exception) {
                    Log.e("ChargeFragment", "잔액 조회 실패: ${e.message}")
                    launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "잔액 조회 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Log.e("ChargeFragment", "BlockchainManager 초기화 실패 - 지갑 정보를 확인하세요")
            Toast.makeText(requireContext(), "지갑 정보 초기화 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // 구매 버튼 처리
    private fun handlePurchase() {
        if (isChargingInProgress) {
            Toast.makeText(requireContext(), "충전 진행 중입니다!", Toast.LENGTH_SHORT).show()
            return
        }

        val inputBase = binding.chargeInput.text.toString().toIntOrNull()
        if (inputBase == null || inputBase <= 0) {
            Toast.makeText(requireContext(), "올바른 금액을 입력하세요!", Toast.LENGTH_SHORT).show()
            return
        }

        // 1 CAT = 10^18 wei
        val tokenUnit = BigInteger.TEN.pow(18)
        // 한도를 wei 단위로 변경: 1,000,000 CAT -> 1,000,000 * 10^18
        val limit = BigInteger.valueOf(1_000_000).multiply(tokenUnit)
        // 충전할 금액 (wei 단위)
        val depositAmountWei = BigInteger.valueOf(inputBase.toLong()).multiply(tokenUnit)

        Log.d("ChargeFragment", "충전 요청 금액(CAT): $inputBase")
        Log.d("ChargeFragment", "충전 요청 금액(wei): $depositAmountWei")

        // 총액 검증
        val total = currentBalance.add(depositAmountWei)
        if (total > limit) {
            AlertDialog.Builder(requireContext())
                .setTitle("충전 불가")
                .setMessage("보유 가능 CAT은 최대 1,000,000까지입니다.\n현재 충전으로 초과됩니다.")
                .setPositiveButton("확인", null)
                .show()
            return
        }

        // 충전 시작 - UserSession에 상태 저장
        isChargingInProgress = true
        binding.purchaseBtn.isEnabled = false
        binding.purchaseBtn.text = "충전 중..."

        // 충전할 금액도 UserSession에 저장 (필요시 복원용)
        UserSession.pendingChargeAmount = depositAmountWei
        UserSession.pendingChargeAmountBase = inputBase

        // DepositRequest에 quantity는 wei 단위로 전송
        val request = DepositRequest(
            userId = UserSession.userId,
            quantity = depositAmountWei
        )

        // API 호출
        val service = ApiClient.retrofit.create(PaymentApiService::class.java)
        service.deposit(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // 충전 중 상태 유지 (잔액 확인 완료 시 verifyBalanceAfterCharge에서 해제)
                if (isAdded && _binding != null) {
                    binding.purchaseBtn.text = "잔액 확인 중..."
                }

                try {
                    val errorBody = response.errorBody()?.string()
                    if (!errorBody.isNullOrEmpty()) {
                        Log.d("ChargeFragment", "응답 상세: $errorBody")
                    }
                } catch (e: Exception) {
                }

                if (response.isSuccessful) {
                    Log.d("ChargeFragment", "✅ 충전 API 호출 성공")
                    // 서버 응답 즉시 메모리 상의 잔액 업데이트 (UI는 즉시 반영)
                    currentBalance = currentBalance.add(depositAmountWei)

                    if (isAdded && _binding != null) {
                        updateChargeOutput(inputBase)
                        // 충전 진행 중임을 알리는 토스트 메시지
                        Toast.makeText(
                            requireContext(),
                            "충전 요청 완료! 블록체인에 반영 중...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // 블록체인에서 실제 잔액 확인 (비동기)
                    verifyBalanceAfterCharge(depositAmountWei, inputBase)
                } else {
                    Log.e("ChargeFragment", "❌ 충전 API 호출 실패: ${response.code()}")

                    // 충전 실패 시 상태 초기화
                    isChargingInProgress = false
                    UserSession.pendingChargeAmount = null
                    UserSession.pendingChargeAmountBase = null

                    if (isAdded && _binding != null) {
                        Toast.makeText(
                            requireContext(),
                            "충전 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()

                        binding.purchaseBtn.isEnabled = true
                        binding.purchaseBtn.text = "충전하기"
                        hideLoadingOverlay()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                isChargingInProgress = false
                UserSession.pendingChargeAmount = null
                UserSession.pendingChargeAmountBase = null

                Log.e("ChargeFragment", "❌ 충전 API 통신 오류: ${t.message}")
                t.printStackTrace()

                if (isAdded && _binding != null) {
                    // 충전 실패 시에도 뒤로가기 버튼 복원
                    binding.purchaseBtn.isEnabled = true
                    binding.purchaseBtn.text = "충전하기"
                    hideLoadingOverlay()

                    Toast.makeText(requireContext(), "통신 오류: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    // 충전 후 잔액 확인 및 UserSession에 저장
    private fun verifyBalanceAfterCharge(depositAmountWei: BigInteger, inputBase: Int) {
        val manager = UserSession.getBlockchainManagerIfAvailable(
            context ?: return
        ) // requireContext() 대신 context 사용, null이면 조기 반환
        if (manager != null) {
            // Thread 대신 Application Context에서 실행될 코루틴으로 변경
            UserSession.applicationScope.launch(Dispatchers.IO) {
                try {
                    // 초기 대기 제거: 즉시 잔액 확인을 시작
                    var actualBalance: BigInteger? = null
                    var retryCount = 0
                    var success = false

                    // 최대 3회 재시도 (지연은 최소화: 100ms)
                    while (retryCount < 3 && !success) {
                        try {
                            actualBalance = manager.getMyCatTokenBalance()
                            Log.d(
                                "ChargeFragment",
                                "충전 후 실제 잔액(wei): $actualBalance (시도 ${retryCount + 1})"
                            )
                            if (actualBalance >= currentBalance) {
                                success = true
                            } else {
                                kotlinx.coroutines.delay(100)  // suspend 함수 사용
                                retryCount++
                            }
                        } catch (e: Exception) {
                            Log.e("ChargeFragment", "잔액 조회 시도 ${retryCount + 1} 실패: ${e.message}")
                            kotlinx.coroutines.delay(100)
                            retryCount++
                        }
                    }

                    // 최종 결과 처리
                    if (success && actualBalance != null) {

                        // 최신 잔액을 UserSession에 저장
                        UserSession.lastKnownBalance = actualBalance

                        // 충전 상태 초기화
                        isChargingInProgress = false
                        UserSession.pendingChargeAmount = null
                        UserSession.pendingChargeAmountBase = null

                        // UI 업데이트는 Main 스레드에서
                        launch(Dispatchers.Main) {
                            if (_binding != null && isAdded) {
                                Toast.makeText(
                                    requireContext(),
                                    "충전이 완료되었습니다. 잔액이 업데이트 되었습니다.",
                                    Toast.LENGTH_LONG
                                ).show()

                                // UI 복원
                                binding.purchaseBtn.isEnabled = true
                                binding.purchaseBtn.text = "충전하기"
                                hideLoadingOverlay()

                                // 화면 종료
                                if (isAdded && !isRemoving) {
                                    requireActivity().onBackPressedDispatcher.onBackPressed()
                                }
                            } else {
                                // Fragment가 없어도 충전 완료 알림 표시
                                try {
                                    Toast.makeText(
                                        UserSession.applicationContext,
                                        "충전이 완료되었습니다. 잔액이 업데이트 되었습니다.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } catch (e: Exception) {
                                    Log.e("ChargeFragment", "토스트 표시 실패: ${e.message}")
                                }
                            }
                        }
                    } else {
                        Log.e("ChargeFragment", "충전 후 잔액 확인 실패 또는 불일치")

                        // 상태는 초기화하지만 실패 표시
                        isChargingInProgress = false
                        UserSession.pendingChargeAmount = null
                        UserSession.pendingChargeAmountBase = null

                        launch(Dispatchers.Main) {
                            if (_binding != null && isAdded) {
                                Toast.makeText(
                                    requireContext(),
                                    "충전은 완료되었으나 잔액 확인에 지연이 있을 수 있습니다.",
                                    Toast.LENGTH_LONG
                                ).show()

                                // UI 복원
                                binding.purchaseBtn.isEnabled = true
                                binding.purchaseBtn.text = "충전하기"
                                hideLoadingOverlay()

                                if (isAdded && !isRemoving) {
                                    requireActivity().onBackPressedDispatcher.onBackPressed()
                                }
                            } else {
                                try {
                                    Toast.makeText(
                                        UserSession.applicationContext,
                                        "충전은 완료되었으나 잔액 확인에 지연이 있을 수 있습니다.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } catch (e: Exception) {
                                    Log.e("ChargeFragment", "토스트 표시 실패: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChargeFragment", "충전 후 잔액 확인 실패: ${e.message}")
                    e.printStackTrace()

                    // 오류 발생 시에도 상태 초기화
                    isChargingInProgress = false
                    UserSession.pendingChargeAmount = null
                    UserSession.pendingChargeAmountBase = null

                    launch(Dispatchers.Main) {
                        if (_binding != null && isAdded) {
                            Toast.makeText(
                                requireContext(),
                                "충전은 완료되었으나 잔액 확인 중 오류가 발생했습니다.",
                                Toast.LENGTH_LONG
                            ).show()

                            // UI 복원
                            binding.purchaseBtn.isEnabled = true
                            binding.purchaseBtn.text = "충전하기"
                            hideLoadingOverlay()

                            if (isAdded && !isRemoving) {
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        } else {
                            try {
                                Toast.makeText(
                                    UserSession.applicationContext,
                                    "충전은 완료되었으나 잔액 확인 중 오류가 발생했습니다.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: Exception) {
                                Log.e("ChargeFragment", "토스트 표시 실패: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    // UI에 표시할 기본 금액 (CAT 단위)을 반환하는 함수
    private fun getBaseAmountFromRadioButton(): Int {
        return when (binding.chargeOptions.checkedRadioButtonId) {
            R.id.chargeOption1 -> 5000
            R.id.chargeOption2 -> 10000
            R.id.chargeOption3 -> 50000
            R.id.chargeOption4 -> 100000
            else -> 0
        }
    }

    // 실제 DepositRequest에 사용할 금액(wei 단위)을 반환하는 함수
    private fun getAmountFromRadioButton(): BigInteger {
        val baseAmount = getBaseAmountFromRadioButton()
        return BigInteger.valueOf(baseAmount.toLong()).multiply(BigInteger.TEN.pow(18))
    }

    // 충전 정보 표시 업데이트
    private fun updateChargeOutput(amount: Int) {
        // 사용자가 선택한 기본 금액에 천 단위 콤마를 추가해 표시 (예: "5,000 CAT")
        binding.chargeOutput.text = String.format(Locale.KOREA, "%,d CAT", amount)

        val tokenUnit = BigInteger.TEN.pow(18)
        // currentBalance는 wei 단위이므로, 충전할 금액도 wei 단위로 변환 후 합산
        val totalWei = currentBalance.add(BigInteger.valueOf(amount.toLong()).multiply(tokenUnit))

        // totalWei를 BigDecimal로 변환하여 CAT 단위 계산 (18자리 정밀도)
        val totalDecimal = totalWei.toBigDecimal()
            .divide(tokenUnit.toBigDecimal(), 18, java.math.RoundingMode.HALF_UP)

        // NumberFormat 설정: 소수점 둘째 자리까지 반올림, 천 단위 콤마 포함
        val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
            roundingMode = java.math.RoundingMode.HALF_UP
        }

        val formattedTotal = numberFormat.format(totalDecimal)
        binding.chargeResult.text = "충전 후 $formattedTotal CAT 보유 예상"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Fragment가 파괴되어도 충전 상태는 유지하지만,
        // 충전이 완료된 뒤 Fragment가 소멸하면 UI 상태 초기화에 문제가 생길 수 있으므로
        // 일정 시간 후 강제로 상태 초기화
        if (isChargingInProgress) {
            // 5분(300000ms) 후에 상태 초기화
            UserSession.applicationScope.launch {
                delay(30 * 1000)
                if (isChargingInProgress) {
                    // 잔액 업데이트 다시 시도
                    val manager =
                        UserSession.getBlockchainManagerIfAvailable(UserSession.applicationContext)
                    if (manager != null) {
                        try {
                            val actualBalance = manager.getMyCatTokenBalance()
                            UserSession.lastKnownBalance = actualBalance
                        } catch (e: Exception) {
                            Log.e("ChargeFragment", "지연된 잔액 업데이트 실패: ${e.message}")
                        }
                    }
                    // 어쨌든 상태 초기화
                    isChargingInProgress = false
                    UserSession.pendingChargeAmount = null
                    UserSession.pendingChargeAmountBase = null
                }
            }
        }
    }
}