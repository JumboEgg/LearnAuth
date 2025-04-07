package com.example.second_project.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.databinding.FragmentChargeBinding
import java.math.BigInteger
import com.example.second_project.data.model.dto.request.DepositRequest
import com.example.second_project.network.ApiClient
import com.example.second_project.network.PaymentApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChargeFragment : Fragment() {
    private var _binding: FragmentChargeBinding? = null
    private val binding get() = _binding!!

    // 기본 금액(단위: CAT, 예: 5000)은 Int로 보관 (UI에 그대로 보여짐)
    private var selectedBaseAmount: Int = 5000

    // currentBalance는 이미 wei 단위로 관리 (10^18 단위)
    private var currentBalance: BigInteger = BigInteger.ZERO

    private var isCharging = false // 중복 전송 방지용 flag

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChargeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 디버깅 로그 추가
        Log.d("ChargeFragment", "onViewCreated 시작")
        Log.d(
            "ChargeFragment",
            "지갑 정보: walletFilePath=${UserSession.walletFilePath}, walletPassword=${UserSession.walletPassword != null}"
        )

        // 초기 기본 금액 설정 (UI에는 5000 CAT으로 보임)
        selectedBaseAmount = getBaseAmountFromRadioButton()
        binding.chargeInput.setText(selectedBaseAmount.toString())

        // 잔액 불러오기
        loadCurrentBalance()

        // 라디오 버튼 선택 시 기본 금액 업데이트
        binding.chargeOptions.setOnCheckedChangeListener { _, _ ->
            selectedBaseAmount = getBaseAmountFromRadioButton()
            binding.chargeInput.setText(selectedBaseAmount.toString())
            updateChargeOutput(selectedBaseAmount)
        }

        // 결제하기 버튼 클릭 시
        binding.purchaseBtn.setOnClickListener {
            handlePurchase()
        }

        // 닫기 버튼 클릭 시
        binding.purchaseCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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
        _binding = null
    }
}