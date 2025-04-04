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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.second_project.R
import com.example.second_project.UserSession.userId
import com.example.second_project.adapter.LectureDetailAdapter
import com.example.second_project.blockchain.SignedRequest
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.databinding.FragmentLectureDetailBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.MetaTxApiService
import com.example.second_project.utils.YoutubeUtil
import com.example.second_project.viewmodel.LectureDetailViewModel
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

            } ?: run {
                // detail이 null인 경우 처리
                binding.loadingProgressBar.visibility = View.GONE
                Log.e(TAG, "강의 상세 정보를 가져오지 못했습니다.")
                Toast.makeText(requireContext(), "강의 상세 정보 로딩 실패", Toast.LENGTH_SHORT).show()
            }
        }

//        binding.buyBtn.setOnClickListener {
//            lifecycleScope.launch {
//                try {
//                    val chainManager = UserSession.getBlockchainManagerIfAvailable(requireContext())!!
//                    val credentials = chainManager.getCredentials()
//                    val web3j = chainManager.getWeb3j()
//                    val forwarder = chainManager.getForwarder()
//                    val lectureSystem = chainManager.lectureSystem
//
//                    val userId = BigInteger.valueOf(UserSession.userId.toLong())
//                    val amount = BigInteger.valueOf(viewModel.lectureDetail.value?.data?.price?.toLong() ?: 0)
////                    val encodedData = lectureSystem.withdrawToken(userId, amount).encodeFunctionCall()
//
//                    val blockTime = web3j.ethGetBlockByNumber(
//                        org.web3j.protocol.core.DefaultBlockParameterName.LATEST, false
//                    ).send().block.timestamp
//
//                    val deadline = blockTime.add(BigInteger.valueOf(600))
//
////                    val metaRequest = MetaTxRequest(
////                        from = credentials.address,
////                        to = lectureSystem.contractAddress,
////                        gas = BigInteger.valueOf(1_000_000),
////                        deadline = deadline,
////                        data = encodedData
////                    )
//
//                    val signedRequest = MetaTransactionSigner
//                        .signMetaTxRequest(credentials, forwarder, web3j, metaRequest)
//                        .get()
//
//                    sendMetaTxToBackend(signedRequest)
//
//                } catch (e: Exception) {
//                    Log.e("LectureDetailFragment", "MetaTx Error: ${e.message}")
//                }
//            }
//        }



        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

    }

    fun sendMetaTxToBackend(signed: SignedRequest) {
        val apiService = ApiClient.retrofit.create(MetaTxApiService::class.java)
        apiService.sendMetaTransaction(signed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "결제 성공!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("MetaTx", "서버 응답 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("MetaTx", "네트워크 오류: ${t.message}")
            }
        })
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