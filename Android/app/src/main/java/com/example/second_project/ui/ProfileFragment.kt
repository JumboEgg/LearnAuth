package com.example.second_project.ui
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.second_project.viewmodel.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 사용자 이름 표시
        binding.textName.text = "${UserSession.nickname}님,"

        // 메뉴 버튼 클릭 리스너 설정
        setupMenuListeners()

        // 지갑 정보 로그
        Log.d("ProfileFragment", "지갑 정보 확인: walletFilePath=${UserSession.walletFilePath}, walletPassword=${UserSession.walletPassword}")

        // 지갑 파일 존재 여부 확인 및 처리
        handleWalletFile()

        // 잔액 조회 및 표시
        loadBalance()
    }

    // 메뉴 버튼 클릭 리스너 설정
    private fun setupMenuListeners() {
        // profileMenu1 -> MyWalletFragment 이동
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

    // 지갑 파일 처리
    private fun handleWalletFile() {
        if (!UserSession.walletFilePath.isNullOrEmpty()) {
            val walletFile = File(requireContext().filesDir, UserSession.walletFilePath)
            Log.d("ProfileFragment", "지갑 파일 존재 여부: ${walletFile.exists()}, 경로: ${walletFile.absolutePath}")

            // 지갑 파일이 없으면서 이더리움 주소 형식이라면
            if (!walletFile.exists() && UserSession.walletFilePath?.startsWith("0x") == true) {
                // 지갑 파일 찾기
                val walletFiles = requireContext().filesDir.listFiles { file ->
                    file.name.startsWith("UTC--") && file.name.endsWith(".json")
                }

                if (walletFiles != null && walletFiles.isNotEmpty()) {
                    // 찾은 지갑 파일 사용
                    val walletFileName = walletFiles[0].name
                    UserSession.walletFilePath = walletFileName
                    Log.d("ProfileFragment", "✅ 지갑 파일을 찾아 설정했습니다: $walletFileName")
                } else {
                    Log.e("ProfileFragment", "⚠️ 지갑 파일을 찾을 수 없습니다!")
                }
            }
        }
    }

    // 잔액 로드
    private fun loadBalance() {
        val manager = UserSession.getBlockchainManagerIfAvailable(requireContext())
        if (manager != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // 지갑 주소 가져오기
                    val address = withContext(Dispatchers.IO) { manager.getMyWalletAddress() }
                    Log.d("ProfileFragment", "📍 내 지갑 주소: $address")

                    // wei 단위의 토큰 잔액 가져오기
                    val balanceInWei = withContext(Dispatchers.IO) { manager.getMyCatTokenBalance() }
                    Log.d("ProfileFragment", "💰 CATToken 잔액(wei): $balanceInWei")

                    // UserSession에 마지막 잔액 저장 (나중에 참조 가능)
                    UserSession.lastKnownBalance = balanceInWei

                    // 잔액 포맷팅 및 표시
                    updateBalanceDisplay(balanceInWei)

                } catch (e: Exception) {
                    Log.e("ProfileFragment", "잔액 조회 실패", e)
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "잔액 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.w("ProfileFragment", "지갑 정보가 없습니다. 로그인 다시 해주세요")
            Toast.makeText(requireContext(), "지갑 정보가 없습니다. 로그인을 다시 해주세요", Toast.LENGTH_SHORT).show()
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
        Log.d("ProfileFragment", "💰 표시용 CATToken 잔액: $formattedBalance")

        // UI 업데이트
        binding.moneyCount.text = "$formattedBalance CAT"
    }

    // 로그아웃 처리
    private fun logout() {
        val refreshToken = UserSession.refreshToken
        if (refreshToken.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Refresh token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("ProfileFragment", "Stored refreshToken: $refreshToken")
        val authApiService = ApiClient.retrofit.create(AuthApiService::class.java)

        authApiService.logout(refreshToken).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 로그아웃 성공: 세션 초기화 후 LoginActivity로 이동
                    UserSession.clear()
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

    // 화면이 다시 보일 때마다 잔액 새로고침
    override fun onResume() {
        super.onResume()
        Log.d("ProfileFragment", "onResume 호출됨 - 잔액 갱신 시도")
        refreshBalance()
    }

    // 잔액 새로고침 메서드
    private fun refreshBalance() {
        val manager = UserSession.getBlockchainManagerIfAvailable(requireContext())
        if (manager != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // 최신 잔액 가져오기
                    val balanceInWei = withContext(Dispatchers.IO) { manager.getMyCatTokenBalance() }
                    Log.d("ProfileFragment", "새로고침된 잔액(wei): $balanceInWei")

                    // UserSession에 마지막 잔액 업데이트
                    UserSession.lastKnownBalance = balanceInWei

                    // 잔액 표시 업데이트
                    updateBalanceDisplay(balanceInWei)

                } catch (e: Exception) {
                    Log.e("ProfileFragment", "잔액 새로고침 실패", e)
                    // 오류가 발생해도 사용자에게는 토스트 메시지를 표시하지 않음 (onResume에서 자동 호출되므로)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}