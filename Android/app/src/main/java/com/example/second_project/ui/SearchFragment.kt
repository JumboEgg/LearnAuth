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
    private val debounceDelay = 50L

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

        // ------------------------------------------------
        // 1) 카테고리 RecyclerView
        // ------------------------------------------------
        val spacing = dpToPx(8)
        // SearchFragment.kt - 카테고리 클릭 리스너 부분 수정
// SearchFragment.kt - 카테고리 항목 클릭 처리 개선
// 카테고리 어댑터 클릭 리스너
        categoryAdapter = CategoryAdapter(categoryList) { position ->
            // 이미 같은 카테고리를 선택한 경우 무시
            if (currentCategoryPosition == position) return@CategoryAdapter

            // 선택된 카테고리 저장
            currentCategoryPosition = position
            currentCategory = categoryList[position]
            categoryAdapter.setSelectedPosition(position)

            // 중요: UI 초기화 (리스트와 빈 메시지 모두 숨김)
            binding.lectureList.visibility = View.GONE
            binding.emptyTextView.visibility = View.GONE

            // 로딩 표시 시작
            showLoading(true)
            searchLectureAdapter.submitList(emptyList())

            val keyword = binding.searchInputText.text.toString().trim()

            // 검색어 유무에 따라 처리
            if (keyword.isNotEmpty()) {
                // 검색 모드
                viewModel.resetSearchResults()
                // 약간의 지연을 주어 UI가 먼저 업데이트되도록 함
                viewModel.searchLectures(keyword, currentCategory)
            } else {
                // 일반 강의 모드
                viewModel.resetLectures()
                // 약간의 지연을 주어 UI가 먼저 업데이트되도록 함
                viewModel.loadLectures(position)

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
                categoryAdapter.setSelectedPosition(currentCategoryPosition)
            }
        }

        // ------------------------------------------------
        // 2) 강의 결과 RecyclerView (2열 Grid)
        // ------------------------------------------------
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
        searchLectureAdapter.notifyDataSetChanged()

        binding.lectureList.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = searchLectureAdapter
            // DiffUtil 애니메이션으로 인한 깜빡임 방지
            itemAnimator = null
            addItemDecoration(GridSpaceItemDecoration(spanCount = 2, space = dpToPx(2)))

            // 무한 스크롤 처리
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
                                viewModel.loadLectures(currentCategoryPosition)
                            }
                        }
                    }
                }
            })

            if (savedInstanceState == null) {
                // 처음 진입 시 항상 초기 로드 실행 (ViewModel 상태와 무관하게)
                binding.lectureList.visibility = View.GONE
                binding.emptyTextView.visibility = View.GONE
                showLoading(true)
                viewModel.resetLectures()
                viewModel.loadLectures(currentCategoryPosition)
            } else {
                // 상태 복원 시
                val keyword = binding.searchInputText.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    binding.lectureList.visibility = View.GONE
                    binding.emptyTextView.visibility = View.GONE
                    showLoading(true)
                    viewModel.searchLectures(keyword, currentCategory)
                } else {
                    // ViewModel에 데이터가 있으면 바로 표시
                    if (!viewModel.lectures.value.isNullOrEmpty()) {
                        binding.lectureList.visibility = View.VISIBLE
                        searchLectureAdapter.submitList(viewModel.lectures.value)
                    } else {
                        // 데이터가 없으면 로드
                        binding.lectureList.visibility = View.GONE
                        binding.emptyTextView.visibility = View.GONE
                        showLoading(true)
                        viewModel.loadLectures(currentCategoryPosition)
                    }
                }
            }
        }

        // ------------------------------------------------
        // 3) 검색어 입력 리스너 (Debounce)
        // ------------------------------------------------
// SearchFragment.kt의 TextWatcher 부분 수정
        binding.searchInputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // 16자 초과 처리
                s?.let {
                    if (it.length > 16) {
                        it.delete(16, it.length)
                        binding.searchErrorText.visibility = View.VISIBLE
                    } else {
                        binding.searchErrorText.visibility = View.GONE
                    }
                }

                // 검색 debounce 처리
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                val keyword = s.toString().trim()

                // UI 즉시 업데이트 (로딩 상태 표시)
                if (keyword.isEmpty()) {
                    // 검색어가 지워진 경우, 기존 강의 목록 표시 준비
                    binding.emptyTextView.visibility = View.GONE
                }

                searchRunnable = Runnable {
                    if (keyword.isNotEmpty()) {
                        // 검색 시작
                        binding.emptyTextView.visibility = View.GONE
                        binding.lectureList.visibility = View.GONE
                        showLoading(true)
                        viewModel.resetSearchResults()
                        viewModel.searchLectures(keyword, currentCategory)
                    } else {
                        // 검색어 삭제 시
                        showLoading(true)
                        binding.emptyTextView.visibility = View.GONE
                        binding.lectureList.visibility = View.GONE

                        // 기존 강의가 있다면 먼저 표시하고 나서 새로 로드
                        if (!viewModel.lectures.value.isNullOrEmpty()) {
                            binding.lectureList.visibility = View.VISIBLE
                            searchLectureAdapter.submitList(viewModel.lectures.value)
                            showLoading(false)
                        } else {
                            // 없으면 새로 로드
                            viewModel.resetLectures()
                            viewModel.loadLectures(currentCategoryPosition)
                        }
                    }
                }

                // 검색어가 완전히 지워진 경우 딜레이를 줄여서 빠르게 반응
                val delay = if (keyword.isEmpty()) 10L else debounceDelay
                searchHandler.postDelayed(searchRunnable!!, delay)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        // ------------------------------------------------
        // 4) Clear 버튼
        // ------------------------------------------------
        binding.clearBtn.setOnClickListener {
            binding.searchInputText.text?.clear()
            viewModel.resetSearchResults()

            // 카테고리는 유지하되, 기존 강의 목록이 비었다면 새로 로드
            if (viewModel.lectures.value.isNullOrEmpty()) {
                showLoading(true)
                viewModel.resetLectures()
                viewModel.loadLectures(currentCategoryPosition)
            } else {
                // ViewModel에 이미 데이터가 있으면 그대로 submit
                searchLectureAdapter.submitList(viewModel.lectures.value)
            }
        }

        // ------------------------------------------------
        // 5) 검색 결과 관찰
        // ------------------------------------------------
// 검색 결과 관찰
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            val keyword = binding.searchInputText.text.toString().trim()
            // 검색 중인 상태일 때만 반영
            if (keyword.isNotEmpty()) {
                // 로딩 상태 확인 - 로딩 중에도 결과가 있으면 UI 업데이트를 허용
                showLoading(viewModel.isLoading.value == true)

                if (!viewModel.isLoading.value!!) {  // 로딩이 끝났을 때
                    if (results.isEmpty()) {
                        // 결과가 없을 때 빈 메시지 표시
                        binding.emptyTextView.visibility = View.VISIBLE
                        binding.lectureList.visibility = View.GONE
                    } else {
                        // 결과가 있을 때 리스트 표시
                        binding.emptyTextView.visibility = View.GONE
                        binding.lectureList.visibility = View.VISIBLE
                        searchLectureAdapter.submitList(results)
                    }
                }
            }
        }    // 6) 일반 강의 목록 관찰
        // ------------------------------------------------
// SearchFragment.kt의 lecture observer 개선
        viewModel.lectures.observe(viewLifecycleOwner) { lectureList ->
            val keyword = binding.searchInputText.text.toString().trim()
            // 검색어가 없을 때만 강의 목록 갱신
            if (keyword.isEmpty()) {
                if (!viewModel.isLoading.value!!) {
                    if (lectureList.isEmpty()) {
                        // 강의가 없을 때 빈 메시지 표시
                        binding.emptyTextView.visibility = View.VISIBLE
                        binding.lectureList.visibility = View.GONE
                        Log.d(TAG, "빈 강의 목록 표시 - 검색어 없음")
                    } else {
                        // 강의가 있을 때 리스트 표시
                        binding.emptyTextView.visibility = View.GONE
                        binding.lectureList.visibility = View.VISIBLE
                        searchLectureAdapter.submitList(lectureList)
                        Log.d(TAG, "강의 목록 표시: ${lectureList.size}개")
                    }
                }
            }
        }
// viewModel.isLoading.observe 부분을 수정
// viewModel.isLoading.observe 부분 수정
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "로딩 상태 변경: $isLoading")

            // 로딩 UI 업데이트
            binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE

            // 로딩 중에는 빈 메시지 숨기기
            if (isLoading) {
                binding.emptyTextView.visibility = View.GONE
            } else {
                // 로딩 완료 시 데이터 상태에 따라 UI 업데이트
                val keyword = binding.searchInputText.text.toString().trim()
                val currentList = if (keyword.isNotEmpty())
                    viewModel.searchResults.value
                else
                    viewModel.lectures.value

                // 데이터 결과에 따라 UI 결정
                if (currentList.isNullOrEmpty()) {
                    // 빈 결과 메시지 표시
                    binding.emptyTextView.visibility = View.VISIBLE
                    binding.lectureList.visibility = View.GONE

                    // 디버그 로그
                    Log.d(TAG, "빈 결과 표시: keyword=$keyword, category=$currentCategory")
                } else {
                    // 결과 리스트 표시
                    binding.emptyTextView.visibility = View.GONE
                    binding.lectureList.visibility = View.VISIBLE

                    // 키워드에 맞는 어댑터에 데이터 제출
                    if (keyword.isEmpty()) {
                        searchLectureAdapter.submitList(viewModel.lectures.value)
                    } else {
                        searchLectureAdapter.submitList(viewModel.searchResults.value)
                    }

                    // 디버그 로그
                    Log.d(TAG, "결과 표시: ${currentList.size}개 항목")
                }
            }
        }        // 7) 초기 진입 시 기본 강의 로드
        // ------------------------------------------------
        if (savedInstanceState == null && viewModel.lectures.value.isNullOrEmpty()) {
            // 처음 진입 & ViewModel이 비어있는 경우에만 초기 로드
            showLoading(true)
            viewModel.resetLectures()
            viewModel.loadLectures(currentCategoryPosition)
        } else {
            // 이미 ViewModel에 강의 목록이 있거나 상태가 복원된 경우
            val keyword = binding.searchInputText.text.toString().trim()
            if (keyword.isNotEmpty()) {
                showLoading(true)
                viewModel.searchLectures(keyword, currentCategory)
            } else {
                // 만약 ViewModel의 lectures가 비어있지 않다면, 그대로 submitList 해도 무방
                if (!viewModel.lectures.value.isNullOrEmpty()) {
                    searchLectureAdapter.submitList(viewModel.lectures.value)
                } else {
                    showLoading(true)
                    viewModel.loadLectures(currentCategoryPosition)
                }
            }
        }

    }

    // 로딩 표시/숨김 함수
    private fun showLoading(isLoading: Boolean) {
        // ViewModel과 UI 모두 업데이트
        viewModel._isLoading.value = isLoading
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE

        // 로딩 중일 때는 빈 결과 메시지 숨기기
        if (isLoading) {
            binding.emptyTextView.visibility = View.GONE
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