package com.example.second_project

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.second_project.databinding.ActivityJoinBinding
import kotlin.math.log

private const val TAG = "JoinActivity_야옹"
class JoinActivity : AppCompatActivity() {

    private lateinit var binding : ActivityJoinBinding
    private var isPwVisible = false
    private var isPw2Visible = false
    private var nameCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // xml파일 내에 일부 요소들을 visiblity=gone 처리해 두었습니다.
        // 본인인증시, 비밀번호 노출버튼 클릭시 각 요소들 visiblity값 바꿔주면 됩니다.
        // complete이미지, joinCheckName, joinPwShow
        binding.joinCheckName.setOnClickListener {
            //ui변화를 확인하기 위해서 '클릭'을 조건으로 설정해 두었습니다.
            //이후 인증 구현시, 클릭이 아닌 '본인인증 완료시'라는 조건부로 메서드 실행하면 됩니다.
            nameCheck = true
            nameChecked()
        }

        binding.joinPwShow.setOnClickListener {
            changePasswordVisibility()
        }
        binding.joinPwShow2.setOnClickListener {
            changePassword2Visibility()
        }

        binding.joinBtn.setOnClickListener {


            //회원가입 성공시 -> 회원가입 api + 토스트 + 로그인 화면으로 이동
            Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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

    private fun nameChecked() {

        if (nameCheck) {
            binding.joinCheckName.visibility = View.GONE
            binding.joinCheckedName.visibility = View.VISIBLE
        } else {
            binding.joinCheckName.visibility = View.VISIBLE
            binding.joinCheckedName.visibility = View.GONE
        }
    }


}