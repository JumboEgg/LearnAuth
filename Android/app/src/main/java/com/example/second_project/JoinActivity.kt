package com.example.second_project

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.second_project.data.model.dto.request.SignupRequest
import com.example.second_project.data.model.dto.response.SignupResponse
import com.example.second_project.databinding.ActivityJoinBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.SignupApiService
import org.web3j.crypto.WalletUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private const val TAG = "JoinActivity_야옹"

class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding

    // 클릭 카운트 변수 (터치 게임용)
    private var clickCount = 0

    // 비밀번호 표시 여부
    private var isPwVisible = false
    private var isPw2Visible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 무효화
        disableBackButton()

        // 비밀번호 보이기 토글
        binding.joinPwShow.setOnClickListener { changePasswordVisibility() }
        binding.joinPwShow2.setOnClickListener { changePassword2Visibility() }

        // [회원가입하기] 버튼
        binding.joinBtn.setOnClickListener {
            val email = binding.joinEmail.text.toString().trim()
            val pw = binding.joinPw.text.toString()
            val pw2 = binding.joinPw2.text.toString()
            val nickname = binding.joinNickname.text.toString().trim()
            val name = binding.joinName.text.toString().trim()

            if (email.isEmpty() || pw.isEmpty() || pw2.isEmpty() || nickname.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pw.length <8){
                Toast.makeText(this,"비밀번호는 8자리 이상 부탁드립니다.",Toast.LENGTH_SHORT).show()
            }
            if (pw != pw2) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 터치 게임 화면 보여주기 (기존 가입 입력 UI 숨김)
            showTouchGameUI()

            // 지갑 생성 + 회원가입 진행 (별도 스레드)
            Thread {
                try {
                    // 1) Web3j 지갑 생성
                    val walletFileName = WalletUtils.generateLightNewWalletFile(pw, filesDir)
                    val walletFile = File(filesDir, walletFileName)
                    val credentials = WalletUtils.loadCredentials(pw, walletFile)
                    val walletAddress = credentials.address
                    Log.d(TAG, "생성된 지갑 주소: $walletAddress")

                    // 2) UserSession에 저장
                    UserSession.walletFilePath = walletFileName
                    UserSession.walletPassword = pw

                    // 3) 서버 회원가입
                    val signupRequest = SignupRequest(
                        email = email,
                        password = pw,
                        nickname = nickname,
                        wallet = walletAddress,
                        name = name
                    )
                    val apiService = ApiClient.retrofit.create(SignupApiService::class.java)

                    runOnUiThread {
                        apiService.signup(signupRequest).enqueue(object : Callback<SignupResponse> {
                            override fun onResponse(
                                call: Call<SignupResponse>,
                                response: Response<SignupResponse>
                            ) {
                                if (response.isSuccessful && response.body() != null) {
                                    Toast.makeText(
                                        this@JoinActivity,
                                        response.body()!!.data.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // 회원가입 성공 → LoginActivity로 이동
                                    startActivity(Intent(this@JoinActivity, LoginActivity::class.java))
                                    finish()
                                } else {
                                    // 실패 시 로그/토스트 후, 다시 가입 화면 복귀
                                    Toast.makeText(
                                        this@JoinActivity,
                                        "회원가입 실패: ${response.message()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    hideTouchGameUI()
                                }
                            }

                            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                                Toast.makeText(
                                    this@JoinActivity,
                                    "네트워크 오류: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                hideTouchGameUI()
                            }
                        })
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Log.e(TAG, "지갑 생성 실패: $e")
                        Toast.makeText(
                            this@JoinActivity,
                            "Wallet 생성 실패: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        hideTouchGameUI()
                    }
                }
            }.start()
        }

        // 터치 게임 레이아웃 터치 시 → 카운트 증가
        binding.touchGameLayout.setOnClickListener {
            clickCount++
            binding.clickCountText.text = "클릭 수: $clickCount"
        }
    }

    /** 뒤로가기 버튼 무효화 */
    private fun disableBackButton() {
        onBackPressedDispatcher.addCallback(this) {
            // 아무 것도 하지 않음
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    /** (A) 터치 게임 UI 보여주기 */
    private fun showTouchGameUI() {
        // 회원가입 입력 UI 컨테이너를 숨기고
        binding.joinFormLayout.visibility = android.view.View.GONE
        // 터치 게임 UI는 루트에 그대로 남김
        binding.touchGameLayout.visibility = android.view.View.VISIBLE
        // 클릭 카운트 초기화
        clickCount = 0
        binding.clickCountText.text = "클릭 수: 0"
    }

    /** (B) 터치 게임 UI 숨기기 */
    private fun hideTouchGameUI() {
        binding.joinFormLayout.visibility = android.view.View.VISIBLE
        binding.touchGameLayout.visibility = android.view.View.GONE
    }

    /** 비밀번호 입력 가시화 토글 */
    private fun changePasswordVisibility() {
        if (isPwVisible) {
            binding.joinPw.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.joinPwShow.setImageResource(R.drawable.invisibleicon)
        } else {
            binding.joinPw.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.joinPwShow.setImageResource(R.drawable.visibleicon)
        }
        isPwVisible = !isPwVisible
        // 커서 위치를 유지하기 위해
        binding.joinPw.setSelection(binding.joinPw.text.length)
    }

    /** 비밀번호 재확인 입력 가시화 토글 */
    private fun changePassword2Visibility() {
        if (isPw2Visible) {
            binding.joinPw2.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.joinPwShow2.setImageResource(R.drawable.invisibleicon)
        } else {
            binding.joinPw2.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.joinPwShow2.setImageResource(R.drawable.visibleicon)
        }
        isPw2Visible = !isPw2Visible
        binding.joinPw2.setSelection(binding.joinPw2.text.length)
    }
}
