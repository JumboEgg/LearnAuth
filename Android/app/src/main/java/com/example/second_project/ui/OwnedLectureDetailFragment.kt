package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.second_project.R
import com.example.second_project.UserSession.userId
import com.example.second_project.adapter.OwnedLectureDetailAdapter
import com.example.second_project.adapter.SearchLectureAdapter
import com.example.second_project.data.ReportItem
import com.example.second_project.data.model.dto.response.ReportApiResponse
import com.example.second_project.data.model.dto.response.SubLecture
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.databinding.DialogReportBinding
import com.example.second_project.databinding.FragmentOwnedLectureDetailBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.ReportApiService
import com.example.second_project.ui.LecturePlayFragmentDirections.Companion.actionOwnedLectureDetailFragmentToLecturePlayFragment
import com.example.second_project.utils.YoutubeUtil
import com.example.second_project.viewmodel.OwnedLectureDetailViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "OwnedLecrtureDetailFrag_야옹"
class OwnedLectureDetailFragment : Fragment() {

    private var _binding: FragmentOwnedLectureDetailBinding? = null
    private val binding get() = _binding!!
    private var recentSubLectureId: Int? = null

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
                recentSubLectureId = it.data.recentLectureId
//                recentSubLectureId = 16 //임시
                val allSubLectures: List<SubLecture> = it.data.subLectures

                // 로딩이 끝났으면 ProgressBar 숨기기
                binding.loadingProgressBar.visibility = View.GONE

                Log.d(TAG, "onViewCreated: $it")
                binding.lectureDetailName.text = it.data.title
                binding.lectureDetailCategory.text = it.data.categoryName
                binding.lectureDetailTeacher.text = it.data.lecturer ?: "강의자 미정"
                binding.lectureDetailGoal.text = it.data.goal

                val foundSubLecture = allSubLectures.find { sub -> sub.subLectureId == recentSubLectureId }

                // "완강한 강의인지 아닌지 조건문 추가 필요"
                if ( foundSubLecture != null ) {
                    binding.ownedDetailPlayBtn.text = "${foundSubLecture.lectureOrder}강 - 이어보기"
                    subLectureId = it.data.recentLectureId
                } else {
                    binding.ownedDetailPlayBtn.text = "수강하기"
                }

                // 모든 강의가 완강 상태인지
                val allCompleted = allSubLectures.all {sub -> sub.endFlag}

                if (it.data.certificate) {
                    // 수료 완료시
                    binding.quizBtn.visibility = View.GONE
                    binding.ownedCertBtn.visibility = View.VISIBLE
                } else {
                    // 미수료 상태라면
                    binding.quizBtn.visibility = View.VISIBLE
                    binding.ownedCertBtn.visibility = View.GONE

                    // 완강 상태인지 아닌지에 따라
                    if (allCompleted) {
                        binding.quizBtnVisible.visibility = View.VISIBLE
                        binding.quizBtnGone.visibility = View.GONE
                        binding.quizBtnVisible.isClickable = true
                        binding.quizBtnVisible.isEnabled = true
                        binding.quizBtn.isClickable = true
                        binding.quizBtn.isEnabled = true
                        Log.d(TAG, "onViewCreated: 퀴즈 버튼 활성화됨")
                    } else {
                        binding.quizBtnGone.visibility = View.VISIBLE
                        binding.quizBtnVisible.visibility = View.GONE
                        binding.quizBtnGone.isClickable = false
                        binding.quizBtnGone.isEnabled = false
                        binding.quizBtn.isClickable = false
                        binding.quizBtn.isEnabled = false
                        Log.d(TAG, "onViewCreated: 퀴즈 버튼 비활성화됨")
                    }
                }

                val subLectures = it.data.subLectures ?: emptyList()
                val adapter = OwnedLectureDetailAdapter(
                    subLectureList = subLectures,
                    onItemClick = { subLecture ->
                        val lectureId = arguments?.getInt("lectureId") ?: return@OwnedLectureDetailAdapter
                        val userId = arguments?.getInt("userId") ?: return@OwnedLectureDetailAdapter
                        val subLectureId = subLecture.subLectureId

                        val action = OwnedLectureDetailFragmentDirections
                            .actionOwnedLectureDetailFragmentToLecturePlayFragment(lectureId, userId, subLectureId)
                        findNavController().navigate(action)
                    }
                )
                
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

                binding.myLectureDetailList.layoutManager = LinearLayoutManager(requireContext())
                binding.myLectureDetailList.adapter = adapter
                binding.myLectureDetailList.isNestedScrollingEnabled = false

                // RecyclerView의 위치를 동적으로 조정
                val contentLayout = binding.ownedLectureDetailContentLayout
                val constraintSet = ConstraintSet()
                constraintSet.clone(contentLayout)
                
                if (binding.quizBtn.visibility == View.GONE) {
                    constraintSet.connect(
                        R.id.myLectureDetailList,
                        ConstraintSet.TOP,
                        R.id.ownedCertBtn,
                        ConstraintSet.BOTTOM,
                        20
                    )
                } else {
                    constraintSet.connect(
                        R.id.myLectureDetailList,
                        ConstraintSet.TOP,
                        R.id.quizBtn,
                        ConstraintSet.BOTTOM,
                        20
                    )
                }
                constraintSet.applyTo(contentLayout)

                // 상단 이어보기 버튼 (파란색)
                binding.ownedDetailPlayBtn.setOnClickListener {
                    val lectureId = arguments?.getInt("lectureId") ?: return@setOnClickListener
                    val userId = arguments?.getInt("userId") ?: return@setOnClickListener

                    val action = OwnedLectureDetailFragmentDirections
                        .actionOwnedLectureDetailFragmentToLecturePlayFragment(
                            lectureId = lectureId,
                            userId = userId,
                            subLectureId = recentSubLectureId!!
                        )

                    findNavController().navigate(action)

                }

                binding.quizBtnVisible.setOnClickListener {
                    Log.d(TAG, "onViewCreated: 퀴즈 버튼 클릭됨")
                    if (binding.quizBtnVisible.visibility == View.VISIBLE) {
                        Log.d(TAG, "onViewCreated: 퀴즈 풀기 버튼 눌림")
                        val lectureId = arguments?.getInt("lectureId") ?: return@setOnClickListener
                        val userId = userId

                        val action = OwnedLectureDetailFragmentDirections
                            .actionOwnedLectureDetailFragmentToQuizFragment(lectureId, userId)
                        findNavController().navigate(action)
                    } else {
                        Log.d(TAG, "onViewCreated: 퀴즈 버튼 비활성화 상태")
                        Toast.makeText(requireContext(), "퀴즈를 진행할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack(R.id.nav_search, true)
            }
        })

        binding.lectureDetailBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.declarationBtn.setOnClickListener {
            showReportDialog(userId, lectureId)
        }

        //뒤로가기 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })


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