package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.NavGraphDirections
import com.example.second_project.UserSession
import com.example.second_project.adapter.OwnedLectureAdapter
import com.example.second_project.databinding.FragmentOwnedLectureBinding
import com.example.second_project.viewmodel.OwnedLectureViewModel

class OwnedLectureFragment : Fragment() {

    private var _binding: FragmentOwnedLectureBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OwnedLectureViewModel by viewModels()
    private lateinit var adapter: OwnedLectureAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnedLectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adapter 초기화: 클릭 시 강의 제목을 Safe Args를 통해 전역 Static Fragment로 이동 (예시)
        adapter = OwnedLectureAdapter { lectureItem ->
            val action = NavGraphDirections.actionGlobalStaticFragment(lectureItem.title)
            findNavController().navigate(action)
        }
        binding.ownedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ownedRecyclerView.adapter = adapter

        // UserSession에 저장된 userId를 그대로 사용하여 강의 데이터를 로드합니다.
        val userId = UserSession.userId
        Log.d("OwnedLectureFragment", "Loading owned lectures for userId: $userId")
        viewModel.loadOwnedLectures(userId)

        // ViewModel의 소유 강의 LiveData를 관찰하여 Adapter 업데이트
        viewModel.ownedLectures.observe(viewLifecycleOwner) { lectureList ->
            Log.d("OwnedLectureFragment", "Owned lectures: $lectureList")
            adapter.submitList(lectureList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
