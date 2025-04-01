package com.example.second_project.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.adapter.ReportAdapter
import com.example.second_project.data.ReportItem
import com.example.second_project.data.model.dto.response.ReportDetailResponse
import com.example.second_project.databinding.DialogReportDetailBinding
import com.example.second_project.databinding.FragmentDeclarationBinding
import com.example.second_project.network.ApiClient
import com.example.second_project.network.ReportApiService
import com.example.second_project.viewmodel.DeclarationViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeclarationFragment : Fragment() {

    private var _binding: FragmentDeclarationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DeclarationViewModel by viewModels()

    private val reportAdapter: ReportAdapter by lazy {
        ReportAdapter { selectedReport ->
            showReportDetailDialog(selectedReport)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeclarationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewReport.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportAdapter
        }

        viewModel.reportList.observe(viewLifecycleOwner) { list ->
            binding.tvReportCount.text = "신고 수 : ${list.size}건"

            reportAdapter.submitList(list)
        }
    }

    /**
     * 아이템 클릭 시 다이얼로그 표시
     */
    private fun showReportDetailDialog(reportItem: ReportItem) {

        val reportApiService = ApiClient.retrofit.create(ReportApiService::class.java)
        reportApiService.getReportDetail(reportItem.reportId)
            .enqueue(object : Callback<ReportDetailResponse> {
                override fun onResponse(
                    call: Call<ReportDetailResponse>,
                    response: Response<ReportDetailResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val detailData = response.body()!!.data
                        val dialogBinding = DialogReportDetailBinding.inflate(layoutInflater)

                        dialogBinding.tvDialogTitle.text = "신고 조회하기"
                        dialogBinding.tvDialogReportTitle.text = detailData.title
                        dialogBinding.tvDialogReportContent.text = detailData.reportDetail
                        dialogBinding.tvDialogReportType.text = mapReportType(detailData.reportType)

                        val alertDialog = AlertDialog.Builder(requireContext())
                            .setView(dialogBinding.root)
                            .create()

                        dialogBinding.btnCloseDialog.setOnClickListener {
                            alertDialog.dismiss()
                        }

                        alertDialog.show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "신고 상세정보 불러오기 실패: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ReportDetailResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "네트워크 오류: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun mapReportType(reportType: Int): String {
        return when (reportType) {
            0 -> "강의자"
            1 -> "강의 자료"
            2 -> "강의 영상"
            else -> "기타"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
