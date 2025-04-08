package com.example.second_project.ui

import android.content.Intent
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
import com.example.second_project.data.model.dto.response.CertificateResponse
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
import android.graphics.pdf.PdfDocument
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.net.URLDecoder
import java.util.regex.Pattern

private const val TAG = "CertDetailFragment_야옹"
private const val IPFS_GATEWAY_URL = "https://j12d210.p.ssafy.io/ipfs"
//private const val IPFS_GATEWAY_URL = "https://gateway.pinata.cloud/ipfs"
class CertDetailFragment : Fragment() {

    private var _binding: FragmentCertDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CertDetailViewModel by viewModels()
    private val args: CertDetailFragmentArgs by navArgs()
    private var isCertificateIssued = false
    private var currentCid: String? = null

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

        // 수료증 발급 여부 확인
        checkCertificateIssued(userId, lectureId)

        // API 호출: CertDetailViewModel에서 수료증 상세 데이터를 받아옴
        viewModel.fetchCertificateDetail(userId, lectureId)

        // 관찰: API 응답이 오면 UI에 데이터 바인딩
        viewModel.certificateDetail.observe(viewLifecycleOwner) { response ->
            response?.data?.let { detail ->
                // 강의 제목 설정
                binding.textLectureTitle.text = detail.title

                // 강사명 및 강사 정보를 원하는 뷰에 바인딩
                binding.textNameLecturer.text = detail.teacherName
                // 필요 시 강사 지갑 주소 등 다른 정보도 바인딩 가능
                binding.textNameStudent.text = UserSession.name

                Log.d(TAG, "onViewCreated: 정보~!!! $detail")

                // QR 코드 이미지 로딩: detail.qrCode가 이미지 URL인 경우 Glide 사용
                if (detail.certificate != null && detail.certificate != 0 ) {
                    isCertificateIssued = true
                    // currentCid = detail.certificate.toString() // 이 부분 제거

                    // QR 코드 이미지가 이미 생성되어 있지 않은 경우에만 Glide로 로딩
                    if (binding.imgQR.drawable == null) {
                        // QR 코드 이미지 로딩
                        Glide.with(this)
                            .load(detail.qrCode)
                            .into(binding.imgQR)
                    }

                    // QR 코드 클릭 이벤트 설정
                    binding.imgQR.setOnClickListener {
                        showQrCodeDialog(detail.qrCode)
                    }

                    // 임시 수료증 텍스트 변경
                    binding.textTempCert.text = "QR코드를 클릭하면 더 크게 볼 수 있습니다."

                    // 임시 수료증 텍스트 숨기기
                    binding.msgOnCert.visibility = View.GONE

                    // 버튼 텍스트 변경
                    binding.btnCertSave.text = "저장하기"

                    // 제목 변경
                    binding.textTitleCertDetail.text = "수료증"

                    binding.imgQR.visibility = View.VISIBLE
                } else {
                    // QR 코드가 없는 경우 (아직 수료증이 발급되지 않은 경우)
                    isCertificateIssued = false
                    binding.imgQR.visibility = View.GONE
                    binding.imgQR.setOnClickListener(null)
                    binding.msgOnCert.visibility = View.VISIBLE
                    binding.textTempCert.text = "수료증을 발급받기 전에는 \n임시 수료증이 조회됩니다."
                    binding.btnCertSave.text = "수료증 발급받기"

                    // 제목 변경
                    binding.textTitleCertDetail.text = "임시 수료증"
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
                // 수료증이 이미 발급된 경우 PDF로 저장
                createAndSavePdf()
            } else {
                // 수료증이 아직 발급되지 않은 경우 발급 다이얼로그 표시
                showConfirmDialog(userId, lectureId)
            }
        }

        // 롱 텍스트 스크롤 효과를 위해 텍스트뷰 선택상태 true 설정
        binding.msgOnCert.isSelected = true
        binding.textLectureTitle.isSelected = true
    }

    // 수료증 발급 여부 확인
    private fun checkCertificateIssued(userId: Int, lectureId: Int) {
        val certificateApiService = ApiClient.retrofit.create(CertificateApiService::class.java)
        certificateApiService.getCertificates(userId).enqueue(object : Callback<CertificateResponse> {
            override fun onResponse(
                call: Call<CertificateResponse>,
                response: Response<CertificateResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val certificates = response.body()!!.data
                    Log.d(TAG, "수료증 목록 조회 성공: 총 ${certificates.size}개")
                    
                    // 해당 강의의 수료증이 발급되었는지 확인
                    val certificate = certificates.find { it.lectureId == lectureId }
                    // certificate 값이 null이거나 0인 경우 발급되지 않은 것으로 처리
                    isCertificateIssued = certificate?.certificate != null && certificate.certificate != 0
                    
                    // 로그 출력
                    Log.d(TAG, "수료증 발급 여부 확인: userId=$userId, lectureId=$lectureId")
                    Log.d(TAG, "수료증 발급 여부: ${if (isCertificateIssued) "발급됨" else "발급되지 않음"}")
                    
                    if (certificate != null) {
                        Log.d(TAG, "수료증 정보: lectureId=${certificate.lectureId}, title=${certificate.title}, " +
                                "categoryName=${certificate.categoryName}, certificate=${certificate.certificate}, " +
                                "certificateDate=${certificate.certificateDate}")
                        
                        // 수료증이 발급된 경우 CID 값 저장 및 QR 코드 생성
                        if (isCertificateIssued && certificate.certificate != null) {
                            // 여기서는 detail.certificate 값을 사용하지 않고, IPFS에서 직접 CID를 가져오도록 수정
                            // currentCid = certificate.certificate.toString()
                            // Log.d(TAG, "CID 값 저장: $currentCid")
                            
                            // QR 코드 생성은 IPFS 업로드 후 직접 생성하도록 수정
                            // generateQrCodeFromCid(currentCid!!)
                        } else {
                            Log.e(TAG, "certificate 값이 없어 CID를 저장할 수 없습니다.")
                        }
                    } else {
                        Log.d(TAG, "해당 강의(lectureId=$lectureId)의 수료증 정보가 없습니다.")
                    }
                    
                    // UI 업데이트
                    updateUIForCertificateStatus()
                } else {
                    Log.e(TAG, "수료증 목록 조회 실패: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CertificateResponse>, t: Throwable) {
                Log.e(TAG, "수료증 목록 조회 실패: ${t.message}")
            }
        })
    }
    
    // 수료증 상태에 따라 UI 업데이트
    private fun updateUIForCertificateStatus() {
        if (isCertificateIssued) {
            binding.textTitleCertDetail.text = "수료증"
            binding.msgOnCert.visibility = View.GONE
            binding.textTempCert.text = "QR코드를 클릭하면 더 크게 볼 수 있습니다."
            binding.btnCertSave.text = "저장하기"
            binding.imgQR.visibility = View.VISIBLE
        } else {
            binding.textTitleCertDetail.text = "임시 수료증"
            binding.msgOnCert.visibility = View.VISIBLE
            binding.textTempCert.text = "수료증을 발급받기 전에는 \n임시 수료증이 조회됩니다."
            binding.btnCertSave.text = "수료증 발급받기"
            binding.imgQR.visibility = View.GONE
        }
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
        try {
            // QR 코드 URL에서 CID 추출
            val cid = extractCidFromUrl(qrCodeUrl)
            if (cid != null) {
                // QR 코드 생성
                val qrCodeBitmap = QrCodeUtils.generateQrCode(qrCodeUrl)
                qrCodeBitmap?.let { 
                    dialogBinding.imgQrCodeDialog.setImageBitmap(it)
                    Log.d(TAG, "QR 코드 다이얼로그에 이미지 설정 성공")
                } ?: run {
                    Log.e(TAG, "showQrCodeDialog: QR코드 생성 실패")
                    Toast.makeText(requireContext(), "QR 코드를 생성할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e(TAG, "showQrCodeDialog: URL에서 CID를 추출할 수 없음: $qrCodeUrl")
                Toast.makeText(requireContext(), "QR 코드 URL이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "showQrCodeDialog: QR코드 생성 중 오류 발생 ${e.message}", e)
            Toast.makeText(requireContext(), "QR 코드를 표시하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }

        dialog.show()

        dialogBinding.btnCloseQrCode.setOnClickListener {
            dialog.dismiss()
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
                // URL에서 직접 CID 추출 시도
                val directPattern = Pattern.compile("ipfs/([^?]+)")
                val directMatcher = directPattern.matcher(decodedUrl)
                if (directMatcher.find()) {
                    directMatcher.group(1)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "URL 파싱 오류: ${e.message}")
            return null
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
                
                // IPFS에 업로드할 JSON 데이터 생성
                val jsonData = JSONObject().apply {
                    put("userName", userName)
                    put("userWalletAddress", userWalletAddress)
                    put("lectureTitle", lectureTitle)
                    put("teacherName", teacherName)
                    put("teacherWallet", teacherWallet)
                    put("category", category)
                    put("certificateDate", certificateDate)
                }
                
                // IPFS에 JSON 데이터 업로드
                val cid = withContext(Dispatchers.IO) {
                    IpfsUtils.uploadJsonToIpfs(jsonData)
                }
                
                if (cid != null) {
                    Log.d(TAG, "IPFS 업로드 성공: CID = $cid")
                    
                    // 백엔드 API로 수료증 발급 요청 (CID 포함)
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
                        currentCid = cid
                        Log.d(TAG, "수료증 발급 성공: CID = $cid")
                        
                        // CID로 QR 코드 생성
                        generateQrCodeFromCid(cid)
                        
                        // UI 업데이트
                        isCertificateIssued = true
                        updateUIForCertificateStatus()
                        
                        Toast.makeText(requireContext(), "NFT 수료증이 발급되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorBody = certResponse.errorBody()?.string()
                        Log.e(TAG, "수료증 발급 실패: ${certResponse.code()} - ${certResponse.message()}")
                        Log.e(TAG, "오류 응답 본문: $errorBody")
                        Toast.makeText(requireContext(), "NFT 수료증 발급에 실패했습니다. (코드: ${certResponse.code()})", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "IPFS 업로드 실패")
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
    
    // CID를 QR 코드로 변환하는 함수
    private fun generateQrCodeFromCid(cid: String) {
        try {
            // QR 코드에 사용할 URL 생성 (CID를 포함)
            val qrCodeUrl = IpfsUtils.createQrCodeUrl(cid)
            
            // QR 코드에 담긴 정보 로그 출력
            Log.d(TAG, "QR 코드에 담긴 정보: $qrCodeUrl")
            Log.d(TAG, "QR 코드에 담긴 CID: $cid")
            
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

    private fun createAndSavePdf() {
        try {
            // PDF 문서 생성
            val document = PdfDocument()
            
            // A4 크기로 페이지 생성 (595 x 842)
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            // 배경 이미지 그리기
            val backgroundDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.img_cert_frame)
            backgroundDrawable?.let { drawable ->
                drawable.setBounds(0, 0, pageInfo.pageWidth, pageInfo.pageHeight)
                drawable.draw(canvas)
            }
            
            // 텍스트 스타일 설정
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            
            // 중앙 정렬을 위한 x 좌표 (페이지 중앙)
            val centerX = pageInfo.pageWidth / 2f
            
            // 동적 데이터 그리기
            val startY = 300f  // 시작 Y 좌표 (조정 필요)
            val lineHeight = 40f
            
            // 강의명
            canvas.drawText(binding.textLectureTitle.text.toString(), centerX, startY, paint)
            
            // 수료자 이름
            canvas.drawText(binding.textNameStudent.text.toString(), centerX, startY + lineHeight, paint)
            
            // 강사명
            canvas.drawText(binding.textNameLecturer.text.toString(), centerX, startY + lineHeight * 2, paint)
            
            // QR 코드 (중앙 정렬)
            val qrBitmap = (binding.imgQR.drawable as? BitmapDrawable)?.bitmap
            qrBitmap?.let {
                val qrX = (pageInfo.pageWidth - it.width) / 2f
                val qrY = startY + lineHeight * 4  // QR 코드 위치 조정
                canvas.drawBitmap(it, qrX, qrY, paint)
            }
            
            // 페이지 완성
            document.finishPage(page)
            
            // PDF 파일 저장
            val fileName = "수료증_${binding.textLectureTitle.text}_${System.currentTimeMillis()}.pdf"
            val filePath = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            FileOutputStream(filePath).use { outputStream ->
                document.writeTo(outputStream)
            }
            
            // PDF 문서 닫기
            document.close()
            
            // 저장 완료 메시지 및 파일 열기
            Toast.makeText(requireContext(), "수료증이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            
            // PDF 파일 열기
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                filePath
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "PDF 뷰어 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "PDF 생성 중 오류 발생: ${e.message}")
            Toast.makeText(requireContext(), "PDF 생성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
