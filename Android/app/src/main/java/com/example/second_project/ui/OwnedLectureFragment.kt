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
            
            // recentId가 null이면 강의 상세 정보를 가져와서 첫 번째 subLecture의 ID를 사용
            if (lectureItem.recentId == null) {
                // 강의 상세 정보를 가져와서 첫 번째 subLecture의 ID를 사용
                lectureDetailRepository.fetchLectureDetail(lectureId, userId).observe(viewLifecycleOwner) { lectureDetail ->
                    lectureDetail?.let {
                        val subLectures = it.data.subLectures
                        if (subLectures.isNotEmpty()) {
                            val firstSubLectureId = subLectures[0].subLectureId
                            navigateToLecturePlay(lectureId, userId, firstSubLectureId)
                        } else {
                            Toast.makeText(requireContext(), "강의 내용이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(requireContext(), "강의 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // recentId가 있으면 해당 ID를 사용
                navigateToLecturePlay(lectureId, userId, lectureItem.recentId)
            }
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
    
    // LecturePlayFragment로 이동하는 함수
    private fun navigateToLecturePlay(lectureId: Int, userId: Int, subLectureId: Int) {
        Log.d("OwnedLectureFragment", "클릭된 강의: lectureId=$lectureId, userId=$userId, subLectureId=$subLectureId")
        
        // LecturePlayFragment로 이동
        val action = OwnedLectureFragmentDirections.actionOwnedLectureFragmentToLecturePlayFragment(
            lectureId = lectureId,
            userId = userId,
            subLectureId = subLectureId
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
