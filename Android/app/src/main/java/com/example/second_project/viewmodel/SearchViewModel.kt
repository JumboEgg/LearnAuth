package com.example.second_project.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.request.Lecture
import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.data.repository.LectureRepository
import com.example.second_project.data.repository.SearchRepository

private const val TAG = "SearchViewModel"

class SearchViewModel : ViewModel() {
    private val repository = LectureRepository()
    private val lectureDetailRepository = LectureDetailRepository()
    private val searchRepository = SearchRepository

    // -----------------------------
    // (1) 기본 강의 목록 페이징용
    // -----------------------------
    private val _lectures = MutableLiveData<List<Lecture>>()
    val lectures: LiveData<List<Lecture>> get() = _lectures

    // 누적 저장할 리스트 & 페이지 정보
    private val loadedLectures = mutableListOf<Lecture>()
    private var currentLecturePage = 1
    private var isLectureLoading = false
    private var isLectureLastPage = false

    // 현재 카테고리 ID 추적 (요청 식별용)
    private var currentCategoryId = 0

    /** 기본 강의 목록을 초기화할 때 사용 **/
    fun resetLectures() {
        loadedLectures.clear()
        currentLecturePage = 1
        isLectureLoading = false
        isLectureLastPage = false
        _lectures.value = emptyList()

        Log.d(TAG, "강의 목록 초기화됨")
    }

    /**
     * 페이지 단위로 강의를 불러와 누적합니다.
     * categoryId는 "전체=0, 수학=1, ..." 식으로 전달한다고 가정.
     */
    fun loadLectures(categoryId: Int) {
        // 이미 로드 중인데 카테고리가 변경된 경우, 이전 요청 무시 플래그 설정
        if (currentCategoryId != categoryId) {
            // 카테고리가 변경되면 데이터 초기화
            resetLectures()
            currentCategoryId = categoryId

            Log.d(TAG, "카테고리 변경됨: $categoryId")
        }

        if (isLectureLoading || isLectureLastPage) return
        isLectureLoading = true

        // 현재 요청에 대한 카테고리 ID 저장 (클로저에서 사용)
        val requestCategoryId = categoryId
        Log.d(TAG, "강의 데이터 요청 시작: 카테고리=$requestCategoryId, 페이지=$currentLecturePage")

        repository.fetchLectures(categoryId, currentLecturePage).observeOnce { lectureList ->
            // 요청 시점의 카테고리와 현재 카테고리가 다르면 결과 무시
            if (requestCategoryId != currentCategoryId) {
                Log.d(TAG, "카테고리 불일치로 결과 무시: 요청=$requestCategoryId, 현재=$currentCategoryId")
                isLectureLoading = false
                return@observeOnce
            }

            if (lectureList.isNotEmpty()) {
                loadedLectures.addAll(lectureList)
                _lectures.value = loadedLectures.toList()
                currentLecturePage++
                Log.d(TAG, "강의 데이터 로드 성공: ${lectureList.size}개 추가, 총 ${loadedLectures.size}개")
            } else {
                // 더 이상 가져올 데이터가 없는 경우
                isLectureLastPage = true
                Log.d(TAG, "더 이상 로드할 강의 없음 (마지막 페이지)")
            }
            isLectureLoading = false
        }
    }

    // -----------------------------
    // (2) 검색 결과 페이징용
    // -----------------------------
    private val _searchResults = MutableLiveData<List<Lecture>>()
    val searchResults: LiveData<List<Lecture>> get() = _searchResults

    private val loadedSearchResults = mutableListOf<Lecture>()
    private var currentSearchPage = 1
    private var isSearchLoading = false
    private var isSearchLastPage = false

    // 마지막으로 검색했던 keyword & category 기억 (바뀌면 reset)
    private var lastSearchKeyword: String = ""
    private var lastSearchCategory: String = "전체"

    // 검색 요청의 고유 식별자 (변경될 때마다 증가)
    private var searchRequestToken = 0

    /** 검색 결과 초기화 **/
    fun resetSearchResults() {
        loadedSearchResults.clear()
        currentSearchPage = 1
        isSearchLoading = false
        isSearchLastPage = false
        lastSearchKeyword = ""
        lastSearchCategory = "전체"
        _searchResults.value = emptyList()

        // 요청 토큰 증가 (이전 요청 무효화)
        searchRequestToken++

        Log.d(TAG, "검색 결과 초기화됨 (토큰 $searchRequestToken)")
    }

    /**
     * 검색어와 카테고리에 따라 강의를 페이지 단위로 조회하고,
     * 가져온 데이터를 누적해서 _searchResults에 넣습니다.
     */
    fun searchLectures(keyword: String, category: String) {
        // 이전 검색과 다르면 초기화 (keyword, category가 변경된 경우)
        if (keyword != lastSearchKeyword || category != lastSearchCategory) {
            resetSearchResults()
            lastSearchKeyword = keyword
            lastSearchCategory = category

            Log.d(TAG, "검색 조건 변경: 키워드='$keyword', 카테고리='$category'")
        }

        if (isSearchLoading || isSearchLastPage) return
        isSearchLoading = true

        // 현재 요청에 대한 토큰 캡처 (클로저에서 사용)
        val currentToken = searchRequestToken
        Log.d(
            TAG,
            "검색 요청 시작: 키워드='$keyword', 카테고리='$category', 토큰=$currentToken, 페이지=$currentSearchPage"
        )

        searchRepository.searchLectures(keyword, currentSearchPage).observeOnce { searchData ->
            // 요청 토큰이 변경되었으면 결과 무시 (새로운 검색이 시작됨)
            if (currentToken != searchRequestToken) {
                Log.d(TAG, "토큰 불일치로 결과 무시: 요청=$currentToken, 현재=$searchRequestToken")
                isSearchLoading = false
                return@observeOnce
            }

            val results = searchData?.searchResults ?: emptyList()
            if (results.isNotEmpty()) {
                // "전체"가 아니라면 필터링
                val filtered = if (category != "전체") {
                    results.filter { it.categoryName == category }
                } else {
                    results
                }

                loadedSearchResults.addAll(filtered)
                _searchResults.value = loadedSearchResults.toList()
                currentSearchPage++

                Log.d(
                    TAG,
                    "검색 결과 로드 성공: 원본=${results.size}개, 필터링 후=${filtered.size}개, 총=${loadedSearchResults.size}개"
                )
            } else {
                isSearchLastPage = true
                Log.d(TAG, "더 이상 검색 결과 없음 (마지막 페이지)")
            }
            isSearchLoading = false
        }
    }

    // -----------------------------
    // (3) 강의 상세 정보
    // -----------------------------
    fun loadLectureDetail(lectureId: Int, userId: Int): LiveData<LectureDetailResponse?> {
        val lectureDetailLiveData = MutableLiveData<LectureDetailResponse?>()

        Log.d(TAG, "강의 상세 정보 요청: lectureId=$lectureId, userId=$userId")
        lectureDetailRepository.fetchLectureDetail(lectureId, userId).observeOnce { response ->
            lectureDetailLiveData.value = response
            Log.d(TAG, "강의 상세 정보 로드 ${if (response != null) "성공" else "실패"}")
        }
        return lectureDetailLiveData
    }

    // -----------------------------
    // (4) 이전 코드와의 호환성
    // -----------------------------
    fun loadLectures(categoryId: Int, page: Int) {
        resetLectures()
        currentCategoryId = categoryId
        // 초기 1페이지부터 다시 불러오기
        loadLectures(categoryId)
    }

    fun searchLectures(keyword: String, page: Int) {
        resetSearchResults()
        searchLectures(keyword, "전체")
    }

    fun searchLectures(keyword: String, page: Int, category: String) {
        resetSearchResults()
        searchLectures(keyword, category)
    }

    // -----------------------------
    // (5) ViewModel 클린업
    // -----------------------------
    override fun onCleared() {
        super.onCleared()
        // observeOnce()에서 한번 호출 후 바로 removeObserver 해주므로
        // 추가로 removeObserver할 필요는 크게 없음
        Log.d(TAG, "ViewModel cleared")
    }
}

fun <T> LiveData<T>.observeOnce(onChangeHandler: (T) -> Unit) {
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            onChangeHandler(value)
        }
    }
    observeForever(observer)
}