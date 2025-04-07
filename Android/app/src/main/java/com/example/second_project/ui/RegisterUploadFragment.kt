package com.example.second_project.ui

import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.second_project.databinding.FragmentRegisterUploadBinding
import com.example.second_project.interfaces.RegisterStepSavable
import com.example.second_project.viewmodel.RegisterViewModel
import android.app.Activity
import android.net.Uri
import com.example.second_project.utils.FileUtils



class RegisterUploadFragment: Fragment(), RegisterStepSavable {

    private var _binding: FragmentRegisterUploadBinding? = null
    private val binding get() = _binding!!

    // 파일 데이터 관리
    private val PICK_FILE_REQUEST_CODE = 100
    private var selectedFileUri: Uri? = null

    private val viewModel: RegisterViewModel by activityViewModels()

    // 파일 크기 제한: 50MB
    private val MAX_FILE_SIZE = 50 * 1024 * 1024L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 작성 중이던 내용 복원
        viewModel.selectedLectureFileName?.let {
            binding.textFile.text = it
        }

        viewModel.selectedLectureFileUri?.let {
            selectedFileUri = it
        }

        // 파일 업로드 레이아웃 클릭 시 파일 선택기 열기
        binding.constraintUploadFile.setOnClickListener {
            openFilePicker()
        }

        binding.btnToPaymentInfo.setOnClickListener {
            (parentFragment as? RegisterMainFragment)?.moveToStep(2)
        }
    }

    // 프래그먼트 전환 시 ViewModel에 현재 입력값 저장 (임시 저장 용도)
    override fun saveDataToViewModel(): Boolean {
        if (selectedFileUri == null) {
            Toast.makeText(requireContext(), "파일을 업로드해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        viewModel.selectedLectureFileName = binding.textFile.text.toString()
        viewModel.selectedLectureFileUri = selectedFileUri
        return true
    }

    // 파일은 zip만 저장할 수 있음
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/zip"
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    // 파일 선택 완료
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return

            val fileSize = FileUtils.getFileSizeFromUri(requireContext(), uri)
            if (fileSize > MAX_FILE_SIZE) {
                Toast.makeText(requireContext(), "50MB 이하의 파일만 업로드 가능합니다.", Toast.LENGTH_SHORT).show()
                return
            }

            selectedFileUri = data.data
            selectedFileUri?.let {

                val fileName = FileUtils.getFileNameFromUri(requireContext(), it)
                binding.textFile.text = "선택된 파일: $fileName"

                Toast.makeText(requireContext(), "파일 선택 완료: $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

}