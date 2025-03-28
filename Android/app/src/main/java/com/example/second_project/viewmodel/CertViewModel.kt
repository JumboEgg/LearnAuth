package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CertViewModel : ViewModel(){
    private val _text = MutableLiveData("수료증화면입니당")
    val text : LiveData<String> = _text
}