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
import androidx.fragment.app.Fragment
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.data.model.dto.request.DepositRequest
import com.example.second_project.databinding.FragmentChargeBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.PaymentApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.math.BigInteger

class ChargeFragment : Fragment() {
    private var _binding: FragmentChargeBinding? = null
    private val binding get() = _binding!!

    // 기본 금액(단위: CAT, 예: 5000)은 Int로 보관 (UI에 그대로 보여짐)
    private var selectedBaseAmount: Int = 5000

    // currentBalance는 이미 wei 단위로 관리 (10^18 단위)
    private var currentBalance: BigInteger = BigInteger.ZERO

    private var isCharging = false // 중복 전송 방지용 flag
    private var isOverlayVisible = false

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

        Log.d("ChargeFragment", "onViewCreated 시작")
        Log.d(
            "ChargeFragment",
            "지갑: ${UserSession.walletFilePath}, pw=${UserSession.walletPassword != null}"
        )

        // 1) 지갑 파일 처리
        handleWalletFile()

        // 2) 기본 값 세팅
        selectedBaseAmount = getBaseAmountFromRadioButton()
        binding.chargeInput.setText(selectedBaseAmount.toString())

        // 3) 잔액 불러오기
        loadCurrentBalance()

        // 라디오 버튼 변경 시
        binding.chargeOptions.setOnCheckedChangeListener { _, _ ->
            selectedBaseAmount = getBaseAmountFromRadioButton()
            binding.chargeInput.setText(selectedBaseAmount.toString())
            updateChargeOutput(selectedBaseAmount)
        }

        // 결제 버튼
        binding.purchaseBtn.setOnClickListener {
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
    private fun startCatAnimation() {
        // 레이아웃 파악 후에 계산하기 위해 post 사용
        binding.loadingOverlay.post {
            // 현재 오버레이 폭
            val parentWidth = binding.loadingOverlay.width
            // 고양이 뷰 폭
            val catWidth = binding.catImageView.width

            if (parentWidth == 0 || catWidth == 0) {
                Log.w("ChargeFragment", "화면/고양이 폭 측정 실패 → 기본 이동값 사용")
                doSingleRun(600f) // 임시 하드코딩
            } else {
                val distanceX = (parentWidth - catWidth).toFloat()
                doSingleRun(distanceX)
            }
        }
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

    // 현재 잔액 로드
    private fun loadCurrentBalance() {
        val manager = UserSession.getBlockchainManagerIfAvailable(requireContext())
        if (manager != null) {
            Thread {
                try {
                    // 지갑 주소 확인 (디버깅용)
                    val address = manager.getMyWalletAddress()
                    Log.d("ChargeFragment", "충전 대상 지갑 주소: $address")

                    // wei 단위의 잔액 가져오기
                    val balance = manager.getMyCatTokenBalance()
                    Log.d("ChargeFragment", "현재 잔액(wei): $balance")

                    currentBalance = balance
                    requireActivity().runOnUiThread {
                        updateChargeOutput(selectedBaseAmount)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("ChargeFragment", "잔액 조회 실패: ${e.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "잔액 조회 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        } else {
            Log.e("ChargeFragment", "BlockchainManager 초기화 실패 - 지갑 정보를 확인하세요")
            Toast.makeText(requireContext(), "지갑 정보 초기화 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // 구매 버튼 처리
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
                isCharging = false
                binding.purchaseBtn.isEnabled = true
                binding.purchaseBtn.text = "충전하기"

                // 응답 로깅
                Log.d("ChargeFragment", "충전 API 응답 코드: ${response.code()}")
                try {
                    val errorBody = response.errorBody()?.string()
                    if (!errorBody.isNullOrEmpty()) {
                        Log.d("ChargeFragment", "응답 상세: $errorBody")
                    }
                } catch (e: Exception) {
                }
                hideLoadingOverlay() // 결제 끝 → 무조건 오버레이 숨김

                if (response.isSuccessful) {
                    Log.d("ChargeFragment", "✅ 충전 API 호출 성공")

                    // 충전 성공 시 현재 잔액 업데이트
                    currentBalance = currentBalance.add(depositAmountWei)
                    updateChargeOutput(inputBase)

                    // 성공 메시지
                    Toast.makeText(requireContext(), "충전 완료!", Toast.LENGTH_SHORT).show()

                    // 블록체인에서 실제 잔액 확인 (비동기)
                    verifyBalanceAfterCharge(depositAmountWei, inputBase)
                } else {
                    Log.e("ChargeFragment", "❌ 충전 API 호출 실패: ${response.code()}")
                    Toast.makeText(
                        requireContext(),
                        "충전 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                isCharging = false
                binding.purchaseBtn.isEnabled = true
                binding.purchaseBtn.text = "충전하기"
                hideLoadingOverlay()

                Log.e("ChargeFragment", "❌ 충전 API 통신 오류: ${t.message}")
                t.printStackTrace()

                Toast.makeText(requireContext(), "통신 오류: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    // 충전 후 잔액 확인 및 UserSession에 저장
    private fun verifyBalanceAfterCharge(depositAmountWei: BigInteger, inputBase: Int) {
        val manager = UserSession.getBlockchainManagerIfAvailable(requireContext())
        if (manager != null) {
            Thread {
                try {
                    // 블록체인에 반영될 시간을 주기 위해 잠시 대기
                    Thread.sleep(1500)

                    // 실제 블록체인 잔액 확인
                    val actualBalance = manager.getMyCatTokenBalance()
                    Log.d("ChargeFragment", "충전 후 실제 잔액(wei): $actualBalance")
                    Log.d("ChargeFragment", "기대 잔액(wei): ${currentBalance}")
                    Log.d("ChargeFragment", "잔액 일치 여부: ${actualBalance == currentBalance}")

                    // UserSession에 마지막 잔액 저장 - 이것이 핵심!
                    UserSession.lastKnownBalance = actualBalance

                    // UI 스레드에서 작업
                    requireActivity().runOnUiThread {
                        // 충전 완료 메시지 강화
                        Toast.makeText(
                            requireContext(),
                            "충전이 완료되었습니다. 잔액이 업데이트 되었습니다.",
                            Toast.LENGTH_LONG
                        ).show()

                        // 3초 후 자동으로 이전 화면으로 돌아가기 (선택사항)
                        binding.root.postDelayed({
                            if (isAdded && !isRemoving) { // Fragment가 아직 유효한 경우에만
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }, 3000)
                    }
                } catch (e: Exception) {
                    Log.e("ChargeFragment", "충전 후 잔액 확인 실패: ${e.message}")
                    e.printStackTrace()
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
        // 사용자가 입력한 기본 금액(예: 5000 CAT)은 그대로 표시
        binding.chargeOutput.text = "$amount CAT"

        val tokenUnit = BigInteger.TEN.pow(18)

        // currentBalance는 이미 wei 단위이므로, 충전 금액도 wei로 변환하여 합산
        val totalWei = currentBalance.add(BigInteger.valueOf(amount.toLong()).multiply(tokenUnit))
        val displayTotal = totalWei.divide(tokenUnit)

        binding.chargeResult.text = "충전 후 ${displayTotal} CAT 보유 예상"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.catImageView.clearAnimation()
        _binding = null
    }
}