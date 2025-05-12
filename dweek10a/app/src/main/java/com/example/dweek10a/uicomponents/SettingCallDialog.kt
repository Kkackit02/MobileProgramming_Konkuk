package com.example.dweek10a.uicomponents

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SettingsCallDialog(onDissmiss: () -> Unit, onGoToSettings: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDissmiss,
        title = { Text("권한 필요") },
        text = { Text("전화 기능을 사용하려면 앱설정에서 권한을 허용해주세요.") },
        confirmButton = {
            Button(onClick = onGoToSettings) {
                Text("설정으로 이동")
            }
        },
        dismissButton={
            Button(onClick = onDissmiss) {
                Text("취소")
            }
        }
    )
}