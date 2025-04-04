package com.example.second_project.ui

import android.os.Bundle
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

    private var selectedAmount: Int = 5000
    private var currentBalance: BigInteger = BigInteger.ZERO

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
        binding.chargeInput.setText(selectedAmount.toString())

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
            binding.chargeInput.setText(selectedAmount.toString())
            updateChargeOutput(selectedAmount)
        }

        // 결제하기 버튼
        binding.purchaseBtn.setOnClickListener {
            val amount = binding.chargeInput.text.toString().toIntOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(requireContext(), "올바른 금액을 입력하세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = DepositRequest(
                userId = UserSession.userId,
                quantity = amount
            )

            val service = ApiClient.retrofit.create(PaymentApiService::class.java)

            service.deposit(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
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
        binding.chargeOutput.text = "$amount CAT"
        val total = currentBalance + BigInteger.valueOf(amount.toLong())
        binding.chargeResult.text = "충전 후 ${total} CAT 보유 예상"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
