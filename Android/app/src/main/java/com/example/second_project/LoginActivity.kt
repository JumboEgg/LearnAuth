package com.example.second_project

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.second_project.data.model.dto.request.LogInRequest
import com.example.second_project.data.model.dto.response.LoginResponse
import com.example.second_project.databinding.ActivityLoginBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.LoginApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import org.web3j.crypto.WalletUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPwVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("TAG", "onCreate 내 userId: ${UserSession.userId}")

        // 이미 로그인한 기록이 있다면 바로 MainActivity로 이동
        if (UserSession.userId != 0) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.loginToJoinBtn.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            performLogin()
        }

        binding.loginPwShow.setOnClickListener {
            changePasswordVisibility()
        }
    }

    private fun performLogin() {
        val email = binding.loginId.text.toString().trim()
        val password = binding.loginPw.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val loginRequest = LogInRequest(email = email, password = password)
        val apiService = ApiClient.retrofit.create(LoginApiService::class.java)

        apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginData = response.body()!!.data
                    UserSession.userId = loginData.userId
                    UserSession.nickname = loginData.nickname
                    UserSession.name = loginData.name
                    Log.d("TAG", "onResponse 이름: ${UserSession.name}")

                    Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                    val accessToken = response.headers()["access"]
                    val refreshToken = response.headers()["refresh"]

                    Log.d("LoginActivity", "Access Token: $accessToken")
                    Log.d("LoginActivity", "Refresh Token: $refreshToken")

                    UserSession.accessToken = accessToken
                    UserSession.refreshToken = refreshToken

                    // 지갑 처리 로직
                    handleWallet(loginData.wallet)

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "로그인 실패: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    // LoginActivity.kt의 handleWallet 함수만 수정

    private fun handleWallet(dbWalletPath: String) {
        // 로그인 시 입력한 비밀번호
        val loginPassword = binding.loginPw.text.toString().trim()

        if (dbWalletPath.isEmpty()) {
            // DB에 지갑 정보가 없는 경우에만 새로 생성
            Log.d("LoginActivity", "DB에 지갑 정보가 없습니다. 새로운 지갑 생성.")
            createAndUploadWallet()
            return
        }

        // DB에 지갑 정보가 있는 경우 - DB의 정보를 사용
        Log.d("LoginActivity", "DB에 지갑 정보가 있습니다: $dbWalletPath")

        if (dbWalletPath.startsWith("0x")) {
            // 이더리움 주소인 경우 - DB에 저장된 주소 사용
            Log.d("LoginActivity", "DB에 이더리움 주소가 저장되어 있습니다: $dbWalletPath")

            // 로컬에 지갑 파일이 있는지 확인
            val walletFiles = filesDir.listFiles { file ->
                file.name.startsWith("UTC--") && file.name.endsWith(".json")
            }

            if (walletFiles != null && walletFiles.isNotEmpty()) {
                // 로컬에 지갑 파일이 있으면 검증 시도
                Log.d("LoginActivity", "로컬에 지갑 파일이 있습니다: ${walletFiles[0].name}")

                try {
                    // 비밀번호 검증 시도
                    val credentials = WalletUtils.loadCredentials(
                        loginPassword,
                        walletFiles[0]
                    )
                    Log.d("LoginActivity", "로컬 지갑 주소: ${credentials.address}")

                    // 주소가 일치하는지 확인
                    if (credentials.address.equals(dbWalletPath, ignoreCase = true)) {
                        Log.d("LoginActivity", "✓ 로컬 지갑 주소와 DB 주소가 일치합니다")
                        // 일치하면 이 지갑 파일 정보 저장
                        UserSession.walletFilePath = walletFiles[0].name
                        UserSession.walletPassword = loginPassword
                    } else {
                        Log.w("LoginActivity", "⚠️ 로컬 지갑 주소와 DB 주소가 다릅니다. DB 주소를 우선합니다.")
                        // 주소가 다르더라도 DB의 정보를 우선함
                        // 이 경우 지갑 파일 경로는 저장하지 않고, 이더리움 주소만 저장됨
                        // UserSession.getBlockchainManagerIfAvailable 메서드에서 나중에 처리할 것
                        UserSession.walletFilePath = dbWalletPath  // 이더리움 주소를 경로로 저장
                        UserSession.walletPassword = loginPassword
                    }
                } catch (e: Exception) {
                    Log.e("LoginActivity", "⚠️ 로컬 지갑 비밀번호 검증 실패: ${e.message}")
                    // 비밀번호가 틀려도 DB의 주소를 경로로 저장
                    UserSession.walletFilePath = dbWalletPath  // 이더리움 주소를 경로로 저장
                    UserSession.walletPassword = loginPassword
                }
            } else {
                Log.d("LoginActivity", "로컬에 지갑 파일이 없습니다. DB의 이더리움 주소를 저장합니다.")
                // 지갑 파일이 없어도 DB의 주소를 경로로 저장
                UserSession.walletFilePath = dbWalletPath  // 이더리움 주소를 경로로 저장
                UserSession.walletPassword = loginPassword
            }

        } else if (dbWalletPath.startsWith("UTC--")) {
            // 실제 지갑 파일 경로인 경우
            Log.d("LoginActivity", "DB에 지갑 파일 경로가 저장되어 있습니다: $dbWalletPath")

            // 로컬 파일 확인
            val walletFile = File(filesDir, dbWalletPath)
            if (walletFile.exists()) {
                try {
                    // 비밀번호 검증 시도
                    val credentials = WalletUtils.loadCredentials(
                        loginPassword,
                        walletFile
                    )
                    // 검증 성공 - 지갑 정보 저장
                    UserSession.walletFilePath = dbWalletPath
                    UserSession.walletPassword = loginPassword
                    Log.d("LoginActivity", "✓ 지갑 검증 성공, 주소: ${credentials.address}")
                } catch (e: Exception) {
                    Log.e("LoginActivity", "⚠️ 지갑 비밀번호 검증 실패: ${e.message}")
                    // 비밀번호가 틀려도 DB 정보 저장
                    UserSession.walletFilePath = dbWalletPath
                    UserSession.walletPassword = loginPassword
                    Toast.makeText(
                        this,
                        "지갑 비밀번호가 일치하지 않을 수 있습니다. 그래도 계속 진행합니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // 로컬에 파일이 없어도 DB 정보 저장
                Log.w("LoginActivity", "⚠️ 로컬에 지갑 파일이 없습니다. DB 정보를 그대로 사용합니다.")
                UserSession.walletFilePath = dbWalletPath
                UserSession.walletPassword = loginPassword
            }
        } else {
            // 알 수 없는 형식 - DB 정보 그대로 사용
            Log.w("LoginActivity", "⚠️ DB에 저장된 형식을 인식할 수 없습니다: $dbWalletPath")
            UserSession.walletFilePath = dbWalletPath
            UserSession.walletPassword = loginPassword
            Toast.makeText(
                this,
                "지갑 정보가 인식되지 않지만 그대로 사용합니다",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * 새 지갑 생성 및 서버 업데이트 함수 (DB에 지갑 정보가 없을 때만 호출됨)
     */
    private fun createAndUploadWallet() {
        try {
            // 로그인 시 입력한 비밀번호를 가져옵니다
            val loginPassword = binding.loginPw.text.toString().trim()
            // 지갑 비밀번호로 로그인 비밀번호 사용
            val walletPassword = loginPassword
            Log.d("LoginActivity", "지갑 생성 시도, 비밀번호: $walletPassword")

            val walletFileName = WalletUtils.generateLightNewWalletFile(walletPassword, filesDir)

            // 파일 경로와 비밀번호 저장
            UserSession.walletFilePath = walletFileName
            UserSession.walletPassword = walletPassword

            // 새 지갑의 주소 가져오기
            val walletFile = File(filesDir, walletFileName)
            val credentials = WalletUtils.loadCredentials(walletPassword, walletFile)
            val walletAddress = credentials.address

            Log.d("LoginActivity", "🪙 지갑 자동 생성됨: $walletFileName, 주소: $walletAddress")

            // 서버에 새로운 지갑 정보(주소) 업데이트
            updateWalletInfoToServer(walletAddress)
        } catch (e: Exception) {
            Log.e("LoginActivity", "지갑 생성 오류", e)
            Toast.makeText(this, "지갑 생성 오류: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 서버에 지갑 정보 업데이트 함수 - 기존 코드 활용
     */
    private fun updateWalletInfoToServer(walletAddress: String) {
        Log.d("LoginActivity", "업데이트할 DB 지갑 정보: $walletAddress")

        // 기존 구현 사용
        // 여기서는 Log만 남기고 나중에 필요시 구현
    }

    private fun changePasswordVisibility() {
        if (isPwVisible) {
            binding.loginPw.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.loginPwShow.setImageResource(R.drawable.invisibleicon)
        } else {
            binding.loginPw.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.loginPwShow.setImageResource(R.drawable.visibleicon)
        }
        isPwVisible = !isPwVisible

        // 커서 위치 유지
        binding.loginPw.setSelection(binding.loginPw.text.length)
    }
}