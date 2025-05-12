package com.example.dweek10a.uicomponents

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun RationaleCallDialog(
    onDissmiss:()->Unit,
    onConfirm:()->Unit,
    ) {
    AlertDialog(
        onDismissRequest = onDissmiss,
        title = { Text("권한 확인요청") },
        text = { Text("전화 기능을 사용하려면 CAL_PHONE 권한이 필요합니다.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("권한승인")
            }
        },
        dismissButton={
            Button(onClick = onDissmiss) {
                Text("취소")
            }
        }
    )
}