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
private const val KEY_CURRENT_CATEGORY = "current_category"
private const val KEY_CURRENT_CATEGORY_POSITION = "current_category_position"

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var searchLectureAdapter: SearchLectureAdapter

    // 현재 선택된 카테고리 (초기값 "전체")
    private var currentCategory: String = "전체"
    private var currentCategoryPosition: Int = 0

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

        // 저장된 상태 복원
        savedInstanceState?.let {
            currentCategory = it.getString(KEY_CURRENT_CATEGORY, "전체")
            currentCategoryPosition = it.getInt(KEY_CURRENT_CATEGORY_POSITION, 0)
        }

        // 1) 카테고리 RecyclerView
        val spacing = dpToPx(8)
        categoryAdapter = CategoryAdapter(categoryList) { position ->
            // 선택된 카테고리를 저장
            currentCategory = categoryList[position]
            currentCategoryPosition = position

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

        // 저장된 카테고리 위치로 스크롤
        binding.categoryRecyclerView.post {
            if (currentCategoryPosition > 0) {
                binding.categoryRecyclerView.scrollToPosition(currentCategoryPosition)
                // 카테고리 선택 상태 복원
                categoryAdapter.setSelectedPosition(currentCategoryPosition)
            }
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
            addItemDecoration(GridSpaceItemDecoration(spanCount = 2, space = dpToPx(2)))
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
                // 1) 16자 초과 시 잘라내기
                s?.let {
                    if (it.length > 16) {
                        // 16자 이후 부분 삭제
                        it.delete(16, it.length)
                        // 안내 문구 표시
                        binding.searchErrorText.visibility = View.VISIBLE
                    } else {
                        // 16자 이하이면 안내 문구 숨김
                        binding.searchErrorText.visibility = View.GONE
                    }
                }

                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                val keyword = s.toString().trim()

                searchRunnable = Runnable {
                    if (keyword.isNotEmpty()) {
                        // 검색 모드
                        viewModel.searchLectures(keyword, currentCategory)
                    } else {
                        // 검색어가 없으면 일반 강의 목록 로드
                        viewModel.resetLectures()
                        viewModel.loadLectures(currentCategoryPosition)
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
            viewModel.loadLectures(currentCategoryPosition)
        }

        // 5) 검색 결과 관찰
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            val keyword = binding.searchInputText.text.toString().trim()

            // 1) 검색어가 존재하고 (즉, 사용자가 검색 중),
            // 2) 결과가 비었을 때 → '등록된 강의 없음' 안내 표시
            if (keyword.isNotEmpty()) {
                if (results.isEmpty()) {
                    // (a) 안내 문구만 표시
                    binding.emptyTextView.visibility = View.VISIBLE
                    binding.lectureList.visibility = View.GONE
                } else {
                    // (b) 결과가 있으면 RecyclerView 보여주고, 안내 문구 숨김
                    binding.emptyTextView.visibility = View.GONE
                    binding.lectureList.visibility = View.VISIBLE
                    searchLectureAdapter.submitList(results)
                }
                Log.d(TAG, "Search results updated: $results")
            }
        }


        // 6) 일반 강의 목록 관찰
        viewModel.lectures.observe(viewLifecycleOwner) { lectureList ->
            val keyword = binding.searchInputText.text.toString().trim()
            if (keyword.isEmpty()) {
                if (lectureList.isEmpty()) {
                    binding.emptyTextView.visibility = View.VISIBLE
                    binding.lectureList.visibility = View.GONE
                } else {
                    binding.emptyTextView.visibility = View.GONE
                    binding.lectureList.visibility = View.VISIBLE
                    searchLectureAdapter.submitList(lectureList)
                }
                Log.d(TAG, "Category lectures updated: $lectureList")
            }
        }


        // 7) 초기 진입 시 기본 강의 로드
        if (savedInstanceState == null) {
            // 처음 생성된 경우에만 초기화
            viewModel.resetLectures()
            viewModel.loadLectures(currentCategoryPosition) // 저장된 카테고리 위치 또는 "전체"
        } else {
            // 상태 복원 시 해당 카테고리의 강의 로드
            val keyword = binding.searchInputText.text.toString().trim()
            if (keyword.isNotEmpty()) {
                viewModel.searchLectures(keyword, currentCategory)
            } else {
                viewModel.loadLectures(currentCategoryPosition)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 현재 상태 저장
        outState.putString(KEY_CURRENT_CATEGORY, currentCategory)
        outState.putInt(KEY_CURRENT_CATEGORY_POSITION, currentCategoryPosition)
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
