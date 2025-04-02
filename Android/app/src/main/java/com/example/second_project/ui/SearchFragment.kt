package com.example.second_project.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var searchLectureAdapter: SearchLectureAdapter

    // 현재 선택된 카테고리 (초기값 "전체")
    private var currentCategory: String = "전체"

    // Handler와 Runnable을 통한 debounce 처리 (실시간 검색)
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val debounceDelay = 500L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 카테고리 리스트 설정
        val categoryList = listOf("전체", "수학", "생물학", "법률", "통계학", "마케팅", "체육")
        val spacing = dpToPx(8)
        categoryAdapter = CategoryAdapter(categoryList) { position ->
            // 업데이트: 선택된 카테고리를 저장
            currentCategory = categoryList[position]
            val keyword = binding.searchInputText.text.toString().trim()
            if (keyword.isNotEmpty()) {
                // 검색어가 있는 경우, 검색 API는 이미 호출되어있다고 가정하고
                // 기존 검색 결과에서 선택된 카테고리에 해당하는 항목만 필터링
                viewModel.searchResults.value?.let { results ->
                    val filtered = if (currentCategory != "전체") {
                        results.filter { it.categoryName == currentCategory }
                    } else {
                        results
                    }
                    searchLectureAdapter.submitList(filtered)
                    Log.d(TAG, "Filtered search results for $currentCategory: $filtered")
                }
            } else {
                // 검색어가 없으면, 기본 강의 목록 로드
                viewModel.loadLectures(if (position == 0) 0 else position, 1)
            }
        }
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            addItemDecoration(HorizontalSpacingItemDecoration(spacing))
        }

        // 2. 강의 결과 RecyclerView 설정 (Grid 레이아웃, 2열)
        searchLectureAdapter = SearchLectureAdapter { lectureId, userId ->
            // 강의 클릭 시, 강의 상세 정보 불러오기 및 상세 페이지 이동
            viewModel.loadLectureDetail(lectureId, userId)
            viewModel.lectureDetail.observe(viewLifecycleOwner) { lectureDetail ->
                lectureDetail?.let {
                    val action = if (it.data.owned == false) {
                        SearchFragmentDirections.actionNavSearchToLectureDetailFragment(
                            lectureId = it.data?.lectureId ?: 0,
                            userId = userId
                        )
                    } else {
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
            addItemDecoration(GridSpaceItemDecoration(spanCount = 2, space = dpToPx(8)))
        }

        // 3. 실시간 검색: TextWatcher 추가 (debounce 처리)
        binding.searchInputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                val keyword = s.toString().trim()
                searchRunnable = Runnable {
                    if (keyword.isNotEmpty()) {
                        // 검색어가 있으면, 검색 API 호출 시 현재 선택된 카테고리로 필터링
                        viewModel.searchLectures(keyword, 1, currentCategory)
                    } else {
                        // 검색어가 비어 있으면 기본 강의 목록 로드
                        viewModel.loadLectures(0, 1)
                    }
                }
                searchHandler.postDelayed(searchRunnable!!, debounceDelay)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 4. Clear 버튼: 검색 입력창 초기화 및 기본 강의 로드 (검색 결과도 초기화)
        binding.clearBtn.setOnClickListener {
            binding.searchInputText.text?.clear()
            // 선택된 카테고리는 그대로 유지 (검색 기록이 유지)
            viewModel.loadLectures(0, 1)
        }

        // 5. Observer: 검색 결과 LiveData 업데이트 (검색어 입력 시)
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            // 검색어가 있는 경우에만 업데이트
            if (binding.searchInputText.text.toString().trim().isNotEmpty()) {
                val filtered = if (currentCategory != "전체") {
                    results.filter { it.categoryName == currentCategory }
                } else {
                    results
                }
                searchLectureAdapter.submitList(filtered)
                Log.d(TAG, "Search results updated: $filtered")
            }
        }

        // 6. Observer: 기본 강의 목록 LiveData 업데이트 (검색어가 없을 때)
        viewModel.lectures.observe(viewLifecycleOwner) { lectureList ->
            if (binding.searchInputText.text.toString().trim().isEmpty()) {
                searchLectureAdapter.submitList(lectureList)
                Log.d(TAG, "Category lectures: $lectureList")
            }
        }

        // 7. 초기 로딩: 기본 카테고리 ("전체") 강의 로드
        viewModel.loadLectures(0, 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
