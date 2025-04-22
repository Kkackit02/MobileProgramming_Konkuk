package com.example.a2024midexam.uicomponent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a2024midexam.R // 이미지 리소스 접근용 (R.drawable.사용 시 필요)



@Composable
fun Test(
    title: String = "정근녕의 Compose 레이아웃",
    onConfirmClick: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF9F9F9)),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        //horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("이름을 입력하세요") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        IconButton(onClick = { println("아이콘 버튼 클릭됨") }) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.Red
            )
        }

        Button(
            onClick = onConfirmClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("확인")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TestPreview() {
    Test()
}