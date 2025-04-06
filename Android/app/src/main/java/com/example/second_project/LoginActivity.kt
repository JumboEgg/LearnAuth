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

                    // 지갑 생성이 필요한 경우 자동 생성
                    val existingFile = UserSession.walletFilePath?.let {
                        File(filesDir, it)
                    }

                    if (existingFile == null || !existingFile.exists()) {
                        val walletPassword = "user-${UserSession.userId}-pw"
                        val walletFileName = org.web3j.crypto.WalletUtils.generateLightNewWalletFile(walletPassword, filesDir)

                        UserSession.walletFilePath = walletFileName
                        UserSession.walletPassword = walletPassword

                        Log.d("LoginActivity", "🪙 지갑 자동 생성됨: $walletFileName")
                    } else {
                        Log.d("LoginActivity", "✅ 기존 지갑 있음: ${existingFile.name}")
                    }

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
