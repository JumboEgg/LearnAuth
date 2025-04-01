package com.example.second_project

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.second_project.data.model.dto.request.SignupRequest
import com.example.second_project.data.model.dto.response.SignupResponse
import com.example.second_project.databinding.ActivityJoinBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.SignupApiService
import org.web3j.crypto.WalletUtils
import java.io.File
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "JoinActivity_야옹"

class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding
    private var isPwVisible = false
    private var isPw2Visible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.joinPwShow.setOnClickListener { changePasswordVisibility() }
        binding.joinPwShow2.setOnClickListener { changePassword2Visibility() }

        binding.joinBtn.setOnClickListener {
            // Wallet 생성 및 회원가입 API 호출을 백그라운드 스레드에서 실행
            Thread {
                try {
                    // 1. web3j를 이용한 wallet 생성 (비밀번호를 wallet 암호로 사용)
                    val walletPassword = binding.joinPw.text.toString()
                    val walletFileName = WalletUtils.generateLightNewWalletFile(walletPassword, filesDir)
                    val walletFile = File(filesDir, walletFileName)
                    val credentials = WalletUtils.loadCredentials(walletPassword, walletFile)
                    val walletAddress = credentials.address

                    // 2. 회원가입 request 객체 생성 (입력한 email, password, nickname, name 정보 사용)
                    val signupRequest = SignupRequest(
                        email = binding.joinEmail.text.toString(),
                        password = walletPassword,
                        nickname = binding.joinNickname.text.toString(),
                        wallet = walletAddress,
                        name = binding.joinName.text.toString()
                    )

                    // 3. ApiClient의 Retrofit 인스턴스를 통해 ApiService 생성
                    val apiService = ApiClient.retrofit.create(SignupApiService::class.java)

                    // 4. UI 스레드에서 Retrofit의 비동기 enqueue 호출 (runOnUiThread 내에서 실행)
                    runOnUiThread {
                        apiService.signup(signupRequest).enqueue(object : Callback<SignupResponse> {
                            override fun onResponse(call: Call<SignupResponse>, response: Response<SignupResponse>) {
                                if (response.isSuccessful && response.body() != null) {
                                    Toast.makeText(
                                        this@JoinActivity,
                                        response.body()!!.data.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // 회원가입 성공 시 LoginActivity로 이동
                                    val intent = Intent(this@JoinActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        this@JoinActivity,
                                        "회원가입 실패: ${response.message()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                                Toast.makeText(
                                    this@JoinActivity,
                                    "네트워크 오류: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Log.d(TAG, "onCreate fucking: $e")
                        Toast.makeText(this, "Wallet 생성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }

    private fun changePasswordVisibility() {
        if (isPwVisible) {
            binding.joinPw.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.joinPwShow.setImageResource(R.drawable.invisibleicon)
        } else {
            binding.joinPw.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.joinPwShow.setImageResource(R.drawable.visibleicon)
        }
        isPwVisible = !isPwVisible
        // 커서 위치 유지
        binding.joinPw.setSelection(binding.joinPw.text.length)
    }

    private fun changePassword2Visibility() {
        if (isPw2Visible) {
            binding.joinPw2.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.joinPwShow2.setImageResource(R.drawable.invisibleicon)
        } else {
            binding.joinPw2.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.joinPwShow2.setImageResource(R.drawable.visibleicon)
        }
        isPw2Visible = !isPw2Visible
        // 커서 위치 유지
        binding.joinPw2.setSelection(binding.joinPw2.text.length)
    }
}
