package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DateStatsViewModel : ViewModel() {
    private var lectureName: String? = null

    private val _dateSelection = MutableLiveData<String>()
    val dateSelection: LiveData<String> get() = _dateSelection

    private val _studentCount = MutableLiveData<String>()
    val studentCount: LiveData<String> get() = _studentCount

    private val _income = MutableLiveData<String>()
    val income: LiveData<String> get() = _income

    // lectureName을 전달받아 초기화하는 메서드
    fun initData(lectureName: String?) {
        // 이미 초기화되었다면 무시 (여러번 호출 방지)
        if (this.lectureName != null) return
        this.lectureName = lectureName

        // Dummy 데이터; 실제 API 호출 결과로 업데이트하세요.
        _dateSelection.value = "2024  3월  20일"
        _studentCount.value = "등록한 수강자 수: 10"
        _income.value = "수익: 100,000"
    }
}
