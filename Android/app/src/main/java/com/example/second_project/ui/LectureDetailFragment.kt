package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.UserSession.userId
import com.example.second_project.adapter.LectureDetailAdapter
import com.example.second_project.blockchain.ForwardRequest
import com.example.second_project.blockchain.MetaTransactionSigner
import com.example.second_project.blockchain.MetaTxRequest
import com.example.second_project.data.model.dto.request.ForwardRequestDto
import com.example.second_project.data.model.dto.request.PurchaseRequest
import com.example.second_project.data.model.dto.request.SignedRequestDto
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.databinding.FragmentLectureDetailBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.LectureApiService
import com.example.second_project.utils.YoutubeUtil
import com.example.second_project.viewmodel.LectureDetailViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger

private const val TAG = "LectureDetailFragment_야옹"

class LectureDetailFragment : Fragment(R.layout.fragment_lecture_detail) {
    private var _binding: FragmentLectureDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LectureDetailViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LectureDetailViewModel::class.java)) {
                    return LectureDetailViewModel(LectureDetailRepository()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLectureDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLectureDetailBinding.bind(view)

        binding.lectureDetailList.layoutManager = LinearLayoutManager(requireContext())
        binding.lectureDetailName.isSelected = true

        Log.d(TAG, "onViewCreated: 뷰 생성됨.")

        // 뒤로가기 버튼 설정
        binding.lectureDetailBack.setOnClickListener {
            Log.d(TAG, "뒤로가기 버튼 클릭됨.")
            findNavController().popBackStack()
        }

        val lectureId = arguments?.getInt("lectureId") ?: run {
            Log.e(TAG, "lectureId 값이 없음.")
            return
        }
        val userId = userId
        Log.d(TAG, "lectureId: $lectureId, userId: $userId")

        viewModel.fetchLectureDetail(lectureId, userId)
        binding.loadingProgressBar.visibility = View.VISIBLE

        viewModel.lectureDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                binding.loadingProgressBar.visibility = View.GONE
                Log.d(TAG, "onViewCreated: 강의 상세 정보 수신 - $it")
                binding.lectureDetailName.text = it.data.title
                binding.lectureDetailCategory.text = it.data.categoryName
                binding.lectureDetailTeacher.text = it.data.lecturer ?: "강의자 미정"
                binding.lectureDetailPrice.text = "${it.data.price}"
                binding.lectureDetailGoal.text = it.data.goal
                binding.lectureDetailContent.text = it.data.description

                val subLectures = it.data.subLectures ?: emptyList()
                val adapter = LectureDetailAdapter(subLectureList = subLectures)
                binding.lectureDetailList.adapter = adapter
                binding.lectureDetailListCount.text = "총 ${subLectures.size}강"

                val firstSubLecture = subLectures.getOrNull(0)
                if (firstSubLecture != null) {
                    val videoId = firstSubLecture.lectureUrl
                    if (videoId != null) {
                        val thumbnailUrl =
                            YoutubeUtil.getThumbnailUrl(videoId, YoutubeUtil.ThumbnailQuality.HIGH)
                        Glide.with(this)
                            .load(thumbnailUrl)
                            .placeholder(R.drawable.white)
                            .into(binding.lectureDetailThumb)
                        Log.d(TAG, "onViewCreated: 썸네일 로드 완료.")
                    } else {
                        Log.e(TAG, "onViewCreated: 유효한 유튜브 URL이 아님.")
                    }
                } else {
                    Log.e(TAG, "onViewCreated: 서브 강의가 없습니다.")
                }

                // 구매 버튼 클릭 이벤트 설정
                val lectureData = it.data
                binding.buyBtn.setOnClickListener { _ ->
                    Log.d(TAG, "구매 버튼 클릭됨 - 강의ID: ${lectureData.lectureId}")
                    handleLecturePurchase(
                        lectureData.lectureId,
                        lectureData.price,
                        lectureData.title
                    )
                }

            } ?: run {
                binding.loadingProgressBar.visibility = View.GONE
                Log.e(TAG, "강의 상세 정보를 가져오지 못했습니다.")
                Toast.makeText(requireContext(), "강의 상세 정보 로딩 실패", Toast.LENGTH_SHORT).show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(TAG, "뒤로가기 버튼 (물리적) 클릭됨.")
                    findNavController().popBackStack()
                }
            })
    }

    // 구매 버튼 클릭 이벤트 처리 함수
    fun handleLecturePurchase(lectureId: Int, price: Int, lectureTitle: String) {
        try {
            Log.d(TAG, "강의 구매 시작 - 강의ID: $lectureId, 가격: $price, 제목: $lectureTitle")

            val chainManager = UserSession.getBlockchainManagerIfAvailable(requireContext())
            if (chainManager == null) {
                Log.e(TAG, "블록체인 매니저가 null입니다.")
                Toast.makeText(requireContext(), "블록체인 연결이 필요합니다.", Toast.LENGTH_SHORT).show()
                return
            }
            Log.d(TAG, "블록체인 매니저 준비 완료.")

            showPaymentConfirmDialog(BigInteger.valueOf(price.toLong())) {
                lifecycleScope.launch {
                    try {
                        Log.d(TAG, "구매 트랜잭션 준비 시작")
                        val credentials = chainManager.credentials
                        val web3j = chainManager.web3j
                        val forwarder = chainManager.forwarder
                        val lectureSystem = chainManager.lectureSystem
                        val catToken = chainManager.catToken

                        val userAddress = credentials.address
                        Log.d(TAG, "사용자 주소: $userAddress")

                        // 표시용 가격 (사용자에게 보여줄 값) - 일반 단위
                        val displayPrice = BigInteger.valueOf(price.toLong())
                        // 표시용 가격 (사용자에게 보여줄 값) 및 거래에 사용할 가격 (wei 단위 그대로 사용)
                        val requiredAmount = displayPrice.multiply(BigInteger.TEN.pow(18))
                        Log.d(TAG, "표시 가격: $displayPrice")
                        Log.d(TAG, "트랜잭션 가격(wei): $requiredAmount")

                        // 토큰 잔액 확인 (이미 wei 단위)
                        val balance = withContext(Dispatchers.IO) {
                            catToken.balanceOf(userAddress).send()
                        }
                        Log.d(TAG, "토큰 잔액(wei): $balance")

                        // wei 단위 그대로 잔액 비교
                        if (balance < requiredAmount) {
                            val shortfall = requiredAmount.subtract(balance)
                            Log.e(TAG, "잔액 부족: 부족액(wei): $shortfall")
                            withContext(Dispatchers.Main) {
                                // 부족액을 원래 wei 단위 그대로 표시
                                showNotEnoughDialog(shortfall)
                                showChargeDialog()
                            }
                            return@launch
                        }


                        // 현재 allowance 확인
                        Log.d(TAG, "현재 allowance 확인 시작")
                        val currentAllowance = withContext(Dispatchers.IO) {
                            catToken.allowance(userAddress, lectureSystem.contractAddress).send()
                        }
                        Log.d(TAG, "현재 allowance: $currentAllowance")

                        val userIdBigInt = BigInteger.valueOf(UserSession.userId.toLong())
                        val lectureIdBigInt = BigInteger.valueOf(lectureId.toLong())

                        // 블록 타임과 데드라인 설정
                        Log.d(TAG, "블록 타임 확인 시작")
                        val blockTime = withContext(Dispatchers.IO) {
                            web3j.ethGetBlockByNumber(
                                org.web3j.protocol.core.DefaultBlockParameterName.LATEST, false
                            ).send().block.timestamp
                        }
                        Log.d(TAG, "블록 타임: $blockTime")
                        val deadline = blockTime.add(BigInteger.valueOf(600))
                        Log.d(TAG, "데드라인: $deadline")

                        var approveRequestDto: SignedRequestDto

                        // 수정된 변수 생성 - 백엔드 트랜잭션이 성공했는지 여부
                        var approveSuccess = true


                        if (currentAllowance < requiredAmount) {
                            Log.d(TAG, "allowance 부족 -> approve 트랜잭션 진행")
                            val approveFunction = org.web3j.abi.datatypes.Function(
                                "approve",
                                listOf(
                                    org.web3j.abi.datatypes.Address(lectureSystem.contractAddress),
                                    org.web3j.abi.datatypes.generated.Uint256(requiredAmount)
                                ),
                                emptyList()
                            )
                            val approveData = org.web3j.abi.FunctionEncoder.encode(approveFunction)
                            Log.d(TAG, "approveData: $approveData")

                            val approveMetaRequest = MetaTxRequest(
                                credentials.address,
                                catToken.contractAddress,
                                BigInteger.valueOf(200_000),
                                deadline,
                                approveData
                            )
                            Log.d(TAG, "approveMetaRequest 생성 완료")

                            // approve nonce 조회
                            Log.d(TAG, "approve nonce 조회 시작")
                            val approveNonce = withContext(Dispatchers.IO) {
                                forwarder.nonces(credentials.address).send()
                            }
                            Log.d(TAG, "approve nonce: $approveNonce")

                            // approve 메타트랜잭션 서명 - 체인에서 가져온 nonce 사용
                            Log.d(TAG, "approve 메타 트랜잭션 서명 시작")
                            val approveSignature = withContext(Dispatchers.IO) {
                                MetaTransactionSigner.signMetaTxRequest(
                                    credentials,
                                    forwarder,
                                    web3j,
                                    approveMetaRequest
                                ).get()
                            }
                            Log.d(TAG, "approve 서명 완료: ${approveSignature.signature}")

                            val approveForwardRequest = ForwardRequest(
                                approveMetaRequest.from,
                                approveMetaRequest.to,
                                BigInteger.ZERO,
                                approveMetaRequest.gas,
                                approveNonce,  // 체인에서 가져온 nonce 사용
                                approveMetaRequest.deadline,
                                approveMetaRequest.data
                            )

                            approveRequestDto = SignedRequestDto(
                                request = ForwardRequestDto(
                                    from = approveForwardRequest.from,
                                    to = approveForwardRequest.to,
                                    value = BigInteger.ZERO,
                                    gas = approveForwardRequest.gas,
                                    nonce = approveForwardRequest.nonce,
                                    deadline = approveForwardRequest.deadline,
                                    data = approveForwardRequest.data
                                ),
                                signature = approveSignature.signature
                            )
                            Log.d(TAG, "approveRequestDto 생성 완료")
                        } else {
                            Log.d(TAG, "현재 allowance가 충분하여 approve 생략")
                            val approveFunction = org.web3j.abi.datatypes.Function(
                                "approve",
                                listOf(
                                    org.web3j.abi.datatypes.Address(lectureSystem.contractAddress),
                                    org.web3j.abi.datatypes.generated.Uint256(requiredAmount)
                                ),
                                emptyList()
                            )
                            val approveData = org.web3j.abi.FunctionEncoder.encode(approveFunction)

                            val approveMetaRequest = MetaTxRequest(
                                credentials.address,
                                catToken.contractAddress,
                                BigInteger.valueOf(200_000),
                                deadline,
                                approveData
                            )
                            Log.d(TAG, "approveMetaRequest 생성 완료")

                            // approve nonce 조회
                            Log.d(TAG, "approve nonce 조회 시작")
                            val approveNonce = withContext(Dispatchers.IO) {
                                forwarder.nonces(credentials.address).send()
                            }
                            Log.d(TAG, "approve nonce: $approveNonce")

                            // approve 메타트랜잭션 서명 - 체인에서 가져온 nonce 사용
                            Log.d(TAG, "approve 메타 트랜잭션 서명 시작")
                            val approveSignature = withContext(Dispatchers.IO) {
                                MetaTransactionSigner.signMetaTxRequest(
                                    credentials,
                                    forwarder,
                                    web3j,
                                    approveMetaRequest
                                ).get()
                            }
                            Log.d(TAG, "approve 서명 완료: ${approveSignature.signature}")

                            val approveForwardRequest = ForwardRequest(
                                approveMetaRequest.from,
                                approveMetaRequest.to,
                                BigInteger.ZERO,
                                approveMetaRequest.gas,
                                approveNonce,  // 체인에서 가져온 nonce 사용
                                approveMetaRequest.deadline,
                                approveMetaRequest.data
                            )

                            approveRequestDto = SignedRequestDto(
                                request = ForwardRequestDto(
                                    from = approveForwardRequest.from,
                                    to = approveForwardRequest.to,
                                    value = BigInteger.ZERO,
                                    gas = approveForwardRequest.gas,
                                    nonce = approveForwardRequest.nonce,
                                    deadline = approveForwardRequest.deadline,
                                    data = approveForwardRequest.data
                                ),
                                signature = approveSignature.signature
                            )
                        }

                        // purchaseLecture 함수 데이터 생성
                        Log.d(TAG, "purchaseLecture 함수 데이터 생성 시작")
                        val purchaseFunction = org.web3j.abi.datatypes.Function(
                            "purchaseLecture",
                            listOf(
                                org.web3j.abi.datatypes.generated.Uint16(userIdBigInt),
                                org.web3j.abi.datatypes.generated.Uint16(lectureIdBigInt),
                                org.web3j.abi.datatypes.generated.Uint256(requiredAmount)
                            ),
                            emptyList()
                        )
                        val purchaseData = org.web3j.abi.FunctionEncoder.encode(purchaseFunction)
                        Log.d(TAG, "purchaseData: $purchaseData")

                        // purchase 트랜잭션 준비
                        Log.d(TAG, "purchase 트랜잭션 준비 시작")
                        val purchaseMetaRequest = MetaTxRequest(
                            credentials.address,
                            lectureSystem.contractAddress,
                            BigInteger.valueOf(600_000),
                            deadline,
                            purchaseData
                        )
                        Log.d(TAG, "purchaseMetaRequest 생성 완료")

                        // purchase nonce 조회 - approve 후에 다시 조회
                        Log.d(TAG, "purchase nonce 조회 시작")
                        val purchaseNonce = withContext(Dispatchers.IO) {
                            forwarder.nonces(credentials.address).send()
                        }
                        Log.d(TAG, "purchase nonce: $purchaseNonce")

                        // 중요: approve 트랜잭션 후 nonce가 증가할 것을 예상하여 1 증가시킴
                        // 하지만 이 값을 서명에도 함께 사용해야 함
                        val adjustedPurchaseNonce = purchaseNonce.add(BigInteger.ONE)
                        Log.d(TAG, "조정된 purchase nonce: $adjustedPurchaseNonce")

                        // purchase 메타트랜잭션 서명 - 조정된 nonce 사용
                        Log.d(TAG, "purchase 메타 트랜잭션 서명 시작")
                        val purchaseSignature = withContext(Dispatchers.IO) {
                            // 수정된 부분: nonce를 명시적으로 전달하는 새로운 메소드 사용
                            MetaTransactionSigner.signMetaTxRequestWithNonce(
                                credentials,
                                forwarder,
                                web3j,
                                purchaseMetaRequest,
                                adjustedPurchaseNonce  // 조정된 nonce 값을 명시적으로 전달
                            ).get()
                        }
                        Log.d(TAG, "purchase 서명 완료: ${purchaseSignature.signature}")

                        // ForwardRequest 생성 시에도 동일한 조정된 nonce 사용
                        val purchaseForwardRequest = ForwardRequest(
                            purchaseMetaRequest.from,
                            purchaseMetaRequest.to,
                            BigInteger.ZERO,
                            purchaseMetaRequest.gas,
                            adjustedPurchaseNonce,  // 조정된 nonce 사용
                            purchaseMetaRequest.deadline,
                            purchaseMetaRequest.data
                        )

                        val purchaseRequestDto = SignedRequestDto(
                            request = ForwardRequestDto(
                                from = purchaseForwardRequest.from,
                                to = purchaseForwardRequest.to,
                                value = BigInteger.ZERO,
                                gas = purchaseForwardRequest.gas,
                                nonce = purchaseForwardRequest.nonce,
                                deadline = purchaseForwardRequest.deadline,
                                data = purchaseForwardRequest.data
                            ),
                            signature = purchaseSignature.signature
                        )
                        Log.d(TAG, "purchaseRequestDto 생성 완료")

                        val purchaseRequest = PurchaseRequest(
                            userId = UserSession.userId,
                            lectureId = lectureId,
                            approveRequest = approveRequestDto,
                            purchaseRequest = purchaseRequestDto
                        )
                        Log.d(TAG, "최종 purchaseRequest 생성 완료: ${Gson().toJson(purchaseRequest)}")

                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "서버 전송 시작")
                            val api = ApiClient.retrofit.create(LectureApiService::class.java)
                            api.purchaseLecture(purchaseRequest).enqueue(object : Callback<Void> {
                                override fun onResponse(
                                    call: Call<Void>,
                                    response: Response<Void>
                                ) {
                                    if (response.isSuccessful) {
                                        Log.d(TAG, "🎉 강의 구매 성공")
                                        Toast.makeText(
                                            requireContext(),
                                            "강의 구매 완료!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        findNavController().navigate(
                                            R.id.action_lectureDetailFragment_self,
                                            bundleOf("lectureId" to lectureId)
                                        )
                                    } else {
                                        Log.e(TAG, "서버 오류: ${response.code()}")
                                        Log.e(TAG, "서버 응답 바디: ${response.errorBody()?.string()}")

                                        Toast.makeText(
                                            requireContext(),
                                            "서버 오류: ${response.code()}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    Log.e(TAG, "🚨 전송 실패", t)
                                    Toast.makeText(
                                        requireContext(),
                                        "전송 실패: ${t.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "강의 구매 오류", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "오류 발생: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "강의 구매 처리 중 오류 발생", e)
            Toast.makeText(requireContext(), "구매 오류: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun showNotEnoughDialog(shortfall: BigInteger) {
        AlertDialog.Builder(requireContext())
            .setTitle("잔액 부족")
            .setMessage("CAT 잔액이 ${shortfall}만큼 부족합니다.\n충전 후 다시 시도해주세요.")
            .setPositiveButton("확인", null)
            .show()
    }

    fun showPaymentConfirmDialog(price: BigInteger, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("결제 확인")
            .setMessage("${price} CAT을 사용하여 강의를 구매하시겠습니까?")
            .setPositiveButton("결제") { _, _ ->
                Log.d(TAG, "결제 확인 버튼 클릭됨.")
                onConfirm()
            }
            .setNegativeButton("취소") { _, _ ->
                Log.d(TAG, "결제 취소 버튼 클릭됨.")
            }
            .show()
    }

    fun showChargeDialog() {
        Log.d(TAG, "showChargeDialog 호출됨.")

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_charge, null)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_radius_20)
            val params = attributes
            params.width = (resources.displayMetrics.widthPixels * 0.6).toInt()
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = params
        }

        val cancelBtn: Button = dialogView.findViewById(R.id.chargeNoBtn)
        cancelBtn.setOnClickListener {
            Log.d(TAG, "충전 다이얼로그 취소 버튼 클릭됨.")
            dialog.dismiss()
        }

        dialog.show()
        Log.d(TAG, "충전 다이얼로그 표시됨.")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView 호출됨.")
    }
}