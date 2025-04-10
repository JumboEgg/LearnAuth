package com.example.second_project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.second_project.data.model.dto.request.SignupRequest
import com.example.second_project.data.model.dto.response.SignupResponse
import com.example.second_project.databinding.ActivityJoinBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.SignupApiService
import com.example.second_project.utils.disableEmojis
import com.example.second_project.utils.isKoreanOrEnglishOnly
import com.example.second_project.utils.setEnterLimit
import org.json.JSONObject
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

    // 뒤로가기 콜백
    private var backPressedCallback: OnBackPressedCallback? = null

    // 회원가입 진행 중 상태
    private var isSigningUp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 비밀번호 보이기 토글
        binding.joinPwShow.setOnClickListener { changePasswordVisibility() }
        binding.joinPwShow2.setOnClickListener { changePassword2Visibility() }

        binding.joinName.disableEmojis()
        binding.joinName.setEnterLimit(0)

        binding.joinEmail.disableEmojis()
        binding.joinEmail.setEnterLimit(0)

        binding.joinPw.disableEmojis()
        binding.joinPw.setEnterLimit(0)

        binding.joinPw2.disableEmojis()
        binding.joinPw2.setEnterLimit(0)

        binding.joinNickname.disableEmojis()
        binding.joinNickname.setEnterLimit(0)

        val editTextList = listOf(
            binding.joinName,
            binding.joinEmail,
            binding.joinPw,
            binding.joinPw2,
            binding.joinNickname
        )

        editTextList.forEach { editText ->
            // 키보드 응답성 향상을 위한 설정
            editText.setSelectAllOnFocus(true)
            editText.isSaveEnabled = true
            editText.isFocusableInTouchMode = true
            editText.inputType = editText.inputType or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }

        // [회원가입하기] 버튼
        binding.joinBtn.setOnClickListener {
            if (isSigningUp) return@setOnClickListener // 이미 진행 중이면 중복 클릭 방지

            val email = binding.joinEmail.text.toString().trim()
            val pw = binding.joinPw.text.toString()
            val pw2 = binding.joinPw2.text.toString()
            val nickname = binding.joinNickname.text.toString().trim()
            val name = binding.joinName.text.toString().trim()

            if (email.isEmpty() || pw.isEmpty() || pw2.isEmpty() || nickname.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isKoreanOrEnglishOnly(name)) {
                Toast.makeText(this, "실명은 한글 또는 영문만 입력 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pw.length < 8) {
                Toast.makeText(this, "비밀번호는 8자리 이상 부탁드립니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pw != pw2) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isKoreanOrEnglishOnly(nickname)) {
                Toast.makeText(this, "닉네임은 한글 또는 영문만 입력 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 회원가입 진행 중 상태 설정
            isSigningUp = true

            // 뒤로가기 버튼 비활성화
            disableBackButton()

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
                                    startActivity(
                                        Intent(
                                            this@JoinActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    // 실패 시 로그/토스트 후, 다시 가입 화면 복귀
                                    val errorBody = response.errorBody()?.string()
                                    try {
                                        val errorMessage =
                                            JSONObject(errorBody).getJSONObject("error")
                                                .getString("message")
                                        if (errorMessage == "이미 사용중인 이메일입니다.") {
                                            Toast.makeText(
                                                this@JoinActivity,
                                                errorMessage,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (errorMessage == "이미 사용중인 닉네임입니다.") {
                                            Toast.makeText(
                                                this@JoinActivity,
                                                errorMessage,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@JoinActivity,
                                                "회원가입 실패! 다시 시도하세요",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        Log.d("joinerror", "회원가입 오류 메시지: $errorMessage")
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            this@JoinActivity,
                                            "회원가입 실패! 다시 시도하세요",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.e(
                                            "joinerror",
                                            "에러 메시지 파싱 실패: ${e.message}, 원본: $errorBody"
                                        )
                                    }

                                    // 회원가입 진행 중 상태 해제
                                    isSigningUp = false
                                    hideTouchGameUI()
                                    enableBackButton() // 뒤로가기 버튼 다시 활성화
                                }
                            }

                            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                                Toast.makeText(
                                    this@JoinActivity,
                                    "네트워크 오류: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // 회원가입 진행 중 상태 해제
                                isSigningUp = false
                                hideTouchGameUI()
                                enableBackButton() // 뒤로가기 버튼 다시 활성화
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

                        // 회원가입 진행 중 상태 해제
                        isSigningUp = false
                        hideTouchGameUI()
                        enableBackButton() // 뒤로가기 버튼 다시 활성화
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
        // 기존 콜백이 있다면 제거
        backPressedCallback?.remove()
        // 새 콜백 생성 및 등록
        backPressedCallback = onBackPressedDispatcher.addCallback(this) {
            // 아무 것도 하지 않음
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    /** 뒤로가기 버튼 다시 활성화 */
    private fun enableBackButton() {
        // 콜백 제거
        backPressedCallback?.remove()
        backPressedCallback = null

        // 액션바 뒤로가기 버튼 활성화 (액션바가 있는 경우)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /** (A) 터치 게임 UI 보여주기 */
    private fun showTouchGameUI() {
        showLoadingOverlay()
        startCatAnimation()


        // 회원가입 입력 UI 컨테이너를 숨기고
//        binding.joinFormLayout.visibility = android.view.View.GONE
        // 터치 게임 UI는 루트에 그대로 남김
//        binding.touchGameLayout.visibility = android.view.View.VISIBLE
//        // 클릭 카운트 초기화
//        clickCount = 0
//        binding.clickCountText.text = "클릭 수: 0"
    }

    /** (B) 터치 게임 UI 숨기기 */
    private fun hideTouchGameUI() {
        hideLoadingOverlay()

//        binding.joinFormLayout.visibility = android.view.View.VISIBLE
//        binding.touchGameLayout.visibility = android.view.View.GONE
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

    // 이메일 형식 검사
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showLoadingOverlay() {
        binding.loadingOverlay.visibility = View.VISIBLE
        Glide.with(this).load(R.raw.loadingimg2).override(560, 560).into(binding.catImageView)
    }

    private fun hideLoadingOverlay() {
        // 애니메이션 정지
        binding.catImageView.clearAnimation()
        // 오버레이 숨기기
        binding.loadingOverlay.visibility = View.GONE
    }

    /**
     * 고양이 ImageView를 "화면 왼쪽→오른쪽"으로만 계속 달리게 하는 메서드
     * (한 번 달린 후 애니메이션 끝나면, 다시 왼쪽으로 복귀 후 반복)
     */
    // 고양이 이미지의 랜덤 이동 애니메이션 시작 함수
    private fun startCatAnimation() {
        binding.loadingOverlay.post {
//            doSingleRun()
        }
    }

}