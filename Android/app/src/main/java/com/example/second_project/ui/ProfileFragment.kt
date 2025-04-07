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
import com.example.second_project.viewmodel.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.WalletUtils
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 1. 사용자 이름 등 즉시 표시 (UI 먼저 보여줌)
        binding.textName.text = "${UserSession.nickname}님,"
        // moneyCount는 일단 "로딩 중..." 등의 문구
        binding.moneyCount.text = "Loading..."
        val safeContext = context ?: return
        // 2. 메뉴 버튼 리스너 등은 즉시 설정
        setupMenuListeners()
        // 3. 백그라운드에서 지갑 파일 처리 + 잔액 조회
        viewLifecycleOwner.lifecycleScope.launch {
            // (a) 지갑 파일 처리 (handleWalletFile)도 오래 걸릴 수 있으니 Dispatchers.IO에서 처리
            withContext(Dispatchers.IO) {
                handleWalletFile() // 원래 함수 로직을 그대로 호출 (파일 스캔 등)
            }
            // (b) 블록체인 잔액 로드 (이미 분리된 메서드라면 그대로 호출)
            loadBalanceAsync()
        }
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

    // 지갑 파일 처리 (주소나 파일 경로 기반으로 유효한 지갑 확인)
    private fun handleWalletFile() {
        val path = UserSession.walletFilePath
        val address = UserSession.walletAddress
        Log.d("ProfileFragment", "지갑 정보 검증 시작: 파일 경로=$path, 주소=$address")

        // 주소가 있지만 파일 경로가 없는 경우 (DB에는 주소만 있는 경우)
        if (!address.isNullOrEmpty() && (path.isNullOrEmpty() || path == address)) {
            Log.d("ProfileFragment", "이더리움 주소가 있지만 파일 경로가 없습니다: $address")
            // 지갑 파일 찾기
            val walletFiles = requireContext().filesDir.listFiles { file ->
                file.name.startsWith("UTC--") && file.name.endsWith(".json")
            }
            if (walletFiles != null && walletFiles.isNotEmpty()) {
                Log.d("ProfileFragment", "총 ${walletFiles.size}개의 지갑 파일을 찾았습니다.")
                // 주소와 일치하는 지갑 파일 찾기
                for (walletFile in walletFiles) {
                    try {
                        val credentials = WalletUtils.loadCredentials(
                            UserSession.walletPassword,
                            walletFile
                        )
                        if (credentials.address.equals(address, ignoreCase = true)) {
                            Log.d("ProfileFragment", "✅ 일치하는 지갑 파일 발견: ${walletFile.name}")
                            UserSession.walletFilePath = walletFile.name
                            return
                        }
                    } catch (e: Exception) {
                        Log.d("ProfileFragment", "지갑 파일 검증 실패: ${walletFile.name}")
                    }
                }
                // 일치하는 지갑을 찾지 못하면 첫 번째 유효한 지갑 사용
                Log.w("ProfileFragment", "⚠️ 일치하는 지갑 파일을 찾지 못했습니다. 첫 번째 유효한 지갑 시도")
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
                            "ProfileFragment",
                            "✅ 대체 지갑으로 업데이트: ${walletFile.name}, 주소=${credentials.address}"
                        )
                        return
                    } catch (e: Exception) {
                        Log.d("ProfileFragment", "대체 지갑 검증 실패: ${walletFile.name}")
                    }
                }
                Log.e("ProfileFragment", "⚠️ 사용 가능한 지갑 파일을 찾지 못했습니다")
            } else {
                Log.e("ProfileFragment", "⚠️ 지갑 파일이 없습니다")
            }
        }
        // 파일 경로가 있는 경우 (일반 파일 경로)
        else if (!path.isNullOrEmpty() && !path.startsWith("0x")) {
            Log.d("ProfileFragment", "지갑 파일 경로가 있습니다: $path")
            val walletFile = File(requireContext().filesDir, path)
            if (walletFile.exists()) {
                try {
                    val credentials = WalletUtils.loadCredentials(
                        UserSession.walletPassword,
                        walletFile
                    )
                    // 주소 업데이트
                    UserSession.walletAddress = credentials.address
                    Log.d("ProfileFragment", "✅ 지갑 파일 검증 성공: 주소=${credentials.address}")
                } catch (e: Exception) {
                    Log.w("ProfileFragment", "⚠️ 지갑 파일 검증 실패: ${e.message}")
                    // 검증 실패 시 다른 지갑 파일 시도
                    findAlternativeWallet()
                }
            } else {
                Log.w("ProfileFragment", "⚠️ 지갑 파일이 존재하지 않습니다: $path")
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
                        "ProfileFragment",
                        "✅ 대체 지갑으로 업데이트: ${walletFile.name}, 주소=${credentials.address}"
                    )
                    return
                } catch (e: Exception) {
                    Log.d("ProfileFragment", "대체 지갑 검증 실패: ${walletFile.name}")
                }
            }
        }
    }

    // 잔액 로드
    private suspend fun loadBalanceAsync() = withContext(Dispatchers.IO) {
        // context가 더 이상 유효하지 않은지 먼저 확인
        if (!isAdded) {
            Log.d(
                "ProfileFragment",
                "Fragment is not attached to context, cancelling balance update"
            )
            return@withContext
        }
        val context = context ?: return@withContext // null 체크 추가
        val manager = UserSession.getBlockchainManagerIfAvailable(context)
        if (manager != null) {
            // viewLifecycleOwner는 View의 수명 주기에 바인딩되어 있으므로 Fragment가 분리되었을 때 안전하지 않습니다.
            // 코루틴 자체의 수명 주기를 사용합니다.
            try {
                // 지갑 주소 가져오기
                val address = withContext(Dispatchers.IO) { manager.getMyWalletAddress() }
                Log.d("ProfileFragment", "📍 내 지갑 주소: $address")
                // 주소 저장 (만약 아직 저장되지 않았다면)
                if (UserSession.walletAddress.isNullOrEmpty()) {
                    UserSession.walletAddress = address
                }
                // wei 단위의 토큰 잔액 가져오기
                val balanceInWei = withContext(Dispatchers.IO) { manager.getMyCatTokenBalance() }
                Log.d("ProfileFragment", "💰 CATToken 잔액(wei): $balanceInWei")
                // UserSession에 마지막 잔액 저장 (나중에 참조 가능)
                UserSession.lastKnownBalance = balanceInWei
                // UI 업데이트는 메인 스레드에서 안전하게 수행하되, Fragment가 아직 유효한지 확인
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        updateBalanceDisplay(balanceInWei)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "잔액 조회 실패", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(context, "잔액 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Log.w("ProfileFragment", "지갑 정보가 없습니다. 로그인 다시 해주세요")
            // UI 업데이트는 메인 스레드에서 안전하게 수행하되, Fragment가 아직 유효한지 확인
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

    override fun onResume() {
        super.onResume()
        // Fragment가 아직 활성 상태인 경우에만 코루틴 시작
        if (isAdded && view != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                // 백그라운드에서 잔액 가져오고 UI 업데이트
                loadBalanceAsync()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}