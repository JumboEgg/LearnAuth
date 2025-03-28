package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyStatsViewModel : ViewModel() {
    private var lectureName: String? = null

    private val _myShareRate = MutableLiveData<String>()
    val myShareRate: LiveData<String> get() = _myShareRate

    private val _myTotalIncome = MutableLiveData<String>()
    val myTotalIncome: LiveData<String> get() = _myTotalIncome

    fun initData(lectureName: String?) {
        if (this.lectureName != null) return
        this.lectureName = lectureName

        _myShareRate.value = "10%"
        _myTotalIncome.value = "100,000"
    }
}
