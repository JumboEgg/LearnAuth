package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.R
import com.example.second_project.UserSession.userId
import com.example.second_project.adapter.OwnedLectureDetailAdapter
import com.example.second_project.data.ReportItem
import com.example.second_project.data.model.dto.response.ReportApiResponse
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.databinding.DialogReportBinding
import com.example.second_project.databinding.FragmentOwnedLectureDetailBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.ReportApiService
import com.example.second_project.ui.LecturePlayFragmentDirections.Companion.actionOwnedLectureDetailFragmentToLecturePlayFragment
import com.example.second_project.viewmodel.OwnedLectureDetailViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "OwnedLecrtureDetailFrag_야옹"
class OwnedLectureDetailFragment : Fragment() {

    private var _binding: FragmentOwnedLectureDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OwnedLectureDetailViewModel by lazy {
        OwnedLectureDetailViewModel(LectureDetailRepository())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnedLectureDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lectureDetailName = binding.lectureDetailName
        lectureDetailName.isSelected = true

        val lectureId = arguments?.getInt("lectureId") ?: return
        val userId = userId
        var subLectureId: Int? = null

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
                binding.lectureDetailGoal.text = it.data.goal

                // "수료 완료한 강의인지 아닌지 조건문 추가 필요"
                if (it.data.recentLectureId != 0 ) {
                    binding.ownedDetailPlayBtn.text = "${it.data.recentLectureId}강 - 이어 보기"
                    subLectureId = it.data.recentLectureId
                } else {
                    binding.ownedDetailPlayBtn.text = "수강하기"

                }


//                val subLectures = it.data.subLectures ?: emptyList()
//                val adapter = LectureDetailAdapter(subLectureList = subLectures)
//                binding.lectureDetailList.adapter = adapter
//                binding.lectureDetailListCount.text = "총 ${ subLectures.size }강"

                val subLectures = it.data.subLectures ?: emptyList()
                val adapter = OwnedLectureDetailAdapter(subLectureList = subLectures)
                binding.myLectureDetailList.layoutManager = LinearLayoutManager(requireContext())
                binding.myLectureDetailList.adapter = adapter


                // 상단 이어보기 버튼 (파란색)
                binding.ownedDetailPlayBtn.setOnClickListener {
                    val lectureId = arguments?.getInt("lectureId") ?: return@setOnClickListener
                    val userId = userId
                    val subLectureId = subLectureId


                    // 수강'중'이 아닐 경우 혹은 수강 완전히 완료한 경우 subLectureId가 무용할 수 있음,
                    // 이때는 첫 영상 틀어주도록 처리 필요... api에 index값 들어오면 추가

                    val action = OwnedLectureDetailFragmentDirections
                        .actionOwnedLectureDetailFragmentToLecturePlayFragment(lectureId, userId, subLectureId ?: 1)
                    findNavController().navigate(action)
                }

                binding.quizBtn.setOnClickListener {
                    val lectureId = arguments?.getInt("lectureId") ?: return@setOnClickListener
                    val userId = userId

                    val action = OwnedLectureDetailFragmentDirections
                        .actionOwnedLectureDetailFragmentToQuizFragment(lectureId, userId)
                    findNavController().navigate(action)
                }

            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack(R.id.nav_search, true)
            }
        })


        binding.declarationBtn.setOnClickListener {
            showReportDialog(userId, lectureId)
        }

    }



    private fun showReportDialog(userId:Int, lectureId: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_report, null)
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

        dialog.show()

        val dialogBinding = DialogReportBinding.bind(dialogView)
        val reportOptions = dialogBinding.reportOptions
        val reportContent = dialogBinding.reportContent
        val reportBtn = dialogBinding.reportBtn

        reportBtn.setOnClickListener {
            val selectedOptionId = reportOptions.checkedRadioButtonId
            Log.d(TAG, "showReportDialog: 버튼눌림, $selectedOptionId")

            val reportType = when (selectedOptionId) {
                R.id.type1 -> 1
                R.id.type2 -> 2
                R.id.type3 -> 3
                else -> 0
            }

            if (reportType == 0) {
                Toast.makeText(requireContext(), "신고 유형을 선택해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val content = reportContent.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "신고 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val report = ReportItem(
                userId = userId,
                lectureId = lectureId,
                reportType = reportType,
                reportContent = content
            )

            Log.d(TAG, "showReportDialog: $userId, $lectureId, $reportType, $content")

            val reportApiService = ApiClient.retrofit.create(ReportApiService::class.java)
            reportApiService.postReport(report).enqueue(object : Callback<ReportApiResponse> {
                override fun onResponse(call: Call<ReportApiResponse>, response: Response<ReportApiResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Log.e(TAG, "신고 접수 실패 - 응답 코드: ${response.code()}")  // 응답 코드 출력
                        Log.e(TAG, "신고 접수 실패 - 응답 메시지: ${response.message()}") // 기본 메시지
                    }
                }

                override fun onFailure(call: Call<ReportApiResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "신고 요청 중 오류 발생", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "신고 요청 실패", t)
                }
            })
        }
    }



}