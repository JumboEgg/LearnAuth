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
import com.bumptech.glide.Glide
import com.example.second_project.UserSession
import com.example.second_project.databinding.DialogCertConfirmBinding
import com.example.second_project.databinding.FragmentCertDetailBinding
import com.example.second_project.viewmodel.CertDetailViewModel

class CertDetailFragment : Fragment() {

    private var _binding: FragmentCertDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CertDetailViewModel by viewModels()
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

        // Safe Args를 통해 전달받은 userId와 lectureId 사용
        val userId = args.userId
        val lectureId = args.lectureId

        // API 호출: CertDetailViewModel에서 수료증 상세 데이터를 받아옴
        viewModel.fetchCertificateDetail(userId, lectureId)

        // 관찰: API 응답이 오면 UI에 데이터 바인딩
        viewModel.certificateDetail.observe(viewLifecycleOwner) { response ->
            response?.data?.let { detail ->
                binding.textTitleCertDetail.text = detail.title
                binding.textLectureTitle.text = detail.title  // 강의 제목이 detail.title이라면, 또는 별도의 강의 제목이 있다면 수정하세요.

                // 강사명 및 강사 정보를 원하는 뷰에 바인딩 (예시로 아래와 같이)
                binding.textNameLecturer.text = detail.teacherName
                // 필요 시 강사 지갑 주소 등 다른 정보도 바인딩 가능
                binding.textNameStudent.text = UserSession.name

                // QR 코드 이미지 로딩: detail.qrCode가 이미지 URL인 경우 Glide 사용
                Glide.with(this)
                    .load(detail.qrCode)
                    .into(binding.imgQR)
            }
        }

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // "수료증 발급받기" 버튼 클릭 시 확인 다이얼로그 표시
        binding.btnCertSave.setOnClickListener {
            showConfirmDialog()
        }

        // 롱 텍스트 스크롤 효과를 위해 텍스트뷰 선택상태 true 설정
        binding.msgOnCert.isSelected = true
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
