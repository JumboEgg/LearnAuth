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

    private var isCharging = false // 중복 전송 방지용 flag
    private var isOverlayVisible = false

    private lateinit var backCallback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChargeBinding.inflate(inflater, container, false)
        return binding.root
    }


    // ChargeFragment에 handleWalletFile 함수 추가 및 onViewCreated 수정
    // ---------------------------------------------
    // onViewCreated
    // ---------------------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        // 2) 기본 값 세팅
        selectedBaseAmount = getBaseAmountFromRadioButton()
        binding.chargeInput.setText(selectedBaseAmount.toString())

        Log.d("ChargeFragment", "onViewCreated 시작")
        Log.d(
            "ChargeFragment",
            "지갑: ${UserSession.walletFilePath}, pw=${UserSession.walletPassword != null}"
        )
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
            backCallback.isEnabled = true
            showLoadingOverlay()
            startCatAnimation()
            handlePurchase()  // 결제 로직
        }

        // 닫기 버튼
        binding.purchaseCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    // ---------------------------------------------
    // (A) 오버레이 + 고양이 애니메이션
    // ---------------------------------------------
    private fun showLoadingOverlay() {
        isOverlayVisible = true
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoadingOverlay() {
        isOverlayVisible = false
        // 애니메이션 정지
        binding.catImageView.clearAnimation()
        // 오버레이 숨기기
        binding.loadingOverlay.visibility = View.GONE
    }

    /**
     * 고양이 ImageView를 “화면 왼쪽→오른쪽”으로만 계속 달리게 하는 메서드
     * (한 번 달린 후 애니메이션 끝나면, 다시 왼쪽으로 복귀 후 반복)
     */
    // 고양이 이미지의 랜덤 이동 애니메이션 시작 함수
    private fun startCatAnimation() {
        binding.loadingOverlay.post {
            doSingleRun()
        }
    }

    // 고양이를 랜덤 위치로 이동시키는 함수
    private fun doSingleRun() {
        if (!isOverlayVisible) return  // 오버레이가 사라졌다면 중단

        // 부모 오버레이(전체 로딩 화면)의 크기
        val parentWidth = binding.loadingOverlay.width
        val parentHeight = binding.loadingOverlay.height

        // 고양이 이미지의 크기
        val catWidth = binding.catImageView.width
        val catHeight = binding.catImageView.height

        if (parentWidth <= 0 || parentHeight <= 0 || catWidth <= 0 || catHeight <= 0) {
            // 크기를 제대로 측정하지 못한 경우, 잠시 후 재시도
            binding.loadingOverlay.postDelayed({ doSingleRun() }, 1000)
            return
        }

        // 현재 고양이 이미지의 위치 (이미 애니메이션으로 인한 이동이 있을 수 있으므로 실제 x, y 좌표 사용)
        val currentX = binding.catImageView.x
        val currentY = binding.catImageView.y

        // 고양이 이미지가 완전히 보일 수 있도록, x 좌표는 0 ~ (부모너비 - 이미지너비),
        // y 좌표는 0 ~ (부모높이 - 이미지높이) 범위 내에서 랜덤하게 생성
        val targetX = Random.nextInt(0, parentWidth - catWidth).toFloat()
        val targetY = Random.nextInt(0, parentHeight - catHeight).toFloat()

        // 현재 위치에서 타겟 위치까지의 차이(델타값)
        val deltaX = targetX - currentX
        val deltaY = targetY - currentY

        val anim = TranslateAnimation(
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, deltaX,
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, deltaY
        ).apply {
            duration = 2000  // 애니메이션 지속 시간 (2초)
            fillAfter = true  // 애니메이션 종료 후 그 위치에 그대로 둠
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    // 애니메이션 종료 후 실제 고양이 이미지의 위치 업데이트
                    binding.catImageView.clearAnimation()
                    binding.catImageView.x = targetX
                    binding.catImageView.y = targetY
                    // 오버레이가 여전히 활성화되어 있다면 다시 랜덤 이동 애니메이션 실행
                    if (isOverlayVisible) {
                        doSingleRun()
                    }
                }
            })
        }
        binding.catImageView.startAnimation(anim)
    }

    /**
     * “왼쪽→오른쪽” 단 한 번 달린 뒤, 애니메이션이 끝나면
     * 다시 왼쪽 위치로 순간 이동 & 재시작하여 계속 반복.
     */
    private fun doSingleRun(distanceX: Float) {
        if (!isOverlayVisible) return  // 이미 오버레이가 사라졌다면 중단

        // 고양이를 왼쪽 시작 위치로 초기화
        binding.catImageView.translationX = 0f

        // “왼쪽(0f) → 오른쪽(distanceX)” 한 번 이동
        val anim = TranslateAnimation(
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, distanceX,
            Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, 0f
        ).apply {
            duration = 2000  // 이동 시간 (2초 예시)
            fillAfter = true // 애니메이션 끝나면 그 위치에 유지
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    // 고양이가 오른쪽까지 도달한 뒤
                    // 다시 왼쪽으로 순간이동 후, 새 애니메이션 반복
                    binding.catImageView.post {
                        if (isOverlayVisible) {
                            // 다음 달리기 시작
                            doSingleRun(distanceX)
                        }
                    }
                }
            })
        }

        binding.catImageView.startAnimation(anim)
    }

    // ChargeFragment에 handleWalletFile 함수 추가
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
// handlePurchase 메서드 수정
    private fun handlePurchase() {
        if (isCharging) {
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

        // 충전 시작
        isCharging = true
        binding.purchaseBtn.isEnabled = false
        binding.purchaseBtn.text = "충전 중..."

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
                binding.purchaseBtn.text = "잔액 확인 중..."

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
                    updateChargeOutput(inputBase)

                    // 충전 진행 중임을 알리는 토스트 메시지
                    Toast.makeText(
                        requireContext(),
                        "충전 요청 완료! 블록체인에 반영 중...",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 블록체인에서 실제 잔액 확인 (비동기)
                    verifyBalanceAfterCharge(depositAmountWei, inputBase)
                } else {
                    Log.e("ChargeFragment", "❌ 충전 API 호출 실패: ${response.code()}")
                    Toast.makeText(
                        requireContext(),
                        "충전 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 충전 실패 시 상태 초기화
                    isCharging = false
                    backCallback.isEnabled = false
                    binding.purchaseBtn.isEnabled = true
                    binding.purchaseBtn.text = "충전하기"
                    hideLoadingOverlay()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                isCharging = false
                // 충전 실패 시에도 뒤로가기 버튼 복원
                backCallback.isEnabled = false
                binding.purchaseBtn.isEnabled = true
                binding.purchaseBtn.text = "충전하기"
                hideLoadingOverlay()

                Log.e("ChargeFragment", "❌ 충전 API 통신 오류: ${t.message}")
                t.printStackTrace()
                Toast.makeText(requireContext(), "통신 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 충전 후 잔액 확인 및 UserSession에 저장
// verifyBalanceAfterCharge 메서드 수정
    private fun verifyBalanceAfterCharge(depositAmountWei: BigInteger, inputBase: Int) {
        val manager = UserSession.getBlockchainManagerIfAvailable(requireContext())
        if (manager != null) {
            Thread {
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
                                Thread.sleep(100)  // 기존 1초보다 훨씬 짧은 지연
                                retryCount++
                            }
                        } catch (e: Exception) {
                            Log.e("ChargeFragment", "잔액 조회 시도 ${retryCount + 1} 실패: ${e.message}")
                            Thread.sleep(100)
                            retryCount++
                        }
                    }

                    // 최종 결과 처리
                    if (success && actualBalance != null) {
                        Log.d("ChargeFragment", "충전 후 실제 잔액(wei): $actualBalance")
                        Log.d("ChargeFragment", "기대 잔액(wei): ${currentBalance}")
                        Log.d("ChargeFragment", "잔액 일치 여부: ${actualBalance >= currentBalance}")

                        // 최신 잔액을 UserSession에 저장
                        UserSession.lastKnownBalance = actualBalance

                        requireActivity().runOnUiThread {
                            if (isAdded && _binding != null) {
                                Toast.makeText(
                                    requireContext(),
                                    "충전이 완료되었습니다. 잔액이 업데이트 되었습니다.",
                                    Toast.LENGTH_LONG
                                ).show()

                                backCallback.isEnabled = false
                                binding.root.postDelayed({
                                    if (isAdded && !isRemoving) {
                                        requireActivity().onBackPressedDispatcher.onBackPressed()
                                    }
                                }, 100)
                            }
                        }
                    } else {
                        Log.e("ChargeFragment", "충전 후 잔액 확인 실패 또는 불일치")
                        requireActivity().runOnUiThread {
                            if (isAdded && _binding != null) {
                                Toast.makeText(
                                    requireContext(),
                                    "충전은 완료되었으나 잔액 확인에 지연이 있을 수 있습니다.",
                                    Toast.LENGTH_LONG
                                ).show()
                                backCallback.isEnabled = false
                                binding.root.postDelayed({
                                    if (isAdded && !isRemoving) {
                                        requireActivity().onBackPressedDispatcher.onBackPressed()
                                    }
                                }, 100)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChargeFragment", "충전 후 잔액 확인 실패: ${e.message}")
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        if (isAdded && _binding != null) {
                            Toast.makeText(
                                requireContext(),
                                "충전은 완료되었으나 잔액 확인 중 오류가 발생했습니다.",
                                Toast.LENGTH_LONG
                            ).show()
                            backCallback.isEnabled = false
                            binding.root.postDelayed({
                                if (isAdded && !isRemoving) {
                                    requireActivity().onBackPressedDispatcher.onBackPressed()
                                }
                            }, 100)
                        }
                    }
                }
            }.start()
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
        binding.catImageView.clearAnimation()
        _binding = null
    }
}