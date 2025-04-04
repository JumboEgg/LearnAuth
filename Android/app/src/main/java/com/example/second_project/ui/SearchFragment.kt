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
import androidx.recyclerview.widget.RecyclerView
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

    // 카테고리 목록 (인덱스 = categoryId 가정)
    private val categoryList = listOf("전체", "수학", "생물학", "법률", "통계학", "마케팅", "체육")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 카테고리 RecyclerView
        val spacing = dpToPx(8)
        categoryAdapter = CategoryAdapter(categoryList) { position ->
            // 선택된 카테고리를 저장
            currentCategory = categoryList[position]

            val keyword = binding.searchInputText.text.toString().trim()
            if (keyword.isNotEmpty()) {
                // 검색 모드
                // => ViewModel의 searchLectures() 다시 호출(카테고리가 바뀌었으므로 reset됨)
                viewModel.searchLectures(keyword, currentCategory)
            } else {
                // 일반 강의 모드
                // => ViewModel의 loadLectures() 다시 호출 (카테고리가 바뀌었으므로 reset)
                viewModel.resetLectures()  // 혹은 내부에서 자동 reset
                viewModel.loadLectures(position) // 0=전체, 1=수학, ...
            }
        }
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            addItemDecoration(HorizontalSpacingItemDecoration(spacing))
        }

        // 2) 강의 결과 RecyclerView (2열 Grid)
        searchLectureAdapter = SearchLectureAdapter { lectureId, userId ->
            // 강의 클릭 시 상세 정보 불러오기 -> 이동
            val lectureDetailLiveData = viewModel.loadLectureDetail(lectureId, userId)
            lectureDetailLiveData.observe(viewLifecycleOwner) { lectureDetail ->
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
            // ---------------------------
            // (A) 스크롤 리스너로 무한 스크롤 처리
            // ---------------------------
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) { // 아래로 스크롤 중
                        val layoutManager = recyclerView.layoutManager as GridLayoutManager
                        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                        val totalItemCount = layoutManager.itemCount

                        // 끝에 가까워지면 다음 페이지 요청
                        if (lastVisibleItem >= totalItemCount - 2) {
                            val keyword = binding.searchInputText.text.toString().trim()
                            if (keyword.isNotEmpty()) {
                                // 검색 모드
                                viewModel.searchLectures(keyword, currentCategory)
                            } else {
                                // 일반 강의 모드
                                viewModel.loadLectures(categoryList.indexOf(currentCategory))
                            }
                        }
                    }
                }
            })
        }

        // 3) 검색어 입력 리스너 (Debounce)
        binding.searchInputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                val keyword = s.toString().trim()

                searchRunnable = Runnable {
                    if (keyword.isNotEmpty()) {
                        // 검색 모드
                        viewModel.searchLectures(keyword, currentCategory)
                    } else {
                        // 검색어가 없으면 일반 강의 목록 로드
                        viewModel.resetLectures()
                        viewModel.loadLectures(0)
                    }
                }
                searchHandler.postDelayed(searchRunnable!!, debounceDelay)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 4) Clear 버튼
        binding.clearBtn.setOnClickListener {
            binding.searchInputText.text?.clear()
            // 검색 관련 LiveData도 초기화
            viewModel.resetSearchResults()
            // 카테고리는 그대로 두고, 일반 강의 목록 로드
            viewModel.resetLectures()
            viewModel.loadLectures(categoryList.indexOf(currentCategory))
        }

        // 5) 검색 결과 관찰
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            // 검색어가 있을 때만 갱신
            if (binding.searchInputText.text.toString().trim().isNotEmpty()) {
                searchLectureAdapter.submitList(results)
                Log.d(TAG, "Search results updated: $results")
            }
        }

        // 6) 일반 강의 목록 관찰
        viewModel.lectures.observe(viewLifecycleOwner) { lectureList ->
            if (binding.searchInputText.text.toString().trim().isEmpty()) {
                searchLectureAdapter.submitList(lectureList)
                Log.d(TAG, "Category lectures: $lectureList")
            }
        }

        // 7) 초기 진입 시 기본 강의 로드
        viewModel.resetLectures()
        viewModel.loadLectures(0) // "전체"
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
