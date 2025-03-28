package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ParticipantsStatsViewModel : ViewModel() {
    private var lectureName: String? = null

    private val _participantRate1 = MutableLiveData<String>()
    val participantRate1: LiveData<String> get() = _participantRate1

    private val _participantRate2 = MutableLiveData<String>()
    val participantRate2: LiveData<String> get() = _participantRate2

    fun initData(lectureName: String?) {
        if (this.lectureName != null) return
        this.lectureName = lectureName

        _participantRate1.value = "3.6%"
        _participantRate2.value = "64%"
    }
}
