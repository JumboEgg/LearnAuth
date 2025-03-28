// StaticFragment.kt
package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.second_project.R
import com.example.second_project.databinding.FragmentStaticBinding

class StaticFragment : Fragment() {

    private var _binding: FragmentStaticBinding? = null
    private val binding get() = _binding!!

    // nav_graph에서 전달받은 강의명
    private var lectureName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lectureName = it.getString("lectureName")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaticBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상단에 강의명 표시
        binding.tvLectureName.text = lectureName

        // child NavHostFragment의 NavController 가져오기
        val navHostFragment = childFragmentManager.findFragmentById(binding.subNavHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController

        // 메뉴 클릭 시 하위 내비게이션 액션 수행 및 lectureName 인자 전달
        binding.layoutDateStats.setOnClickListener {
            val bundle = Bundle().apply { putString("lectureName", lectureName) }
            navController.navigate(R.id.dateStatsFragment, bundle)
        }
        binding.layoutLectureStats.setOnClickListener {
            val bundle = Bundle().apply { putString("lectureName", lectureName) }
            navController.navigate(R.id.lectureStatsFragment, bundle)
        }
        binding.layoutMyStats.setOnClickListener {
            val bundle = Bundle().apply { putString("lectureName", lectureName) }
            navController.navigate(R.id.myStatsFragment, bundle)
        }
        binding.layoutParticipantsStats.setOnClickListener {
            val bundle = Bundle().apply { putString("lectureName", lectureName) }
            navController.navigate(R.id.participantsStatsFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
