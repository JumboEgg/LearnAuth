package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.request.Lecture
import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.data.model.dto.response.MostCompletedLecturesResponse
import com.example.second_project.data.model.dto.response.MostRecentLecturesResponse
import com.example.second_project.data.model.dto.response.RandomLecturesResponse
import com.example.second_project.data.repository.LectureDetailRepository
import com.example.second_project.network.ApiClient
import com.example.second_project.network.LectureApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {
    // 기존 텍스트 LiveData
    private val _text = MutableLiveData("러너스")
    val text: LiveData<String> = _text

    // 가장 많이 수료한 강의 리스트 LiveData
    private val _mostCompletedLectures = MutableLiveData<List<Lecture>>()
    val mostCompletedLectures: LiveData<List<Lecture>> = _mostCompletedLectures

    // "최근 등록 강의" LiveData
    private val _recentLectures = MutableLiveData<List<Lecture>>()
    val recentLectures: LiveData<List<Lecture>> = _recentLectures

    // "(닉네임) 님을 위한 추천 강의" LiveData (random 강의)
    private val _randomLectures = MutableLiveData<List<Lecture>>()
    val randomLectures: LiveData<List<Lecture>> = _randomLectures
    
    // LectureDetailRepository 추가
    private val lectureDetailRepository = LectureDetailRepository()
    
    init {
        fetchMostCompletedLectures()
        fetchRecentLectures()
        fetchRandomLectures()
    }

    fun fetchMostCompletedLectures() {
        val lectureApiService = ApiClient.retrofit.create(LectureApiService::class.java)
        lectureApiService.getMostCompletedLectures().enqueue(object : Callback<MostCompletedLecturesResponse> {
            override fun onResponse(
                call: Call<MostCompletedLecturesResponse>,
                response: Response<MostCompletedLecturesResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    _mostCompletedLectures.value = response.body()!!.data
                } else {
                    _mostCompletedLectures.value = emptyList()
                }
            }

            override fun onFailure(call: Call<MostCompletedLecturesResponse>, t: Throwable) {
                _mostCompletedLectures.value = emptyList()
            }
        })
    }

    fun fetchRecentLectures() {
        val lectureApiService = ApiClient.retrofit.create(LectureApiService::class.java)
        lectureApiService.getMostRecentLectures().enqueue(object : Callback<MostRecentLecturesResponse> {
            override fun onResponse(
                call: Call<MostRecentLecturesResponse>,
                response: Response<MostRecentLecturesResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    _recentLectures.value = response.body()!!.data
                } else {
                    _recentLectures.value = emptyList()
                }
            }

            override fun onFailure(call: Call<MostRecentLecturesResponse>, t: Throwable) {
                _recentLectures.value = emptyList()
            }
        })
    }

    fun fetchRandomLectures() {
        val lectureApiService = ApiClient.retrofit.create(LectureApiService::class.java)
        lectureApiService.getRandomLectures().enqueue(object : Callback<RandomLecturesResponse> {
            override fun onResponse(
                call: Call<RandomLecturesResponse>,
                response: Response<RandomLecturesResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    _randomLectures.value = response.body()!!.data
                } else {
                    _randomLectures.value = emptyList()
                }
            }

            override fun onFailure(call: Call<RandomLecturesResponse>, t: Throwable) {
                _randomLectures.value = emptyList()
            }
        })
    }
    
    // 강의 상세 정보를 불러오는 메서드 추가
    fun loadLectureDetail(lectureId: Int, userId: Int): LiveData<LectureDetailResponse?> {
        val lectureDetailLiveData = MutableLiveData<LectureDetailResponse?>()
        lectureDetailRepository.fetchLectureDetail(lectureId, userId).observeForever { response ->
            lectureDetailLiveData.value = response
        }
        return lectureDetailLiveData
    }
}
