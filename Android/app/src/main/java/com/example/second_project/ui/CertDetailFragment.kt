package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.databinding.DialogCertConfirmBinding
import com.example.second_project.databinding.FragmentCertDetailBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.CertificateApiService
import com.example.second_project.utils.ApiKeyProvider
import com.example.second_project.utils.IpfsUtils
import com.example.second_project.viewmodel.CertDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CertDetailFragment_야옹"
class CertDetailFragment : Fragment() {

    private var _binding: FragmentCertDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CertDetailViewModel by viewModels()
    private val args: CertDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCertDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Safe Args를 통해 전달받은 userId와 lectureId 사용
        val userId = args.userId.toString()
        val lectureId = args.lectureId.toString()

        // API 호출: CertDetailViewModel에서 수료증 상세 데이터를 받아옴
        viewModel.fetchCertificateDetail(userId, lectureId)

        // 관찰: API 응답이 오면 UI에 데이터 바인딩
        viewModel.certificateDetail.observe(viewLifecycleOwner) { response ->
            response?.data?.let { detail ->
                binding.textTitleCertDetail.text = detail.title
                binding.textLectureTitle.text = detail.title  // 강의 제목이 detail.title이라면, 또는 별도의 강의 제목이 있다면 수정하세요.

                // 강사명 및 강사 정보를 원하는 뷰에 바인딩 (예시로 아래와 같이)
                binding.textNameLecturer.text = detail.teacherName
                // 필요 시 강사 지갑 주소 등 다른 정보도 바인딩 가능
                binding.textNameStudent.text = UserSession.name

                // QR 코드 이미지 로딩: detail.qrCode가 이미지 URL인 경우 Glide 사용
                Glide.with(this)
                    .load(detail.qrCode)
                    .into(binding.imgQR)
            }
        }

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // "수료증 발급받기" 버튼 클릭 시 확인 다이얼로그 표시
        binding.btnCertSave.setOnClickListener {
            showConfirmDialog(userId, lectureId)
        }

        // 롱 텍스트 스크롤 효과를 위해 텍스트뷰 선택상태 true 설정
        binding.msgOnCert.isSelected = true

        binding.textLectureTitle.isSelected = true
    }

    private fun showConfirmDialog(userId: String, lectureId: String) {
        val dialogBinding = DialogCertConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_radius_20)
        }

        dialog.show()

        dialogBinding.btnCloseCert.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirmCert.setOnClickListener {
            dialog.dismiss()
            issueCertificate(userId, lectureId)
        }
    }

    // NFT 수료증 발급받는 로직
    private fun issueCertificate(userId: String, lectureId: String) {
        lifecycleScope.launch {
            try {
                // 프로그레스바 표시
                binding.loadingProgressBar.visibility = View.VISIBLE
                
                // 수료일 가져오기 (퀴즈 다 푼 날짜)
                val certificateDate = viewModel.certificateDetail.value?.data?.certificateDate 
                    ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                
                // JSON 데이터 생성
                val jsonData = mapOf(
                    "userId" to userId,
                    "lectureId" to lectureId,
                    "studentName" to binding.textNameStudent.text.toString(),
                    "lectureTitle" to binding.textLectureTitle.text.toString(),
                    "teacherName" to binding.textNameLecturer.text.toString(),
                    "issueDate" to certificateDate
                )

                // Pinata API 키 가져오기
                val apiKey = ApiKeyProvider.getPinataApiKey()
                
                // IPFS에 JSON 데이터 업로드
                val response = IpfsUtils.uploadJsonToIpfs(apiKey, jsonData)
                
                if (response.isSuccessful && response.body() != null) {
                    val cid = response.body()!!.IpfsHash
                    
                    // 백엔드 API로 CID 전송하여 수료증 발급 요청
                    val certResponse = ApiClient.certificateApiService.issueCertificate(
                        lectureId = lectureId,
                        requestBody = mapOf(
                            "userId" to userId,
                            "cid" to cid
                        )
                    ).execute()
                    
                    if (certResponse.isSuccessful) {
                        Toast.makeText(requireContext(), "NFT 수료증이 발급되었습니다.", Toast.LENGTH_SHORT).show()
                        // 수료증 발급 후 화면 갱신 또는 다른 처리 필요
                    } else {
                        Toast.makeText(requireContext(), "NFT 수료증 발급에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "issueCertificate: IPFS 업로드 실패")
                }
            } catch (e: Exception) {
                Log.d(TAG, "issueCertificate: 오류 발생: ${e.message}")
            } finally {
                // 프로그레스바 숨기기
                binding.loadingProgressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
