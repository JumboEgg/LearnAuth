package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.request.Lecture
import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.data.repository.LectureRepository
import com.example.second_project.data.repository.SearchRepository

// 만약 별도 파일이 아니라 여기 아래에 observeOnce를 두고 싶다면 이렇게 둘 수도 있습니다.
//fun <T> LiveData<T>.observeOnce(onChangeHandler: (T) -> Unit) {
//    val observer = object : Observer<T> {
//        override fun onChanged(value: T) {
//            removeObserver(this)
//            onChangeHandler(value)
//        }
//    }
//    observeForever(observer)
//}

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

    /** 기본 강의 목록을 초기화할 때 사용 **/
    fun resetLectures() {
        loadedLectures.clear()
        currentLecturePage = 1
        isLectureLoading = false
        isLectureLastPage = false
        _lectures.value = emptyList()
    }

    /**
     * 페이지 단위로 강의를 불러와 누적합니다.
     * categoryId는 "전체=0, 수학=1, ..." 식으로 전달한다고 가정.
     */
    fun loadLectures(categoryId: Int) {
        if (isLectureLoading || isLectureLastPage) return
        isLectureLoading = true

        // 기존 observeForever(...) → observeOnce(...)
        repository.fetchLectures(categoryId, currentLecturePage).observeOnce { lectureList ->
            if (lectureList.isNotEmpty()) {
                loadedLectures.addAll(lectureList)
                _lectures.value = loadedLectures.toList()
                currentLecturePage++
            } else {
                // 더 이상 가져올 데이터가 없는 경우
                isLectureLastPage = true
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

    /** 검색 결과 초기화 **/
    fun resetSearchResults() {
        loadedSearchResults.clear()
        currentSearchPage = 1
        isSearchLoading = false
        isSearchLastPage = false
        lastSearchKeyword = ""
        lastSearchCategory = "전체"
        _searchResults.value = emptyList()
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
        }

        if (isSearchLoading || isSearchLastPage) return
        isSearchLoading = true

        // 기존 observeForever(...) → observeOnce(...)
        searchRepository.searchLectures(keyword, currentSearchPage).observeOnce { searchData ->
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
            } else {
                isSearchLastPage = true
            }
            isSearchLoading = false
        }
    }

    // -----------------------------
    // (3) 강의 상세 정보
    // -----------------------------
    fun loadLectureDetail(lectureId: Int, userId: Int): LiveData<LectureDetailResponse?> {
        val lectureDetailLiveData = MutableLiveData<LectureDetailResponse?>()
        // 여기도 observeForever(...) → observeOnce(...)
        lectureDetailRepository.fetchLectureDetail(lectureId, userId).observeOnce { response ->
            lectureDetailLiveData.value = response
        }
        return lectureDetailLiveData
    }

    // -----------------------------
    // (4) 이전 코드와의 호환성
    // -----------------------------
    fun loadLectures(categoryId: Int, page: Int) {
        resetLectures()
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

