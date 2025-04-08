package com.example.second_project.ui

import android.content.Intent
import android.net.Uri
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
import com.example.second_project.databinding.DialogQrCodeBinding
import com.example.second_project.databinding.FragmentCertDetailBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.CertificateApiService
import com.example.second_project.network.CertificateIssueRequest
import com.example.second_project.utils.ApiKeyProvider
import com.example.second_project.utils.IpfsUtils
import com.example.second_project.utils.QrCodeUtils
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
    private var isCertificateIssued = false
    private var currentToken: String? = null

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
        val userId = args.userId
        val lectureId = args.lectureId

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
                if (detail.qrCode != null && detail.qrCode.isNotEmpty()) {
                    isCertificateIssued = true
                    currentToken = detail.tokenId
                    
                    // QR 코드 이미지 로딩
                    Glide.with(this)
                        .load(detail.qrCode)
                        .into(binding.imgQR)
                    
                    // QR 코드 클릭 이벤트 설정
                    binding.imgQR.setOnClickListener {
                        showQrCodeDialog(detail.qrCode)
                    }
                    
                    // 임시 수료증 텍스트 변경
                    binding.textTempCert.text = "QR코드를 더 크게 보려면 수료증을 클릭하세요!"
                    
                    // 임시 수료증 텍스트 숨기기
                    binding.msgOnCert.visibility = View.GONE
                    
                    // 버튼 텍스트 변경
                    binding.btnCertSave.text = "저장하기"
                } else {
                    // QR 코드가 없는 경우 (아직 수료증이 발급되지 않은 경우)
                    isCertificateIssued = false
                    binding.imgQR.setOnClickListener(null)
                    binding.msgOnCert.visibility = View.VISIBLE
                    binding.textTempCert.text = "수료증을 발급받기 전에는 \n임시 수료증이 조회됩니다."
                    binding.btnCertSave.text = "수료증 발급받기"
                }
            }
        }

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // "수료증 발급받기" 또는 "저장하기" 버튼 클릭 시
        binding.btnCertSave.setOnClickListener {
            if (isCertificateIssued) {
                // 수료증이 이미 발급된 경우 저장 기능 구현
                Toast.makeText(requireContext(), "수료증 저장 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 수료증이 아직 발급되지 않은 경우 발급 다이얼로그 표시
                showConfirmDialog(userId, lectureId)
            }
        }

        // 롱 텍스트 스크롤 효과를 위해 텍스트뷰 선택상태 true 설정
        binding.msgOnCert.isSelected = true
        binding.textLectureTitle.isSelected = true
    }

    // QR 코드 다이얼로그 표시 함수
    private fun showQrCodeDialog(qrCodeUrl: String) {
        val dialogBinding = DialogQrCodeBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_radius_20)
        }

        // QR 코드 이미지 로딩
        Glide.with(this)
            .load(qrCodeUrl)
            .into(dialogBinding.imgQrCodeDialog)

        dialog.show()

        dialogBinding.btnCloseQrCode.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showConfirmDialog(userId: Int, lectureId: Int) {
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
    private fun issueCertificate(userId: Int, lectureId: Int) {
        lifecycleScope.launch {
            try {
                // 프로그레스바 표시
                binding.loadingProgressBar.visibility = View.VISIBLE
                
                // 수료일 가져오기 (퀴즈 다 푼 날짜)
                val certificateDate = viewModel.certificateDetail.value?.data?.certificateDate 
                    ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                
                // 수료자 정보 가져오기
                val userName = UserSession.name ?: ""
                val userWalletAddress = UserSession.walletFilePath ?: ""
                
                // 강의 정보 가져오기
                val lectureTitle = binding.textLectureTitle.text.toString()
                val teacherName = binding.textNameLecturer.text.toString()
                val teacherWallet = viewModel.certificateDetail.value?.data?.teacherWallet ?: ""
                
                // 카테고리 정보 가져오기 (예시로 "데이터"로 설정, 실제로는 API에서 가져와야 함)
                val category = "데이터" // 실제 카테고리 정보로 대체 필요
                
                // JSON 데이터 생성
                val jsonData = mapOf(
                    "userName" to userName,
                    "userWalletAddress" to userWalletAddress,
                    "category" to category,
                    "lectureTitle" to lectureTitle,
                    "teacherName" to teacherName,
                    "teacherWallet" to teacherWallet,
                    "certificateDate" to certificateDate
                )

                // Map<String, Any>를 Map<String, String>으로 변환
                val stringJsonData = jsonData.mapValues { it.value.toString() }

                // Pinata API 키 가져오기
                val apiKey = ApiKeyProvider.getPinataApiKey()
                
                // IPFS에 JSON 데이터 업로드
                val response = IpfsUtils.uploadJsonToIpfs(apiKey, stringJsonData)
                
                if (response.isSuccessful && response.body() != null) {
                    val cid = response.body()!!.IpfsHash
                    Log.d(TAG, "IPFS 업로드 성공: CID = $cid")
                    
                    // 백엔드 API로 CID 전송하여 수료증 발급 요청
                    val certResponse = withContext(Dispatchers.IO) {
                        ApiClient.certificateApiService.issueCertificate(
                            lectureId = lectureId,
                            requestBody = CertificateIssueRequest(
                                userId = userId,
                                cid = cid
                            )
                        ).execute()
                    }
                    
                    if (certResponse.isSuccessful && certResponse.body() != null) {
                        val token = certResponse.body()!!.data.token
                        currentToken = token
                        Log.d(TAG, "수료증 발급 성공: Token = $token")
                        
                        // 토큰 값을 QR 코드로 변환
                        generateQrCodeFromToken(token)
                        
                        // IPFS에서 정보 가져오기 (선택적)
                        fetchIpfsData(cid)
                        
                        // UI 업데이트
                        isCertificateIssued = true
                        binding.msgOnCert.visibility = View.GONE
                        binding.textTempCert.text = "QR코드를 더 크게 보려면 수료증을 클릭하세요!"
                        binding.btnCertSave.text = "저장하기"
                        
                        Toast.makeText(requireContext(), "NFT 수료증이 발급되었습니다.", Toast.LENGTH_SHORT).show()
                        // 수료증 발급 후 화면 갱신 또는 다른 처리 필요
                    } else {
                        val errorBody = certResponse.errorBody()?.string()
                        Log.e(TAG, "수료증 발급 실패: ${certResponse.code()} - ${certResponse.message()}")
                        Log.e(TAG, "오류 응답 본문: $errorBody")
                        Toast.makeText(requireContext(), "NFT 수료증 발급에 실패했습니다. (코드: ${certResponse.code()})", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "IPFS 업로드 실패: ${response.code()} - ${response.message()}")
                    Toast.makeText(requireContext(), "IPFS 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "수료증 발급 중 오류 발생: ${e.message}")
                Log.e(TAG, "오류 스택 트레이스: ${e.stackTraceToString()}")
                Toast.makeText(requireContext(), "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                // 프로그레스바 숨기기
                binding.loadingProgressBar.visibility = View.GONE
            }
        }
    }
    
    // 토큰 값을 QR 코드로 변환하는 함수
    private fun generateQrCodeFromToken(token: String) {
        try {
            // QR 코드에 사용할 URL 생성 (토큰 ID를 포함)
            val qrCodeUrl = IpfsUtils.createQrCodeUrl(token)
            
            // QR 코드 생성
            val qrCodeBitmap = QrCodeUtils.generateQrCode(qrCodeUrl)
            
            // QR 코드 이미지뷰에 표시
            qrCodeBitmap?.let {
                binding.imgQR.setImageBitmap(it)
                binding.imgQR.visibility = View.VISIBLE
                
                // QR 코드 클릭 이벤트 설정
                binding.imgQR.setOnClickListener {
                    showQrCodeDialog(qrCodeUrl)
                }
            } ?: run {
                Log.e(TAG, "QR 코드 생성 실패")
                Toast.makeText(requireContext(), "QR 코드 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "QR 코드 생성 중 오류 발생: ${e.message}")
            Toast.makeText(requireContext(), "QR 코드 생성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
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
                        // 필요한 경우 UI 업데이트
                        // 예: 수료증 정보 표시 등
                    }
                } else {
                    Log.e(TAG, "IPFS 데이터 가져오기 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "IPFS 데이터 가져오기 중 오류 발생: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
