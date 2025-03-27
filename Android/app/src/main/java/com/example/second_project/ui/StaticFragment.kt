package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.second_project.R
import com.example.second_project.databinding.FragmentStaticBinding

class StaticFragment : Fragment() {

    private var _binding: FragmentStaticBinding? = null
    private val binding get() = _binding!!

    // SafeArgs로 넘어온 강의명
    private var lectureName: String? = null

    // 현재 표시 중인 뷰(날짜별 통계, 강의 통계 등)를 담아두는 변수
    private var currentInflatedView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // nav_graph에서 정의한 argument
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

        // 날짜별 통계 클릭
        binding.layoutDateStats.setOnClickListener {
            inflateSubLayout(R.layout.layout_date_stats)
        }
        // 강의 통계 클릭
        binding.layoutLectureStats.setOnClickListener {
            inflateSubLayout(R.layout.layout_lecture_stats)
        }
        // 나의 통계 클릭
        binding.layoutMyStats.setOnClickListener {
            inflateSubLayout(R.layout.layout_my_stats)
        }
        // 참여자 통계 클릭
        binding.layoutParticipantsStats.setOnClickListener {
            inflateSubLayout(R.layout.layout_participants_stats)
        }

        // 화면 진입 시 기본으로 표시할 레이아웃(원한다면)
        inflateSubLayout(R.layout.layout_date_stats)
    }

    /**
     * FrameLayout 내부에 다른 레이아웃을 동적으로 인플레이트하여 표시
     */
    private fun inflateSubLayout(layoutResId: Int) {
        // 이전에 인플레이트한 뷰가 있다면 제거
        currentInflatedView?.let {
            binding.subContainer.removeView(it)
        }

        // 새 레이아웃 인플레이트
        val newView = layoutInflater.inflate(layoutResId, binding.subContainer, false)
        // subContainer에 추가
        binding.subContainer.addView(newView)

        // 현재 뷰 갱신
        currentInflatedView = newView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
