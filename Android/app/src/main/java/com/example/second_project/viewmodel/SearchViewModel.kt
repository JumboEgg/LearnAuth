package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel :ViewModel(){
    private val _text = MutableLiveData("검색화면입니당")
    val text: LiveData<String> = _text
}