package com.example.second_project.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.second_project.databinding.FragmentCertVeifyBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.CertificateVerifyRequest
import com.example.second_project.utils.IpfsUtils
import com.example.second_project.viewmodel.CertVerifyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLDecoder
import java.util.regex.Pattern

private const val TAG = "CertVerifyFragment_야옹"

class CertVerifyFragment : Fragment() {

    private var _binding: FragmentCertVeifyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CertVerifyViewModel by viewModels()
    private val args: CertVerifyFragmentArgs by navArgs()
    
    // 카메라 권한 요청
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCameraApp()
        } else {
            Toast.makeText(requireContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCertVeifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        
        // QR 코드 스캔 버튼 숨기기 (핸드폰 카메라 앱에서 직접 스캔)
        binding.btnScanQrCode.visibility = View.GONE
        
        // 수료증 발급받기 버튼 (이 화면에서는 사용하지 않음)
        binding.btnCloseCertVerify.visibility = View.GONE
        
        // 토큰 ID가 전달된 경우 직접 검증
        args.tokenId?.let { tokenId ->
            verifyCertificate(tokenId)
        }
    }
    
    // 카메라 권한 확인 및 카메라 앱 실행
    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCameraApp()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    // 핸드폰 자체의 카메라 앱을 실행
    private fun openCameraApp() {
        try {
            // 카메라 앱 실행 인텐트 생성
            val cameraIntent = Intent(Intent.ACTION_VIEW)
            cameraIntent.data = Uri.parse("content://media/external/images/media")
            
            // 카메라 앱이 설치되어 있는지 확인
            val packageManager = requireContext().packageManager
            val activities = packageManager.queryIntentActivities(cameraIntent, 0)
            
            if (activities.isNotEmpty()) {
                // 카메라 앱 실행
                startActivity(cameraIntent)
                
                // 사용자에게 안내 메시지 표시
                Toast.makeText(
                    requireContext(), 
                    "카메라 앱에서 QR 코드를 스캔하세요. 스캔 후 앱으로 돌아와서 결과를 입력하세요.", 
                    Toast.LENGTH_LONG
                ).show()
                
                // 수동으로 토큰 ID 입력 받기
                showTokenIdInputDialog()
            } else {
                Toast.makeText(requireContext(), "카메라 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "카메라 앱 실행 오류: ${e.message}")
            Toast.makeText(requireContext(), "카메라 앱을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 토큰 ID 수동 입력 다이얼로그 표시
    private fun showTokenIdInputDialog() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("토큰 ID 입력")
        builder.setMessage("QR 코드를 스캔한 후 토큰 ID를 입력하세요.")
        
        // 입력 필드 생성
        val input = android.widget.EditText(requireContext())
        builder.setView(input)
        
        // 확인 버튼
        builder.setPositiveButton("확인") { _, _ ->
            val tokenId = input.text.toString()
            if (tokenId.isNotEmpty()) {
                verifyCertificate(tokenId)
            } else {
                Toast.makeText(requireContext(), "토큰 ID를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 취소 버튼
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.cancel()
        }
        
        builder.show()
    }
    
    // URL에서 토큰 ID 추출
    private fun extractTokenIdFromUrl(url: String): String? {
        try {
            // URL 디코딩
            val decodedUrl = URLDecoder.decode(url, "UTF-8")
            
            // 토큰 ID 파라미터 추출
            val pattern = Pattern.compile("tokenId=([^&]+)")
            val matcher = pattern.matcher(decodedUrl)
            
            return if (matcher.find()) {
                matcher.group(1)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "URL 파싱 오류: ${e.message}")
            return null
        }
    }
    
    // 수료증 검증
    private fun verifyCertificate(tokenId: String) {
        lifecycleScope.launch {
            try {
                // 프로그레스바 표시
                binding.loadingProgressBar.visibility = View.VISIBLE
                
                // 백엔드 API로 토큰 ID 전송하여 수료증 검증 요청
                val verifyResponse = withContext(Dispatchers.IO) {
                    ApiClient.certificateApiService.verifyCertificate(
                        requestBody = CertificateVerifyRequest(tokenId = tokenId)
                    ).execute()
                }
                
                if (verifyResponse.isSuccessful && verifyResponse.body() != null) {
                    val cid = verifyResponse.body()!!.data?.cid
                    Log.d(TAG, "수료증 검증 성공: CID = $cid")
                    
                    // IPFS에서 정보 가져오기
                    if (cid != null) {
                        fetchIpfsData(cid)
                    } else {
                        Log.e(TAG, "CID가 null입니다.")
                        Toast.makeText(requireContext(), "수료증 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = verifyResponse.errorBody()?.string()
                    Log.e(TAG, "수료증 검증 실패: ${verifyResponse.code()} - ${verifyResponse.message()}")
                    Log.e(TAG, "오류 응답 본문: $errorBody")
                    Toast.makeText(requireContext(), "수료증 검증에 실패했습니다. (코드: ${verifyResponse.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "수료증 검증 중 오류 발생: ${e.message}")
                Log.e(TAG, "오류 스택 트레이스: ${e.stackTraceToString()}")
                Toast.makeText(requireContext(), "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                // 프로그레스바 숨기기
                binding.loadingProgressBar.visibility = View.GONE
            }
        }
    }
    
    // IPFS에서 정보를 가져오는 함수
    private fun fetchIpfsData(cid: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jsonData = IpfsUtils.getJsonFromIpfs(cid)
                
                if (jsonData != null) {
                    Log.d(TAG, "IPFS 데이터 가져오기 성공: $jsonData")
                    
                    // UI 업데이트는 메인 스레드에서 수행
                    withContext(Dispatchers.Main) {
                        updateUIWithIpfsData(jsonData)
                    }
                } else {
                    Log.e(TAG, "IPFS 데이터 가져오기 실패")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "수료증 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "IPFS 데이터 가져오기 중 오류 발생: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "수료증 정보를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    // IPFS 데이터로 UI 업데이트
    private fun updateUIWithIpfsData(jsonData: JSONObject) {
        try {
            // 수료자 정보
            binding.textNameStudent.text = jsonData.optString("userName", "정보 없음")
            binding.textCodeStudent.text = jsonData.optString("userWalletAddress", "정보 없음")
            
            // 카테고리 정보
            binding.textCategory.text = jsonData.optString("category", "정보 없음")
            
            // 강의 정보
            binding.textNameLecture.text = jsonData.optString("lectureTitle", "정보 없음")
            
            // 강사 정보
            binding.textNameLecturer.text = jsonData.optString("teacherName", "정보 없음")
            binding.textCodeLecturer.text = jsonData.optString("teacherWallet", "정보 없음")
            
            // 수료 일자
            binding.textDate.text = jsonData.optString("certificateDate", "정보 없음")
            
            // 검증 성공 메시지
            Toast.makeText(requireContext(), "수료증이 검증되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "UI 업데이트 중 오류 발생: ${e.message}")
            Toast.makeText(requireContext(), "수료증 정보 표시 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 