package com.example.second_project.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.second_project.data.model.dto.RegisterTempQuiz
import com.example.second_project.data.model.dto.RegisterTempSubLecture
import com.example.second_project.data.model.dto.request.Quiz
import com.example.second_project.data.model.dto.request.Ratio
import com.example.second_project.data.model.dto.request.RegisterLectureRequest
import com.example.second_project.data.model.dto.request.SubLecture
import com.example.second_project.data.model.dto.response.CategoryResponse
import com.example.second_project.network.ApiClient.registerService
import com.example.second_project.network.YoutubeApiClient
import com.example.second_project.utils.IpfsUtils
import com.example.second_project.utils.YoutubeUtil
import kotlinx.coroutines.launch
import com.example.second_project.data.model.dto.request.QuizOption
import com.example.second_project.data.model.dto.response.RegisterEmailResponse

class RegisterViewModel : ViewModel() {
    // 등록 강의 카테고리 드롭다운 메뉴
    private val _categoryList = MutableLiveData<List<CategoryResponse>>()
    val categoryList: LiveData<List<CategoryResponse>> = _categoryList
    val tempSubLectures = mutableListOf<RegisterTempSubLecture>()

    // 사용자 검색 결과 상태
    private val _searchResults = MutableLiveData<List<RegisterEmailResponse>>()
    val searchResults: LiveData<List<RegisterEmailResponse>> = _searchResults
    private val _totalResults = MutableLiveData<Int>()
    val totalResults: LiveData<Int> = _totalResults
    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int> = _currentPage

    // IPFS 업로드 상태
    private val _ipfsUploadState = MutableLiveData<IpfsUploadState>()
    val ipfsUploadState: LiveData<IpfsUploadState> = _ipfsUploadState

    // IPFS 해시 저장
    private var ipfsHash: String? = null

    // 기본 강의 정보 (Getter와 Setter 최적화)
    private var _title: String = ""
    var title: String
        get() = _title
        set(value) {
            _title = value
        }

    private var _categoryName: String = ""
    var categoryName: String
        get() = _categoryName
        set(value) {
            _categoryName = value
        }

    private var _goal: String = ""
    var goal: String
        get() = _goal
        set(value) {
            _goal = value
        }

    private var _description: String = ""
    var description: String
        get() = _description
        set(value) {
            _description = value
        }

    var price: Int = 0
    val ratios = mutableListOf<Ratio>() // 강의자/참여자
    val subLectures = mutableListOf<SubLecture>()
    val quizzes = mutableListOf<Quiz>()

    // 파일 정보
    var selectedLectureFileName: String? = null
    var selectedLectureFileUri: Uri? = null

    fun fetchCategories() {
        viewModelScope.launch {
            runCatching {
                registerService.getCategories()
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    _categoryList.value = response.body()?.data ?: emptyList()
                } else {
                    Log.e("fetchCategories", "fetchCategories Error")
                }
            }.onFailure { throwable ->
                Log.e("fetchCategories", "❗예외 발생: ${throwable.message}")
            }
        }
    }

    fun reset() {
        _title = ""
        _categoryName = ""
        _goal = ""
        _description = ""
        price = 0
        ratios.clear()
        subLectures.clear()
        quizzes.clear()
        tempSubLectures.clear()
        tempQuizzes.clear()
        selectedLectureFileName = null
        selectedLectureFileUri = null
        ipfsHash = null
    }

    fun toRequest(): RegisterLectureRequest {
        return RegisterLectureRequest(
            title = _title.trim(),
            categoryName = _categoryName,
            goal = _goal.trim(),
            description = _description,
            price = price,
            ratios = ratios,
            subLectures = subLectures,
            quizzes = quizzes,
            cid = ipfsHash
        )
    }

    fun isValid(): Boolean {
        if (_title.isBlank()) {
            Log.e("isValid", "제목이 비어있음")
            return false
        }
        if (_categoryName.isBlank()) {
            Log.e("isValid", "카테고리명이 비어있음")
            return false
        }
        if (_goal.isBlank()) {
            Log.e("isValid", "목표가 비어있음")
            return false
        }
        if (_description.isBlank()) {
            Log.e("isValid", "설명이 비어있음")
            return false
        }
        if (price < 0) {
            Log.e("isValid", "가격이 0 이하임")
            return false
        }
        if (ratios.isEmpty()) {
            Log.e("isValid", "참여자 비율 정보 없음")
            return false
        }
        if (!ratios.any { it.lecturer }) {
            Log.e("isValid", "강의자로 지정된 사람이 없음")
            return false
        }
        if (subLectures.isEmpty()) {
            Log.e("isValid", "개별 강의 정보 없음")
            return false
        }
        if (quizzes.size < 3) {
            Log.e("isValid", "퀴즈가 3개 미만임")
            return false
        }
        if (!quizzes.all { it.quizOptions.size == 3 }) {
            Log.e("isValid", "퀴즈 중 보기 3개가 아닌 항목 있음")
            return false
        }
        if (!quizzes.all { quiz -> quiz.quizOptions.count { it.isCorrect } == 1 }) {
            Log.e("isValid", "퀴즈 중 정답이 하나가 아닌 항목 있음")
            return false
        }
        return true
    }

    fun fetchYoutubeMetaData(
        videoId: String,
        apiKey: String,
        onResult: (title: String, durationSeconds: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                YoutubeApiClient.youtubeService.getVideoInfo(
                    videoId = videoId,
                    apiKey = apiKey,
                    fields = "items(id,snippet(title),contentDetails(duration))"
                )
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val video = response.body()?.items?.firstOrNull()
                    if (video != null) {
                        val title = video.snippet.title
                        val durationIso = video.contentDetails.duration
                        val durationSeconds = YoutubeUtil.parseDuration(durationIso)
                        onResult(title, durationSeconds)
                    } else {
                        onError("영상 정보를 찾을 수 없습니다.")
                    }
                } else {
                    onError("API 응답 오류: ${response.code()}")
                }
            }.onFailure { throwable ->
                onError("네트워크 요청 실패: ${throwable.message}")
            }
        }
    }

    fun convertTempToFinalSubLectures() {
        subLectures.clear()
        subLectures.addAll(
            tempSubLectures.map {
                SubLecture(
                    subLectureTitle = it.title,
                    subLectureUrl = it.videoId,
                    subLectureLength = it.duration
                )
            }
        )
    }

    val tempQuizzes = mutableListOf<RegisterTempQuiz>()

    fun convertTempToFinalQuizzes() {
        quizzes.clear()
        quizzes.addAll(
            tempQuizzes.map { temp ->
                Quiz(
                    question = temp.question,
                    quizOptions = temp.options.mapIndexed { index, optionText ->
                        QuizOption(
                            quizOption = optionText,
                            isCorrect = index == temp.correctAnswerIndex
                        )
                    }
                )
            }
        )
    }

    /**
     * IPFS에 파일을 업로드합니다.
     * @param context 컨텍스트
     * @param apiKey Pinata API 키
     * @param onSuccess 성공 시 호출될 콜백
     * @param onError 실패 시 호출될 콜백
     */
    fun uploadFileToIpfs(
        context: android.content.Context,
        apiKey: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val fileUri = selectedLectureFileUri ?: run {
            Log.e("uploadFileToIpfs", "파일 URI가 null입니다")
            onError("업로드할 파일이 선택되지 않았습니다.")
            return
        }
        _ipfsUploadState.value = IpfsUploadState.Loading
        viewModelScope.launch {
            try {
                Log.d("uploadFileToIpfs", "파일 업로드 시작: $fileUri")
                val hash = IpfsUtils.uploadFileToIpfs(context, fileUri, apiKey)
                if (hash != null) {
                    Log.d("uploadFileToIpfs", "파일 업로드 성공: $hash")
                    ipfsHash = hash
                    _ipfsUploadState.value = IpfsUploadState.Success(hash)
                    onSuccess(hash)
                } else {
                    Log.e("uploadFileToIpfs", "파일 업로드 실패: hash가 null입니다")
                    _ipfsUploadState.value = IpfsUploadState.Error("파일 업로드 실패")
                    onError("파일 업로드 실패")
                }
            } catch (e: Exception) {
                Log.e("uploadFileToIpfs", "파일 업로드 중 예외 발생: ${e.message}", e)
                _ipfsUploadState.value = IpfsUploadState.Error(e.message ?: "알 수 없는 오류")
                onError(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun registerLecture(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val request = toRequest()
            // ✅ 요청 본문 로그 출력
            Log.d("registerLecture", "요청 데이터: $request")
            runCatching {
                registerService.registerLecture(toRequest())
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val msg = "등록 실패: ${response.code()} - ${response}" // 이런 거 사용자가 못 보게 다 지워야 합니다.
                    Log.e("registerLecture", msg)
                    onError(msg)
                }
            }.onFailure { throwable ->
                val msg = "네트워크 오류: ${throwable.message}"
                Log.e("registerLecture", msg, throwable)
                onError(msg)
            }
        }
    }

    fun searchUsers(keyword: String, page: Int = 1, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            runCatching {
                registerService.searchUserEmail(keyword, page)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    val newResults = data?.searchResults ?: emptyList()
                    val currentList = _searchResults.value.orEmpty()
                    _searchResults.value = if (page == 1) {
                        newResults
                    } else {
                        currentList + newResults
                    }
                    _totalResults.value = data?.totalResults ?: 0
                    _currentPage.value = data?.currentPage ?: 1
                    Log.d("searchUsers", "검색 성공: ${response.code()} ${response.body()}")
                } else {
                    Log.d("searchUsers", "검색 실패: ${response.code()}")
                }
            }.onFailure { throwable ->
                Log.d("searchUsers", "예외 발생: ${throwable.message}")
            }.also {
                onComplete?.invoke()
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun isEmailAlreadyRegistered(email: String): Boolean {
        return ratios.any { it.email == email }
    }
}

/**
 * IPFS 업로드 상태를 나타내는 sealed class
 */
sealed class IpfsUploadState {
    object Loading : IpfsUploadState()
    data class Success(val hash: String) : IpfsUploadState()
    data class Error(val message: String) : IpfsUploadState()
}