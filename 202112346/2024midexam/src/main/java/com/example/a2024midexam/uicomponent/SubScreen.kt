package com.example.a2024midexam.uicomponent

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.a2024midexam.model.ItemData

@Composable

// 변환용 스크린
fun SubScreen(
    UserName: String?,
    UserNumber: String?,
    UserClick : Int?
    ,modifier: Modifier = Modifier) {
    Column {

        Text("상세 정보")
        Text("이 름 :" + UserName)
        Text("전화번호 :" + UserNumber)
        Text("클릭횟수 :" + UserClick)

    }

}