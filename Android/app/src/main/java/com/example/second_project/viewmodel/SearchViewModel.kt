package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.request.Lecture
import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.data.repository.LectureRepository
import com.example.second_project.data.repository.SearchRepository

class SearchViewModel : ViewModel() {

    private val repository = LectureRepository()
    private val lectureDetailRepository = LectureDetailRepository()
    private val searchRepository = SearchRepository

    private val _lectures = MutableLiveData<List<Lecture>>()
    val lectures: LiveData<List<Lecture>> = _lectures

    private val _searchResults = MutableLiveData<List<Lecture>>()
    val searchResults: LiveData<List<Lecture>> = _searchResults

    // 강의 상세 정보를 위한 새로운 LiveData 생성 함수
    fun loadLectureDetail(lectureId: Int, userId: Int): LiveData<LectureDetailResponse?> {
        val lectureDetailLiveData = MutableLiveData<LectureDetailResponse?>()
        lectureDetailRepository.fetchLectureDetail(lectureId, userId).observeForever { response ->
            lectureDetailLiveData.value = response
        }
        return lectureDetailLiveData
    }

    fun loadLectures(categoryId: Int, page: Int) {
        repository.fetchLectures(categoryId, page).observeForever { lectureList ->
            _lectures.value = lectureList
        }
    }

    fun searchLectures(keyword: String, page: Int) {
        searchRepository.searchLectures(keyword, page).observeForever { searchData ->
            _searchResults.value = searchData?.searchResults ?: emptyList()
        }
    }

    // 새로운 검색 API 호출 함수: 검색어와 선택된 카테고리명을 모두 반영
    fun searchLectures(keyword: String, page: Int, category: String) {
        searchRepository.searchLectures(keyword, page).observeForever { searchData ->
            val results = searchData?.searchResults ?: emptyList()
            // "전체"가 아니라면, 강의의 categoryName이 선택된 카테고리와 일치하는 결과만 필터링
            _searchResults.value = if (category != "전체") {
                results.filter { it.categoryName == category }
            } else {
                results
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    // observeForever 사용 시 ViewModel 소멸 시 리소스 해제 고려 필요
    override fun onCleared() {
        super.onCleared()
        // 필요하다면 Repository에서 등록한 Observer를 제거하는 로직 추가
    }
}
