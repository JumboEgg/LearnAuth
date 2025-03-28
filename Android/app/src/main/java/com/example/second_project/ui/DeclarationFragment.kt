package com.example.second_project.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.adapter.ReportAdapter
import com.example.second_project.data.ReportItem
import com.example.second_project.databinding.DialogReportDetailBinding
import com.example.second_project.databinding.FragmentDeclarationBinding
import com.example.second_project.viewmodel.DeclarationViewModel

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
        val dialogBinding = DialogReportDetailBinding.inflate(layoutInflater)

        dialogBinding.tvDialogTitle.text = "신고 조회하기"
        dialogBinding.tvDialogReportTitle.text = "${reportItem.title}"
        dialogBinding.tvDialogReportContent.text = "${reportItem.content}"
        dialogBinding.tvDialogReportType.text = "${reportItem.type}"

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCloseDialog.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
