package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _text = MutableLiveData("러너스")
    val text: LiveData<String> = _text
}