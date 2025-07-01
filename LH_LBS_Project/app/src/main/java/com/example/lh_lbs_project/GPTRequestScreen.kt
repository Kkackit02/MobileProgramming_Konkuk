package com.example.lh_lbs_project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lh_lbs_project.ui.theme.LH_LBS_ProjectTheme

@Composable
fun GPTRequestScreen(
    resultText: String = "아직 결과 없음",
    onRequestClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier // 기본값도 함께 설정해주면 편해요
) {
    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(text = "GPT 응답 결과:")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = resultText)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onRequestClick?.invoke() }) {
            Text("GPT에게 추천받기")
        }
    }
}
