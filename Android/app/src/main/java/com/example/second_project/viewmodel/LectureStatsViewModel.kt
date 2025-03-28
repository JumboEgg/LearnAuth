package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LectureStatsViewModel : ViewModel() {
    private var lectureName: String? = null

    private val _lectureNameLive = MutableLiveData<String>()
    val lectureNameLive: LiveData<String> get() = _lectureNameLive

    private val _registerDate = MutableLiveData<String>()
    val registerDate: LiveData<String> get() = _registerDate

    private val _lecturer = MutableLiveData<String>()
    val lecturer: LiveData<String> get() = _lecturer

    private val _totalStudentCount = MutableLiveData<String>()
    val totalStudentCount: LiveData<String> get() = _totalStudentCount

    private val _totalIncome = MutableLiveData<String>()
    val totalIncome: LiveData<String> get() = _totalIncome

    fun initData(lectureName: String?) {
        if (this.lectureName != null) return
        this.lectureName = lectureName

        _lectureNameLive.value = lectureName ?: "차이는 법"
        _registerDate.value = "2022.01.01"
        _lecturer.value = "김차인"
        _totalStudentCount.value = "10"
        _totalIncome.value = "999,999,999,999"
    }
}
