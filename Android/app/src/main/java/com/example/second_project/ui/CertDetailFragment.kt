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
import androidx.core.content.res.ResourcesCompat
import android.graphics.Bitmap
import com.example.second_project.network.ErrorResponse
import android.animation.ObjectAnimator

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

        // 고양이 이미지 설정 (GIF)
        Glide.with(this)
            .asGif()
            .load(R.raw.loadingimg2)
            .into(binding.catImageView)

        // 수료증 발급 여부 확인
        checkCertificateIssued(userId, lectureId)

        // API 호출 전 로딩 인디케이터 표시
        binding.loadingProgressBar.visibility = View.VISIBLE

        // API 호출: CertDetailViewModel에서 수료증 상세 데이터를 받아옴
        viewModel.fetchCertificateDetail(userId, lectureId)

        // 관찰: API 응답이 오면 UI에 데이터 바인딩
        viewModel.certificateDetail.observe(viewLifecycleOwner) { response ->
            // 로딩 인디케이터 숨기기
            binding.loadingProgressBar.visibility = View.GONE

            if (response != null && response.data != null) {
                val detail = response.data
                
                // 강의 제목 설정
                binding.textLectureTitle.text = detail.title ?: ""

                // 강사명 및 강사 정보를 원하는 뷰에 바인딩
                binding.textNameLecturer.text = detail.teacherName ?: ""
                binding.textNameStudent.text = UserSession.name ?: ""

                Log.d(TAG, "onViewCreated: 정보~!!! $detail")

                // QR 코드 이미지 로딩: detail.qrCode가 이미지 URL인 경우 Glide 사용
                if (detail.certificate != null && detail.certificate != 0) {
                    isCertificateIssued = true

                    // QR 코드 URL이 있는 경우 (CID 값)
                    if (!detail.qrCode?.isNullOrEmpty()!!) {
                        // CID를 사용하여 QR 코드 생성
                        generateQrCodeFromCid(detail.qrCode)
                    } else {
                        // QR 코드 URL이 없는 경우 직접 생성
                        val cid = detail.certificate.toString()
                        generateQrCodeFromCid(cid)
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
                    binding.textTitleCertDetail.text = "임시 수료증"
                }
            } else {
                // 응답이 null이거나 data가 null인 경우
                Log.e(TAG, "응답이 null이거나 data가 null입니다.")
                binding.textLectureTitle.text = ""
                binding.textNameLecturer.text = ""
                binding.textNameStudent.text = UserSession.name ?: ""
                binding.imgQR.visibility = View.GONE
                binding.msgOnCert.visibility = View.VISIBLE
                binding.textTempCert.text = "수료증 정보를 불러오는데 실패했습니다."
                binding.btnCertSave.text = "수료증 발급받기"
                binding.textTitleCertDetail.text = "임시 수료증"
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
                // qrCodeUrl이 직접 CID인 경우
                val qrCodeBitmap = QrCodeUtils.generateQrCode(IpfsUtils.createQrCodeUrl(qrCodeUrl))
                qrCodeBitmap?.let {
                    dialogBinding.imgQrCodeDialog.setImageBitmap(it)
                    Log.d(TAG, "QR 코드 다이얼로그에 이미지 설정 성공 (직접 CID)")
                } ?: run {
                    Log.e(TAG, "showQrCodeDialog: QR코드 생성 실패")
                    Toast.makeText(requireContext(), "QR 코드를 생성할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
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
                // 수료증 발급 시 로딩 오버레이 표시
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.btnCertSave.isEnabled = false
                // 고양이 이미지 애니메이션 시작
                val rotation = ObjectAnimator.ofFloat(binding.catImageView, "rotation", 0f, 360f)
                rotation.duration = 2000
                rotation.repeatCount = ObjectAnimator.INFINITE
                rotation.start()

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

                // 카테고리 정보 가져오기
                var category = " " // 기본값

                // 수료증 목록에서 카테고리 정보 가져오기
                val certificateApiService = ApiClient.retrofit.create(CertificateApiService::class.java)
                val certificateResponse = withContext(Dispatchers.IO) {
                    try {
                        certificateApiService.getCertificates(userId).execute()
                    } catch (e: Exception) {
                        Log.e(TAG, "카테고리 정보 가져오기 실패: ${e.message}")
                        null
                    }
                }

                // 해당 강의의 카테고리 정보 찾기
                if (certificateResponse?.isSuccessful == true && certificateResponse.body() != null) {
                    val certificates = certificateResponse.body()!!.data
                    val certificate = certificates.find { it.lectureId == lectureId }
                    if (certificate != null) {
                        category = certificate.categoryName
                        Log.d(TAG, "카테고리 정보 가져오기 성공: $category")
                    } else {
                        Log.e(TAG, "해당 강의의 카테고리 정보를 찾을 수 없습니다.")
                    }
                } else {
                    Log.e(TAG, "카테고리 정보 가져오기 실패: ${certificateResponse?.code()} - ${certificateResponse?.message()}")
                }

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

                    // 요청 본문 생성
                    val requestBody = CertificateIssueRequest(
                        userId = userId,
                        cid = cid
                    )

                    // 요청 본문 로깅
                    Log.d(TAG, "수료증 발급 요청: lectureId=$lectureId, userId=$userId, cid=$cid")
                    Log.d(TAG, "요청 본문: ${requestBody.toString()}")

                    // 백엔드 API로 수료증 발급 요청 (CID 포함)
                    val certResponse = withContext(Dispatchers.IO) {
                        try {
                            val response = ApiClient.certificateApiService.issueCertificate(
                                lectureId = lectureId,
                                requestBody = requestBody
                            ).execute()

                            // 응답 로깅
                            Log.d(TAG, "응답 코드: ${response.code()}")
                            Log.d(TAG, "응답 메시지: ${response.message()}")
                            Log.d(TAG, "응답 헤더: ${response.headers()}")

                            if (response.isSuccessful) {
                                Log.d(TAG, "응답 본문: ${response.body()}")
                                response.body()?.let { issueResponse ->
                                    if (issueResponse.code == 200) {
                                        // 성공적으로 인증서가 발급됨
                                        requireActivity().runOnUiThread {
                                            Toast.makeText(requireContext(), "인증서가 성공적으로 발급되었습니다.", Toast.LENGTH_SHORT).show()
                                            // 인증서 상세 정보 다시 로드
                                            loadCertificateDetail()
                                        }
                                    } else {
                                        requireActivity().runOnUiThread {
                                            Toast.makeText(requireContext(), "인증서 발급에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.e(TAG, "오류 응답 본문: $errorBody")

                                // 오류 응답 파싱 시도
                                try {
                                    val errorResponse = ApiClient.gson.fromJson<ErrorResponse>(errorBody, ErrorResponse::class.java)
                                    Log.e(TAG, "파싱된 오류 응답: $errorResponse")

                                    // 오류 메시지가 있는 경우 표시
                                    errorResponse.message?.let { message ->
                                        Log.e(TAG, "서버 오류 메시지: $message")
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "오류 응답 파싱 실패: ${e.message}")
                                }
                            }

                            response
                        } catch (e: Exception) {
                            Log.e(TAG, "API 호출 중 예외 발생: ${e.message}")
                            Log.e(TAG, "예외 스택 트레이스: ${e.stackTraceToString()}")
                            throw e
                        }
                    }

                    if (certResponse.isSuccessful && certResponse.body() != null) {
                        currentCid = cid
                        Log.d(TAG, "수료증 발급 성공: CID = $cid")

                        // 수료증 상세 정보 다시 불러오기
                        viewModel.fetchCertificateDetail(userId, lectureId)

                        // UI 업데이트
                        isCertificateIssued = true
                        updateUIForCertificateStatus()

                        Toast.makeText(requireContext(), "NFT 수료증이 발급되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorBody = certResponse.errorBody()?.string()
                        Log.e(TAG, "수료증 발급 실패: ${certResponse.code()} - ${certResponse.message()}")
                        Log.e(TAG, "오류 응답 본문: $errorBody")

                        // 오류 메시지 표시
                        val errorMessage = when (certResponse.code()) {
                            400 -> "잘못된 요청입니다. 입력값을 확인해주세요."
                            401 -> "인증에 실패했습니다. 다시 로그인해주세요."
                            403 -> "권한이 없습니다."
                            404 -> "요청한 리소스를 찾을 수 없습니다."
                            500 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                            else -> "수료증 발급에 실패했습니다. (코드: ${certResponse.code()})"
                        }

                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
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
                // 로딩 오버레이 숨기기
                binding.loadingOverlay.visibility = View.GONE
                binding.btnCertSave.isEnabled = true
                // 고양이 이미지 애니메이션 중지
                binding.catImageView.animate().cancel()
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
                // 103% 비율로 확대하기 위한 계산
                val scale = 1.03f
                val scaledWidth = (pageInfo.pageWidth * scale).toInt()
                val scaledHeight = (pageInfo.pageHeight * scale).toInt()
                val offsetX = (pageInfo.pageWidth - scaledWidth) / 2
                val offsetY = (pageInfo.pageHeight - scaledHeight) / 2
                
                drawable.setBounds(offsetX, offsetY, offsetX + scaledWidth, offsetY + scaledHeight)
                drawable.draw(canvas)
            }

            // 페이지 중앙 좌표
            val centerX = pageInfo.pageWidth / 2f
            val centerY = pageInfo.pageHeight / 2f

            // 강의 제목 스타일 설정 (파란색, 큰 글씨)
            val titlePaint = Paint().apply {
                color = ContextCompat.getColor(requireContext(), R.color.primary_color)
                textSize = 60f
                typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_black)
                textAlign = Paint.Align.CENTER
            }

            // 이름 스타일 설정 (검정색, 중간 크기)
            val namePaint = Paint().apply {
                color = Color.BLACK
                textSize = 40f
                typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
                textAlign = Paint.Align.CENTER
            }

            // 라벨 스타일 설정 (파란색/검정색, 작은 글씨)
            val labelPaint = Paint().apply {
                color = ContextCompat.getColor(requireContext(), R.color.primary_color)
                textSize = 30f
                typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
                textAlign = Paint.Align.CENTER
            }

            // 강의자 라벨 스타일 (검정색)
            val teacherLabelPaint = Paint().apply {
                color = Color.BLACK
                textSize = 30f
                typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
                textAlign = Paint.Align.CENTER
            }

            // 강의 제목 (중앙에서 살짝 왼쪽으로)
            val lectureTitle = binding.textLectureTitle.text.toString()

            // 한글 기준 14글자 제한
            val limitedTitle = if (lectureTitle.count { "[가-힣]".toRegex().matches(it.toString()) } > 14) {
                // 한글 글자 수 계산
                var koreanCount = 0
                var result = ""

                for (char in lectureTitle) {
                    if ("[가-힣]".toRegex().matches(char.toString())) {
                        koreanCount++
                        if (koreanCount <= 14) {
                            result += char
                        } else {
                            break
                        }
                    } else {
                        result += char
                    }
                }

                result + "..."
            } else {
                lectureTitle
            }

            // 긴 제목은 줄바꿈 처리
            val maxWidth = pageInfo.pageWidth * 0.8f
            val titleLines = splitTextIntoLines(limitedTitle, titlePaint, maxWidth)

            // 제목 그리기 (여러 줄일 경우 중앙 정렬, 살짝 왼쪽으로)
            val titleStartY = centerY - (titleLines.size * titlePaint.textSize) / 2
            val titleX = centerX - 20f
            titleLines.forEachIndexed { index, line ->
                canvas.drawText(line, titleX, titleStartY + index * titlePaint.textSize, titlePaint)
            }

            // 수료자 정보 (하단 좌측)
            val studentName = binding.textNameStudent.text.toString()
            // 수료자 이름 한글 기준 4글자 제한
            val limitedStudentName = if (studentName.count { "[가-힣]".toRegex().matches(it.toString()) } > 4) {
                var koreanCount = 0
                var result = ""
                for (char in studentName) {
                    if ("[가-힣]".toRegex().matches(char.toString())) {
                        koreanCount++
                        if (koreanCount <= 4) {
                            result += char
                        } else {
                            break
                        }
                    } else {
                        result += char
                    }
                }
                result + "..."
            } else {
                studentName
            }

            val studentX = pageInfo.pageWidth * 0.30f
            val studentY = pageInfo.pageHeight * 0.75f

            // 수료자 이름
            canvas.drawText(limitedStudentName, studentX, studentY, namePaint)

            // "수료자" 라벨
            canvas.drawText("수료자", studentX, studentY + namePaint.textSize + 10, labelPaint)

            // 강의자 정보 (하단 우측)
            val teacherName = binding.textNameLecturer.text.toString()
            // 강의자 이름 한글 기준 4글자 제한
            val limitedTeacherName = if (teacherName.count { "[가-힣]".toRegex().matches(it.toString()) } > 4) {
                var koreanCount = 0
                var result = ""
                for (char in teacherName) {
                    if ("[가-힣]".toRegex().matches(char.toString())) {
                        koreanCount++
                        if (koreanCount <= 4) {
                            result += char
                        } else {
                            break
                        }
                    } else {
                        result += char
                    }
                }
                result + "..."
            } else {
                teacherName
            }

            val teacherX = pageInfo.pageWidth * 0.65f
            val teacherY = pageInfo.pageHeight * 0.75f

            // 강의자 이름
            canvas.drawText(limitedTeacherName, teacherX, teacherY, namePaint)

            // "강의자" 라벨
            canvas.drawText("강의자", teacherX, teacherY + namePaint.textSize + 10, teacherLabelPaint)

            // QR 코드 (우측 하단)
            val qrBitmap = (binding.imgQR.drawable as? BitmapDrawable)?.bitmap
            qrBitmap?.let {
                // QR 코드 크기 조정 (원본의 40%로 축소)
                val scaledWidth = (it.width * 0.2).toInt()
                val scaledHeight = (it.height * 0.2).toInt()
                val scaledQrBitmap = Bitmap.createScaledBitmap(it, scaledWidth, scaledHeight, true)

                // 우측 하단 모서리에 배치 (여백 40픽셀)
                val qrX = pageInfo.pageWidth - scaledWidth - 40f
                val qrY = pageInfo.pageHeight - scaledHeight - 40f
                canvas.drawBitmap(scaledQrBitmap, qrX, qrY, null)

                // 메모리 해제
                scaledQrBitmap.recycle()
            }

            // 페이지 완성
            document.finishPage(page)

            // PDF 파일 저장 - 다운로드 폴더에 저장
            val fileName = "수료증_${binding.textLectureTitle.text}_${System.currentTimeMillis()}.pdf"
            val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

            FileOutputStream(filePath).use { outputStream ->
                document.writeTo(outputStream)
            }

            // PDF 문서 닫기
            document.close()

            // 저장 완료 메시지 표시
            Toast.makeText(requireContext(), "수료증이 다운로드 폴더에 저장되었습니다.", Toast.LENGTH_LONG).show()

            // 파일 경로 로그 출력
            Log.d(TAG, "PDF 파일 저장 경로: ${filePath.absolutePath}")

        } catch (e: Exception) {
            Log.e(TAG, "PDF 생성 중 오류 발생: ${e.message}")
            Toast.makeText(requireContext(), "PDF 생성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 긴 텍스트를 여러 줄로 나누는 함수
    private fun splitTextIntoLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        // 한글과 영어를 구분하여 처리
        val koreanPattern = "[가-힣]".toRegex()
        val englishPattern = "[a-zA-Z]".toRegex()

        // 한글과 영어의 비율 계산
        val koreanCount = text.count { koreanPattern.matches(it.toString()) }
        val englishCount = text.count { englishPattern.matches(it.toString()) }

        // 한글과 영어의 비율에 따라 최대 글자 수 결정
        val maxCharsPerLine = if (koreanCount > englishCount) {
            // 한글이 더 많은 경우
            7
        } else {
            // 영어가 더 많거나 비슷한 경우
            11
        }

        // 문자 단위로 분리
        val lines = mutableListOf<String>()
        var currentLine = ""
        var currentCharCount = 0

        // 공백으로 단어 분리
        val words = text.split(" ")

        for (word in words) {
            // 단어가 너무 길면 문자 단위로 분리
            if (word.length > maxCharsPerLine) {
                // 현재 줄에 내용이 있으면 먼저 추가
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                    currentLine = ""
                    currentCharCount = 0
                }

                // 단어를 문자 단위로 분리
                var tempLine = ""
                var tempCharCount = 0

                for (char in word) {
                    val isKorean = koreanPattern.matches(char.toString())
                    // 한글 글자 수 계산 방식 수정 (한글은 1글자로 계산)
                    val charWidth = 1

                    if (tempCharCount + charWidth <= maxCharsPerLine) {
                        tempLine += char
                        tempCharCount += charWidth
                    } else {
                        lines.add(tempLine)
                        tempLine = char.toString()
                        tempCharCount = charWidth
                    }
                }

                if (tempLine.isNotEmpty()) {
                    currentLine = tempLine
                    currentCharCount = tempCharCount
                }
            } else {
                // 단어가 짧은 경우
                val wordCharCount = word.length

                if (currentLine.isEmpty()) {
                    currentLine = word
                    currentCharCount = wordCharCount
                } else if (currentCharCount + wordCharCount + 1 <= maxCharsPerLine) {
                    // 공백 포함 글자 수 계산
                    currentLine += " $word"
                    currentCharCount += wordCharCount + 1
                } else {
                    lines.add(currentLine)
                    currentLine = word
                    currentCharCount = wordCharCount
                }
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }

    private fun loadCertificateDetail() {
        val userId = args.userId
        val lectureId = args.lectureId
        viewModel.fetchCertificateDetail(userId, lectureId)
        checkCertificateIssued(userId, lectureId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
