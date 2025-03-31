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
import com.example.second_project.utils.FileUtils



class RegisterUploadFragment: Fragment(), RegisterStepSavable {

    private var _binding: FragmentRegisterUploadBinding? = null
    private val binding get() = _binding!!
    private val PICK_FILE_REQUEST_CODE = 100

    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 파일 업로드 레이아웃 클릭 시
        binding.constraintUploadFile.setOnClickListener {
            openFilePicker()
        }

        binding.btnToPaymentInfo.setOnClickListener {
            (parentFragment as? RegisterMainFragment)?.moveToStep(2)
        }
    }

    // 인터페이스 구현!
    override fun saveDataToViewModel() {
//        viewModel.title = binding.
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/zip"
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedFileUri = data?.data
            selectedFileUri?.let {
                // 선택된 파일 처리 (ex. 파일명 보여주기, 서버 업로드 등)
                Toast.makeText(requireContext(), "파일 선택됨: ${it.lastPathSegment}", Toast.LENGTH_SHORT).show()

                // 만약 파일명 표시하려면 TextView 하나 추가해서 보여줄 수도 있음
                val fileName = FileUtils.getFileNameFromUri(requireContext(), it)
                binding.textFile.text = "선택된 파일: $fileName"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

}