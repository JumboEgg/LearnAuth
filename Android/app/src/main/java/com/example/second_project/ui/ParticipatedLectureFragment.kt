package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraphNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.NavGraphDirections
import com.example.second_project.R
import com.example.second_project.adapter.MyLectureListAdapter
import com.example.second_project.adapter.MyParticiListAdapter
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

        // RecyclerView 설정
        val lectureAdapter = MyParticiListAdapter { lectureItem ->
            // 클릭 시 NavGraph 액션 실행 (SafeArgs 사용)
            val action = NavGraphDirections.actionGlobalStaticFragment(lectureItem.title)
            findNavController().navigate(action)

        }
        binding.participatedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.participatedRecyclerView.adapter = lectureAdapter

        // 뷰모델에서 참여 강의 리스트 가져오기 (LiveData)
        viewModel.participatedLectures.observe(viewLifecycleOwner) { lectureList ->
            lectureAdapter.submitList(lectureList)
        }


        binding.btnNewLecture.setOnClickListener {
            findNavController().navigate(R.id.registerMainFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
