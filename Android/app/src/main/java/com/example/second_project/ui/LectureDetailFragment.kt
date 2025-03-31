package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.second_project.R
import com.example.second_project.adapter.LectureDetailAdapter
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.data.repository.LectureRepository
import com.example.second_project.databinding.FragmentLectureDetailBinding
import com.example.second_project.databinding.FragmentOwnedLectureDetailBinding
import com.example.second_project.viewmodel.LectureDetailViewModel

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

        val lectureId = arguments?.getInt("lectureId") ?: return
//        val lectureId = 2 //임시..!!!!
        val userId = 1 //임시 고정값, 수정 필요

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
            }
        }

        binding.buyBtn.setOnClickListener {
            Log.d(TAG, "onViewCreated: 클릭")
            showChargeDialog()
        }

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
}