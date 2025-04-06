package com.example.second_project.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class ChargeFragment : Fragment() {
    private var _binding: FragmentChargeBinding? = null
    private val binding get() = _binding!!

    private var selectedAmount: Int = 5000
    private var currentBalance: BigInteger = BigInteger.ZERO
    private var isCharging = false // ✅ 중복 전송 방지용 flag
    private val decimalFormat = DecimalFormat("#,###")
    private var isTextWatcherActive = false

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

        selectedAmount = getAmountFromRadioButton()
        val num = decimalFormat.format(selectedAmount)
        binding.chargeInput.setText(num)

        // 잔액 불러오기
        val manager = UserSession.getBlockchainManagerIfAvailable(requireContext())
        if (manager != null) {
            Thread {
                try {
                    val balance = manager.getMyCatTokenBalance()
                    currentBalance = balance
                    requireActivity().runOnUiThread {
                        updateChargeOutput(selectedAmount)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "잔액 조회 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        // 라디오 버튼 → 입력창 자동 입력
        binding.chargeOptions.setOnCheckedChangeListener { _, _ ->
            selectedAmount = getAmountFromRadioButton()
            val finalSelectedAmount = decimalFormat.format(selectedAmount)
            binding.chargeInput.setText(finalSelectedAmount)
            updateChargeOutput(selectedAmount)
        }

        // 직접 입력 시 콤마 자동 적용 및 충전단위 업데이트
        binding.chargeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                if (isTextWatcherActive) return
                
                isTextWatcherActive = true
                
                // 콤마 제거 후 숫자만 추출
                val cleanString = s.toString().replace(",", "")
                
                // 숫자로 변환
                val amount = cleanString.toIntOrNull() ?: 0
                
                // 콤마 적용된 문자열 생성
                val formattedAmount = if (amount > 0) decimalFormat.format(amount) else ""
                
                // 현재 커서 위치 저장
                val cursorPosition = binding.chargeInput.selectionStart
                
                // 텍스트 설정
                binding.chargeInput.setText(formattedAmount)
                
                // 커서 위치 조정 (콤마 추가로 인한 위치 변화 보정)
                val newCursorPosition = if (cursorPosition > 0) {
                    val oldLength = s?.length ?: 0
                    val newLength = formattedAmount.length
                    val diff = newLength - oldLength
                    cursorPosition + diff
                } else {
                    formattedAmount.length
                }
                
                // 커서 위치 설정
                binding.chargeInput.setSelection(newCursorPosition.coerceIn(0, formattedAmount.length))
                
                // 충전단위 업데이트
                updateChargeOutput(amount)
                
                isTextWatcherActive = false
            }
        })

        // 결제하기 버튼
        binding.purchaseBtn.setOnClickListener {

            if (isCharging) {
                Toast.makeText(requireContext(), "충전 진행 중입니다!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 콤마 제거 후 숫자로 변환
            val cleanString = binding.chargeInput.text.toString().replace(",", "")
            val amount = cleanString.toIntOrNull()
            
            if (amount == null || amount <= 0) {
                Toast.makeText(requireContext(), "올바른 금액을 입력하세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val limit = BigInteger.valueOf(1_000_000)
            val total = currentBalance + amount.toBigInteger()
            // ★ 1) 100만 초과 여부 체크
            if (total > limit) {
                // 다이얼로그를 띄운 뒤 결제 요청 중단
                AlertDialog.Builder(requireContext())
                    .setTitle("충전 불가")
                    .setMessage("보유 가능 CAT은 최대 1,000,000까지입니다.\n현재 충전으로 초과됩니다.")
                    .setPositiveButton("확인", null)
                    .show()
                return@setOnClickListener
            }

            isCharging = true
            binding.purchaseBtn.isEnabled = false
            binding.purchaseBtn.text = "충전 중..."

            // ★ 2) 여기서부터 실제 충전 로직
            val request = DepositRequest(
                userId = UserSession.userId,
                quantity = amount
            )

            val service = ApiClient.retrofit.create(PaymentApiService::class.java)
            service.deposit(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    isCharging = false
                    binding.purchaseBtn.isEnabled = true
                    binding.purchaseBtn.text = "충전하기"

                    if (response.isSuccessful) {
                        currentBalance += amount.toBigInteger()
                        updateChargeOutput(amount)
                        Toast.makeText(requireContext(), "충전 완료!", Toast.LENGTH_SHORT).show()
                    } else {
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

                    Toast.makeText(requireContext(), "통신 오류: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }


        // 닫기 버튼
        binding.purchaseCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getAmountFromRadioButton(): Int {
        return when (binding.chargeOptions.checkedRadioButtonId) {
            R.id.chargeOption1 -> 5000
            R.id.chargeOption2 -> 10000
            R.id.chargeOption3 -> 50000
            R.id.chargeOption4 -> 100000
            else -> 0
        }
    }

    private fun updateChargeOutput(amount: Int) {
        val finalAmount = decimalFormat.format(amount)
        binding.chargeOutput.text = "$finalAmount  CAT"
        val total = currentBalance + BigInteger.valueOf(amount.toLong())
        val finalTotal = decimalFormat.format(total)
        if (total.toInt() > 1000000) {
            binding.chargeResult.text = "1,000,000 CAT을 초과하여 충전할 수 없습니다."
            binding.purchaseBtn.isEnabled = false

        } else {
            binding.chargeResult.text = "충전 후 ${finalTotal} CAT 보유 예상"
            binding.purchaseBtn.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
