package com.example.week13

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier


@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var msg by remember {
        mutableStateOf("")
    }

    MyBR(Intent.ACTION_POWER_CONNECTED) {
        msg = "전원연결"
    }
    MyBR(Intent.ACTION_POWER_DISCONNECTED) {
        msg = "전원연결해제"
    }

    Column {
        Text(text=msg)
    }
}