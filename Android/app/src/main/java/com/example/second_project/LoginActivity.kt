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
        if (dbWalletPath.isEmpty()) {
            // DB에 지갑 정보가 없는 경우 새로 생성
            Log.d("LoginActivity", "DB에 지갑 정보가 없습니다. 새로운 지갑 생성.")
            createAndUploadWallet()
            return
        }

        // 로그인 시 입력한 비밀번호
        val loginPassword = binding.loginPw.text.toString().trim()

        // DB에 지갑 정보가 있는 경우 - 검사 후 적절히 처리
        if (dbWalletPath.startsWith("0x")) {
            // 이더리움 주소인 경우 - 지갑 파일을 찾아야 함
            Log.d("LoginActivity", "DB에 이더리움 주소가 저장되어 있습니다: $dbWalletPath")

            // 지갑 파일 찾기
            val walletFiles = filesDir.listFiles { file ->
                file.name.startsWith("UTC--") && file.name.endsWith(".json")
            }

            if (walletFiles != null && walletFiles.isNotEmpty()) {
                // 찾은 지갑 파일 사용
                val walletFileName = walletFiles[0].name

                // 지갑 파일과 로그인 비밀번호 저장
                UserSession.walletFilePath = walletFileName
                UserSession.walletPassword = loginPassword

                Log.d("LoginActivity", "✅ 찾은 지갑 파일: $walletFileName, 비밀번호: $loginPassword")

                // 비밀번호 검증 테스트
                try {
                    val credentials = org.web3j.crypto.WalletUtils.loadCredentials(
                        loginPassword,
                        walletFiles[0]
                    )
                    val address = credentials.address
                    Log.d("LoginActivity", "✅ 지갑 비밀번호 검증 성공, 주소: $address")

                    // 주소 확인 (DB의 주소와 일치하는지)
                    if (address.equals(dbWalletPath, ignoreCase = true)) {
                        Log.d("LoginActivity", "지갑 주소 일치 확인: $address")
                    } else {
                        Log.w("LoginActivity", "주의: 찾은 지갑 주소($address)와 DB 주소($dbWalletPath)가 다릅니다")
                    }
                } catch (e: Exception) {
                    Log.e("LoginActivity", "⚠️ 지갑 비밀번호 검증 실패: ${e.message}")

                    // 새 지갑 생성 필요 - 기존 지갑 파일을 백업하고 새로 생성
                    val backupDir = File(filesDir, "backup")
                    if (!backupDir.exists()) {
                        backupDir.mkdir()
                    }

                    try {
                        val backupFile = File(backupDir, "backup_${walletFiles[0].name}")
                        walletFiles[0].copyTo(backupFile, overwrite = true)
                        Log.d("LoginActivity", "기존 지갑 파일 백업 완료: ${backupFile.name}")

                        // 기존 파일 삭제 후 새 지갑 생성
                        walletFiles[0].delete()
                        createAndUploadWallet()
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "지갑 파일 백업 실패", e)
                        // 백업 실패해도 새 지갑 생성
                        createAndUploadWallet()
                    }
                }
            } else {
                // 지갑 파일을 찾지 못한 경우 - 새로 생성
                Log.d("LoginActivity", "이더리움 주소에 해당하는 지갑 파일을 찾지 못했습니다. 새 지갑을 생성합니다.")
                createAndUploadWallet()
            }
        } else if (dbWalletPath.startsWith("UTC--")) {
            // 실제 지갑 파일 경로인 경우
            UserSession.walletFilePath = dbWalletPath
            UserSession.walletPassword = loginPassword

            Log.d("LoginActivity", "✅ DB에 저장된 기존 지갑 경로: $dbWalletPath, 비밀번호: $loginPassword")

            // 로컬 파일 확인 및 비밀번호 검증
            val walletFile = File(filesDir, dbWalletPath)
            if (walletFile.exists()) {
                try {
                    val credentials = org.web3j.crypto.WalletUtils.loadCredentials(
                        loginPassword,
                        walletFile
                    )
                    Log.d("LoginActivity", "✅ 지갑 비밀번호 검증 성공, 주소: ${credentials.address}")
                } catch (e: Exception) {
                    Log.e("LoginActivity", "⚠️ 지갑 비밀번호 검증 실패: ${e.message}")

                    // 기존 파일 백업 후 새 지갑 생성
                    val backupDir = File(filesDir, "backup")
                    if (!backupDir.exists()) {
                        backupDir.mkdir()
                    }

                    try {
                        val backupFile = File(backupDir, "backup_$dbWalletPath")
                        walletFile.copyTo(backupFile, overwrite = true)
                        Log.d("LoginActivity", "기존 지갑 파일 백업 완료: ${backupFile.name}")

                        // 기존 파일 삭제 후 새 지갑 생성
                        walletFile.delete()
                        createAndUploadWallet()
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "지갑 파일 백업 실패", e)
                        // 백업 실패해도 새 지갑 생성
                        createAndUploadWallet()
                    }
                }
            } else {
                Log.w("LoginActivity", "⚠️ 지갑 파일이 로컬에 없습니다. 새 지갑을 생성합니다.")
                createAndUploadWallet()
            }
        } else {
            // 알 수 없는 형식
            Log.w("LoginActivity", "⚠️ 알 수 없는 지갑 정보 형식: $dbWalletPath. 새 지갑을 생성합니다.")
            createAndUploadWallet()
        }
    }

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

    private fun updateWalletInfoToServer(walletFileName: String) {
        Log.d("LoginActivity", "업데이트할 DB 지갑 정보: $walletFileName")
        // 예시 (구현에 맞게 수정):
        // val walletApi = ApiClient.retrofit.create(WalletApiService::class.java)
        // walletApi.updateWallet(UserSession.userId, walletFileName, walletPassword)
        //     .enqueue(object : Callback<SomeResponse> {
        //         override fun onResponse(call: Call<SomeResponse>, response: Response<SomeResponse>) {
        //             if (response.isSuccessful) {
        //                 Log.d("LoginActivity", "DB 지갑 정보 업데이트 성공")
        //             } else {
        //                 Log.e("LoginActivity", "DB 지갑 정보 업데이트 실패: ${response.message()}")
        //             }
        //         }
        //         override fun onFailure(call: Call<SomeResponse>, t: Throwable) {
        //             Log.e("LoginActivity", "DB 지갑 정보 업데이트 실패", t)
        //         }
        //     })
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