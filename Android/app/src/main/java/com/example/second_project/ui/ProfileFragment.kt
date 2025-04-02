package com.example.second_project.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.second_project.LoginActivity
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.databinding.FragmentProfileBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.AuthApiService
import com.example.second_project.data.model.dto.response.LogoutResponse
import com.example.second_project.viewmodel.ProfileViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
