package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.ReportItem

class DeclarationViewModel : ViewModel() {

    private val _reportList = MutableLiveData<List<ReportItem>>()
    val reportList: LiveData<List<ReportItem>> = _reportList

    // 샘플 데이터
    init {
        _reportList.value = listOf(
            ReportItem(
                title = "눈 감고 차이는 법",
                type = "강의자",
                content = "강의자가 이상합니다. 강의자를 고소하겠습니다. 참 고소하군요.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "실험중입니다." +
                        "악" +
                        "진짜" +
                        "\n" +
                        "\n" +
                        "어렵네요"
            ),
            ReportItem(
                title = "눈 감고 차이는 법",
                type = "강의 자료",
                content = "강의자료가 이상합니다. 고소하겠습니다."
            ),
            ReportItem(
                title = "눈 감고 차이는 법",
                type = "강의 영상",
                content = "영상이 이상해요. 내용이 적절하지 않습니다."
            )
        )
    }
}
