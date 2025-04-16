package com.example.dweek07a.example01.uicomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onNavigateA: () -> Unit, onNavigateB: () -> Unit) {
    //A, B 로 이동하는 함수 각각 받음
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Home Screen",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold
        )

        //A이동 버튼
        Button(onClick = {
            onNavigateA()
        }) {
            Text(text = "Screen A")
        }

        //B이동 버튼
        Button(onClick = {
            onNavigateB()
        }) {
            Text(text = "Screen B")
        }
    }
}
