package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel :ViewModel(){
    private val _text  =MutableLiveData("마이페이지입니당")
    val text :LiveData<String> = _text
}