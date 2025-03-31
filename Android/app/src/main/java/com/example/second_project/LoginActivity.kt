package com.example.second_project
//
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.appcompat.app.AppCompatActivity
import com.example.second_project.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private var isPwVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginToJoinBtn.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.loginPwShow.setOnClickListener {
            changePasswordVisibility()
        }

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