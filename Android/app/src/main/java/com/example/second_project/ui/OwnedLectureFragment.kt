package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.NavGraphDirections
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.adapter.OwnedLectureAdapter
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.databinding.FragmentOwnedLectureBinding
import com.example.second_project.viewmodel.OwnedLectureViewModel

class OwnedLectureFragment : Fragment() {

    private var _binding: FragmentOwnedLectureBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OwnedLectureViewModel by viewModels()
    private lateinit var adapter: OwnedLectureAdapter
    private val lectureDetailRepository = LectureDetailRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnedLectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adapter 초기화: 클릭 시 LecturePlayFragment로 이동
        adapter = OwnedLectureAdapter { lectureItem ->
            val userId = UserSession.userId
            val lectureId = lectureItem.lectureId
            
            // 부모 프래그먼트의 네비게이션 컨트롤러를 사용
            val parentNavController = requireParentFragment().findNavController()
            val action = NavGraphDirections.actionGlobalOwnedLectureDetailFragment(
                lectureId = lectureId,
                userId = userId
            )
            parentNavController.navigate(action)
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
            
            // 데이터가 없을 때 메시지 표시
            if (lectureList.isEmpty()) {
                binding.emptyMessage.text = "보유한 강의가 없습니다."
                binding.emptyMessage.visibility = View.VISIBLE
                binding.ownedRecyclerView.visibility = View.GONE
            } else {
                binding.emptyMessage.visibility = View.GONE
                binding.ownedRecyclerView.visibility = View.VISIBLE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun refreshData() {
        val userId = UserSession.userId
        Log.d("OwnedLectureFragment", "Refreshing owned lectures for userId: $userId")
        viewModel.loadOwnedLectures(userId)
    }
}
