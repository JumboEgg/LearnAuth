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
import com.example.second_project.blockchain.LectureForwarder
import com.example.second_project.blockchain.MetaTransactionSigner
import com.example.second_project.blockchain.MetaTxRequest
import com.example.second_project.blockchain.SignedRequest
import com.example.second_project.data.model.dto.request.LecturePurchaseRequest
import com.example.second_project.data.model.dto.request.PurchaseRequest
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.databinding.FragmentLectureDetailBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.LectureApiService
import com.example.second_project.utils.TokenManager
import com.example.second_project.utils.YoutubeUtil
import com.example.second_project.viewmodel.LectureDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.StructuredData
import org.web3j.crypto.StructuredDataEncoder
import org.web3j.crypto.Sign
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger

private const val TAG = "LectureDetailFragment_야옹"
class LectureDetailFragment: Fragment(R.layout.fragment_lecture_detail) {

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

        //뒤로가기 버튼 설정
        binding.lectureDetailBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val lectureId = arguments?.getInt("lectureId") ?: return
        val userId = userId

        viewModel.fetchLectureDetail(lectureId, userId)
        binding.loadingProgressBar.visibility = View.VISIBLE


        viewModel.lectureDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                // 로딩이 끝났으면 ProgressBar 숨기기
                binding.loadingProgressBar.visibility = View.GONE

                Log.d(TAG, "onViewCreated: $it")
                binding.lectureDetailName.text = it.data.title
                binding.lectureDetailCategory.text = it.data.categoryName
                binding.lectureDetailTeacher.text = it.data.lecturer ?: "강의자 미정"
                binding.lectureDetailPrice.text = "${it.data.price}"
                binding.lectureDetailGoal.text = it.data.goal
                binding.lectureDetailContent.text = it.data.description

                val subLectures = it.data.subLectures ?: emptyList()
                val adapter = LectureDetailAdapter(subLectureList = subLectures)
                binding.lectureDetailList.adapter = adapter
                binding.lectureDetailListCount.text = "총 ${ subLectures.size }강"

                val firstSubLecture = subLectures.get(0)
                val videoId = firstSubLecture.lectureUrl
                if(videoId != null) {
                    val thumbnailUrl = YoutubeUtil.getThumbnailUrl(videoId, YoutubeUtil.ThumbnailQuality.HIGH)
                    Glide.with(this)
                        .load(thumbnailUrl)
                        .placeholder(R.drawable.white)
                        .into(binding.lectureDetailThumb)
                } else {
                    Log.e(TAG, "onViewCreated: 유효한 유튜브 URL이 아님.", )
                }

                // 구매 버튼 클릭 이벤트 설정
                val lectureData = it.data
                binding.buyBtn.setOnClickListener { _ ->
                    handleLecturePurchase(lectureData.lectureId, lectureData.price, lectureData.title)
                }

            } ?: run {
                // detail이 null인 경우 처리
                binding.loadingProgressBar.visibility = View.GONE
                Log.e(TAG, "강의 상세 정보를 가져오지 못했습니다.")
                Toast.makeText(requireContext(), "강의 상세 정보 로딩 실패", Toast.LENGTH_SHORT).show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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

            // 구매 확인 다이얼로그 표시
            showPaymentConfirmDialog(BigInteger.valueOf(price.toLong())) {
                // 구매 처리
                lifecycleScope.launch {
                    try {
                        Log.d(TAG, "구매 트랜잭션 준비 시작")

                        // 블록체인 컴포넌트 초기화
                        val credentials = chainManager.credentials
                        val web3j = chainManager.web3j
                        val forwarder = chainManager.forwarder
                        val lectureSystem = chainManager.lectureSystem
                        val catToken = chainManager.catToken

                        Log.d(TAG, "블록체인 컴포넌트 초기화 완료")
                        Log.d(TAG, "사용자 주소: ${credentials.address}")

                        // 현재 사용자의 CAT 토큰 잔액 확인
                        val userAddress = credentials.address
                        val balance = withContext(Dispatchers.IO) {
                            catToken.balanceOf(userAddress).send()
                        }
                        val requiredAmount = BigInteger.valueOf(price.toLong())

                        Log.d(TAG, "현재 잔액: $balance, 필요한 금액: $requiredAmount")

                        if (balance < requiredAmount) {
                            // 잔액 부족 시 충전 다이얼로그 표시
                            val shortfall = requiredAmount.subtract(balance)
                            Log.d(TAG, "잔액 부족 - 부족액: $shortfall")
                            withContext(Dispatchers.Main) {
                                showNotEnoughDialog(shortfall)
                                showChargeDialog()
                            }
                            return@launch
                        }

                        // 블록체인 거래 준비
                        val userId = BigInteger.valueOf(UserSession.userId.toLong())
                        val lectureIdBigInt = BigInteger.valueOf(lectureId.toLong())

                        Log.d(TAG, "Function 인코딩 시작 - userId: $userId, lectureId: $lectureIdBigInt, amount: $requiredAmount")

                        // 강의 구매 함수 호출 데이터 준비
                        val function = org.web3j.abi.datatypes.Function(
                            "purchaseLecture",
                            listOf(
                                org.web3j.abi.datatypes.generated.Uint256(userId),
                                org.web3j.abi.datatypes.generated.Uint256(lectureIdBigInt),
                                org.web3j.abi.datatypes.generated.Uint256(requiredAmount)
                            ),
                            emptyList()
                        )
                        val encodedData = org.web3j.abi.FunctionEncoder.encode(function)
                        Log.d(TAG, "Function 인코딩 완료: $encodedData")

                        // 현재 블록 타임스탬프 가져오기
                        val blockTime = withContext(Dispatchers.IO) {
                            web3j.ethGetBlockByNumber(
                                org.web3j.protocol.core.DefaultBlockParameterName.LATEST, false
                            ).send().block.timestamp
                        }

                        // 10분 후로 데드라인 설정
                        val deadline = blockTime.add(BigInteger.valueOf(600))

                        Log.d(TAG, "메타 트랜잭션 요청 생성")
                        // 메타 트랜잭션 요청 생성
                        val metaRequest = try {
                            MetaTxRequest(
                                credentials.address,
                                lectureSystem.contractAddress,
                                BigInteger.valueOf(3_000_000), // 가스 한도를 3,000,000으로 설정
                                deadline,
                                encodedData
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "메타 트랜잭션 요청 생성 중 오류 발생", e)
                            Log.e(TAG, "상세 오류: ${e.message}")
                            e.printStackTrace()
                            throw e
                        }

                        Log.d(TAG, "메타 트랜잭션 서명 시작")

                        // MetaTransactionSigner를 사용하여 서명 생성
                        val signedRequest = withContext(Dispatchers.IO) {
                            try {
                                // 체인 ID 가져오기
                                val chainId = web3j.ethChainId().send().chainId

                                // 포워더 컨트랙트에서 nonce 가져오기
                                val nonce = forwarder.nonces(credentials.address).send()

                                Log.d(TAG, "체인 ID: $chainId, Nonce: $nonce")

                                // ForwardRequest 객체 생성
                                val forwardRequest = ForwardRequest(
                                    metaRequest.getFrom(),
                                    metaRequest.getTo(),
                                    BigInteger.ZERO, // value는 0으로 설정
                                    metaRequest.getGas(),
                                    nonce, // 실제 nonce 사용
                                    metaRequest.getDeadline(),
                                    metaRequest.getData()
                                )

                                // MetaTransactionSigner를 사용하여 서명 생성
                                val signature = MetaTransactionSigner.signMetaTxRequest(
                                    credentials,
                                    forwarder,
                                    web3j,
                                    metaRequest
                                ).get()

                                Log.d(TAG, "서명 생성 완료: ${signature.signature}")

                                // SignedRequest 객체 생성
                                SignedRequest(forwardRequest, signature.signature)
                            } catch (e: Exception) {
                                Log.e(TAG, "메타 트랜잭션 서명 중 오류 발생", e)
                                Log.e(TAG, "상세 오류: ${e.message}")
                                e.printStackTrace()
                                throw e
                            }
                        }
                        Log.d(TAG, "메타 트랜잭션 서명 완료")

                        // 릴레이어 서버에 메타 트랜잭션 전송
                        Log.d(TAG, "릴레이어 서버에 메타 트랜잭션 전송 시작")
                        
                        // 여기서 릴레이어 서버에 메타 트랜잭션을 전송하는 코드를 추가할 수 있습니다.
                        // 예: MetaTxApiService를 통해 릴레이어 서버에 전송
                        
                        // 현재는 릴레이어 서버 없이 블록체인 트랜잭션만 처리
                        Log.d(TAG, "블록체인 트랜잭션 처리 완료")
                        
                        // 실제 블록체인 트랜잭션 실행
                        withContext(Dispatchers.IO) {
                            try {
                                Log.d(TAG, "블록체인 트랜잭션 실행 시작")
                                
                                // 포워더 컨트랙트를 통해 메타 트랜잭션 실행
                                val receipt = forwarder.execute(
                                    signedRequest.request,
                                    BigInteger.ZERO // weiValue는 0으로 설정
                                ).send()
                                
                                Log.d(TAG, "블록체인 트랜잭션 실행 완료: ${receipt.transactionHash}")
                                
                                // 트랜잭션 영수증 확인
                                if (receipt.isStatusOK) {
                                    Log.d(TAG, "트랜잭션 성공: ${receipt.transactionHash}")
                                    
                                    // CAT 토큰 잔액 확인
                                    val newBalance = catToken.balanceOf(credentials.address).send()
                                    Log.d(TAG, "새로운 CAT 토큰 잔액: $newBalance")
                                    
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(requireContext(), "강의 구매가 완료되었습니다. (트랜잭션 해시: ${receipt.transactionHash})", Toast.LENGTH_LONG).show()
                                        // 구매 완료 후 강의 상세 페이지로 이동
                                        findNavController().navigate(
                                            R.id.action_lectureDetailFragment_self,
                                            bundleOf("lectureId" to lectureId)
                                        )
                                    }
                                } else {
                                    Log.e(TAG, "트랜잭션 실패: ${receipt.transactionHash}")
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(requireContext(), "강의 구매에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "블록체인 트랜잭션 실행 중 오류 발생", e)
                                Log.e(TAG, "상세 오류: ${e.message}")
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "블록체인 트랜잭션 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "강의 구매 중 오류 발생", e)
                        Log.e(TAG, "상세 오류: ${e.message}")
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "강의 구매 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "강의 구매 처리 중 오류 발생", e)
            Log.e(TAG, "상세 오류: ${e.message}")
            e.printStackTrace()
            Toast.makeText(requireContext(), "강의 구매 처리 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
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
            .setPositiveButton("결제") { _, _ -> onConfirm() }
            .setNegativeButton("취소", null)
            .show()
    }


    fun showChargeDialog() {
        Log.d(TAG, "showChargeDialog:")

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_charge, null)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_radius_20)

            val params = attributes
            params.width =
                (resources.displayMetrics.widthPixels * 0.6).toInt() // 화면 너비의 60%로 설정? (반영될지 안될지는..)
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = params
        }

        val cancelBtn: Button = dialogView.findViewById(R.id.chargeNoBtn)
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}