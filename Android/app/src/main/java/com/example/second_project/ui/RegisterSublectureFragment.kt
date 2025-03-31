package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.adapter.RegisterSublectureAdapter
import com.example.second_project.databinding.FragmentRegisterSublectureBinding

class RegisterSublectureFragment: Fragment() {

    private var _binding: FragmentRegisterSublectureBinding? = null
    private val binding get() = _binding!!
    private lateinit var sublectureAdapter: RegisterSublectureAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterSublectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerSubLectures.layoutManager = LinearLayoutManager(requireContext())

        // 어댑터 초기화
        sublectureAdapter = RegisterSublectureAdapter(
            subLectureCount = { sublectureAdapter.itemCount },
            onDeleteClick = { position ->
                // 삭제 동작은 어댑터 내에서 처리되므로 여기선 따로 할 일 없음
            }
        )

        binding.recyclerSubLectures.adapter = sublectureAdapter
        binding.recyclerSubLectures.visibility = View.VISIBLE

        // 개별 강의 추가하기 버튼 클릭
        binding.btnAddSubLecture.setOnClickListener {
            sublectureAdapter.addItem()
            binding.recyclerSubLectures.scrollToPosition(sublectureAdapter.itemCount - 1)
        }

        binding.btnToQuiz.setOnClickListener {
            (parentFragment as? RegisterMainFragment)?.moveToStep(4)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}