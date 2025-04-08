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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.second_project.databinding.FragmentCertVeifyBinding
import com.example.second_project.utils.IpfsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLDecoder
import java.util.regex.Pattern

private const val TAG = "CertVerifyFragment_야옹"
private const val IPFS_GATEWAY_URL = "https://j12d210.p.ssafy.io/ipfs"
//private const val IPFS_GATEWAY_URL = "https://gateway.pinata.cloud/ipfs"

class CertVerifyFragment : Fragment() {

    private var _binding: FragmentCertVeifyBinding? = null
    private val binding get() = _binding!!
    private val args: CertVerifyFragmentArgs by navArgs()
    
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
        
        // CID가 전달된 경우 직접 IPFS에서 데이터 가져오기
        args.tokenId?.let { cid ->
            fetchIpfsData(cid)
        }
    }
    
    // URL에서 CID 추출
    private fun extractCidFromUrl(url: String): String? {
        try {
            // URL 디코딩
            val decodedUrl = URLDecoder.decode(url, "UTF-8")
            
            // IPFS 게이트웨이 URL에서 CID 추출
            val pattern = Pattern.compile("$IPFS_GATEWAY_URL/([^?]+)")
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
    
    // IPFS에서 정보를 가져오는 함수
    private fun fetchIpfsData(cid: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 프로그레스바 표시
                withContext(Dispatchers.Main) {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                }
                
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
            } finally {
                // 프로그레스바 숨기기
                withContext(Dispatchers.Main) {
                    binding.loadingProgressBar.visibility = View.GONE
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
            Toast.makeText(requireContext(), "수료증 정보를 불러왔습니다.", Toast.LENGTH_SHORT).show()
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