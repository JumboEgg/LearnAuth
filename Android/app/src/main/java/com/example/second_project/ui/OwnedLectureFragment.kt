package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.adapter.MyLectureListAdapter
import com.example.second_project.databinding.FragmentOwnedLectureBinding
import com.example.second_project.viewmodel.OwnedLectureViewModel

class OwnedLectureFragment : Fragment() {

    private var _binding: FragmentOwnedLectureBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OwnedLectureViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnedLectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MyLectureListAdapter 사용
        val adapter = MyLectureListAdapter { lectureNum, lectureTitle ->
            // 아이템 클릭 시 동작 예시
            // Toast.makeText(requireContext(), "Clicked: $lectureNum", Toast.LENGTH_SHORT).show()
        }

        binding.ownedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ownedRecyclerView.adapter = adapter

        // 뷰모델 LiveData 관찰
        viewModel.ownedLectures.observe(viewLifecycleOwner) { lectureList ->
            adapter.submitList(lectureList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
