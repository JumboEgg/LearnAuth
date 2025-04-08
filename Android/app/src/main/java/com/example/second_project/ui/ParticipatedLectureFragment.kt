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
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.adapter.MyParticiListAdapter
import com.example.second_project.adapter.ParticipatedLectureAdapter
import com.example.second_project.databinding.FragmentParticipatedLectureBinding
import com.example.second_project.viewmodel.ParticipatedLectureViewModel

class ParticipatedLectureFragment : Fragment() {

    private var _binding: FragmentParticipatedLectureBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParticipatedLectureViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParticipatedLectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정: 참여 강의 목록을 LinearLayoutManager로 표시
        val adapter = ParticipatedLectureAdapter { lectureItem ->
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
        binding.participatedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.participatedRecyclerView.adapter = adapter


        val userId = UserSession.userId
        Log.d("TAG", "onViewCreated userId: $userId")
        viewModel.loadParticipatedLectures(userId)
        // 뷰모델에서 참여 강의 리스트를 관찰하여 어댑터에 업데이트
        viewModel.participatedLectures.observe(viewLifecycleOwner) { lectureList ->
            adapter.submitList(lectureList)
        }

        // 새 강의 등록 버튼 클릭 시 RegisterMainFragment로 이동
        binding.btnNewLecture.setOnClickListener {
            findNavController().navigate(R.id.registerMainFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
