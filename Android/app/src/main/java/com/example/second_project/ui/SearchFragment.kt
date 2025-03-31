package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraphNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.adapter.CategoryAdapter
import com.example.second_project.adapter.SearchLectureAdapter
import com.example.second_project.databinding.FragmentSearchBinding
import com.example.second_project.viewmodel.SearchViewModel

private const val TAG = "SearchFragment_야옹"
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 카테고리 리스트 정의 (첫번째 "전체" 선택 시 전체 강의 로드)
        val categoryList = listOf("전체", "통계학", "법률", "생물학", "체육", "수학", "마케팅")

        // dp -> px로 변환
        val spacing = dpToPx(8)

        val categoryAdapter = CategoryAdapter(categoryList){ position ->
            val selectedCategory = categoryList[position]
            if (selectedCategory == "전체") {
                viewModel.loadLectures("", 1)
            } else {
                viewModel.loadLectures(selectedCategory, 1)
            }
        }
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            addItemDecoration(HorizontalSpacingItemDecoration(spacing))
        }

        // SearchLectureAdapter 생성 (강의 클릭 시 NavGraph를 통해 화면 이동)
//        val searchLectureAdapter = SearchLectureAdapter { lectureId, lectureTitle ->
//            val action = SearchFragmentDirections.actionNavSearchToQuizFragment(
//                lectureId = lectureId,
//                lectureTitle = lectureTitle
//            )
//        val searchLectureAdapter = SearchLectureAdapter { lectureId, userId ->
//            val action = SearchFragmentDirections.actionNavSearchToLectureDetailFragment(
//                lectureId = lectureId,
//                userId = userId
//            )
//            findNavController().navigate(action)
//        }

        val searchLectureAdapter = SearchLectureAdapter { lectureId, userId ->
            val action = SearchFragmentDirections.actionNavSearchToOwnedLectureDetailFragment(
                lectureId = lectureId,
                userId = userId
            )
            findNavController().navigate(action)
        }

        binding.lectureList.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = searchLectureAdapter
            addItemDecoration(
                GridSpaceItemDecoration(spanCount = 1, space = dpToPx(8) )
            )
        }


        // ViewModel의 강의 데이터를 관찰하여 어댑터에 업데이트
        viewModel.lectures.observe(viewLifecycleOwner) { lectureList ->
            searchLectureAdapter.submitList(lectureList)
            Log.d(TAG, "onViewCreated: ${lectureList}")
        }

        // 초기 로딩: 기본값은 "전체"로 모든 강의 데이터 로드
        viewModel.loadLectures("", 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // dp 값을 px로 변환하는 함수
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

}
