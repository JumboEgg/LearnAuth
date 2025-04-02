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
import com.example.second_project.UserSession.userId
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
        val categoryList = listOf("전체", "수학", "생물학", "법률", "통계학", "마케팅", "체육")

        // dp -> px로 변환
        val spacing = dpToPx(8)

        val categoryAdapter = CategoryAdapter(categoryList){ position ->
            val selectedCategory = position
            if (selectedCategory == 0) {
                viewModel.loadLectures(0, 1)
            } else {
                viewModel.loadLectures(selectedCategory, 1)
            }
        }
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            addItemDecoration(HorizontalSpacingItemDecoration(spacing))
        }

        val searchLectureAdapter = SearchLectureAdapter { lectureId, userId ->


            // 강의를 클릭했을 때, 강의 상세 정보를 불러옵니다.
            viewModel.loadLectureDetail(lectureId, userId)

            viewModel.lectureDetail.observe(viewLifecycleOwner) { lectureDetail ->
                lectureDetail?.let {
                    val action = if (it.data.owned == false) {
                        // 보유하지 않은 강의라면
                        Log.d(TAG, "onViewCreated: 보유?? ${it.data.owned}, ${it.data.lectureId}")
                        SearchFragmentDirections.actionNavSearchToLectureDetailFragment(
                            lectureId = it.data?.lectureId ?: 0,
                            userId = userId
                        )
                    } else {
                        // 보유한 강의라면
                        Log.d(TAG, "onViewCreated: 보유?? ${it.data.owned}, ${it.data.lectureId}")
                        SearchFragmentDirections.actionNavSearchToOwnedLectureDetailFragment(
                            lectureId = it.data?.lectureId ?: 0,
                            userId = userId
                        )
                    }
                    findNavController().navigate(action)
                }
            }
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

//        viewModel.lectureDetail.observe(viewLifecycleOwner) { lectureDetail ->
//            lectureDetail?.let {
//                val action = if (it.data.owned == false) {
//                    // 보유하지 않은 강의라면
//                    Log.d(TAG, "onViewCreated: 보유?? ${it.data.owned}, ${it.data.lectureId}")
//                    SearchFragmentDirections.actionNavSearchToLectureDetailFragment(
//                        lectureId = it.data?.lectureId ?: 0,
//                        userId = userId
//                    )
//                } else {
//                    // 보유한 강의라면
//                    Log.d(TAG, "onViewCreated: 보유?? ${it.data.owned}, ${it.data.lectureId}")
//                    SearchFragmentDirections.actionNavSearchToOwnedLectureDetailFragment(
//                        lectureId = it.data?.lectureId ?: 0,
//                        userId = userId
//                    )
//                }
//
//                findNavController().navigate(action)
//            }
//        }


            // 초기 로딩: 기본값은 "전체"로 모든 강의 데이터 로드
        viewModel.loadLectures(0, 1)
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
