package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.second_project.databinding.DialogCertConfirmBinding
import com.example.second_project.databinding.FragmentCertDetailBinding
import com.example.second_project.viewmodel.CertDetailViewModel
import com.example.second_project.viewmodel.CertViewModel

class CertDetailFragment : Fragment() {

    private var _binding: FragmentCertDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CertDetailViewModel by viewModels()

    // Safe Args: nav_graph에 정의한 인자를 자동으로 받아옴
    private val args: CertDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCertDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달받은 인자 사용: userId와 lectureId를 이용해 상세 API 호출(예: ViewModel 호출)
        val userId = args.userId
        val lectureId = args.lectureId
         viewModel.fetchCertificateDetail(userId, lectureId) // 실제 API 호출 예시

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 수료증 발급받기 버튼 클릭 시 확인 다이얼로그 표시
        binding.btnCertSave.setOnClickListener {
            showConfirmDialog()
        }

        // 롱 텍스트 스크롤 효과를 위해 텍스트뷰 선택상태 true 설정
        binding.msgOnCert.isSelected = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showConfirmDialog() {
        val dialogBinding = DialogCertConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.show()

        dialogBinding.btnCloseCert.setOnClickListener {
            dialog.dismiss()
        }
    }
}
