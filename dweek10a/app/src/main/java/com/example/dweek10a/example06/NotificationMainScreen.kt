package com.example.dweek10a.example06

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


enum class PickerMode { Date, Time }

fun formatDateTime(
    epochMillis: Long,
    hour: Int,
    minute: Int
): String {
    val dateTime = Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .withHour(hour)
        .withMinute(minute)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return dateTime.format(formatter)
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotificationMainScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // 2초 후에 수행하게 하려고 코루틴 scope 선언

    val notifyPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    // 권한 저장

    LaunchedEffect(Unit) {
        if (!notifyPermission.status.isGranted) {
            notifyPermission.launchPermissionRequest()
        }
        NotificationHelper.initChannel(context)
        //채널 정보 등록
    }

    var message by remember { mutableStateOf("") }
    var pickerMode by remember { mutableStateOf<PickerMode?>(null) }
    var pickedDateMillis by remember { mutableStateOf<Long?>(null) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(
        initialHour = LocalDateTime.now().hour,
        initialMinute = LocalDateTime.now().minute
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { pickerMode = PickerMode.Date }) {
            Text("알림 설정")
        }
    }

    pickerMode?.let { mode ->
        when (mode) {
            PickerMode.Date -> {
                DatePickerDialog(
                    onDismissRequest = { pickerMode = null },
                    confirmButton = {
                        TextButton(onClick = {
                            pickedDateMillis = datePickerState.selectedDateMillis
                                ?: Instant.now().toEpochMilli()
                            pickerMode = PickerMode.Time
                        }) { Text("확인") }
                    },
                    dismissButton = {
                        TextButton(onClick = { pickerMode = null }) { Text("취소") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            PickerMode.Time -> {
                TimePickerDialog(
                    onDismiss = { pickerMode = null },
                    onConfirm = {
                        val dateMillis = pickedDateMillis
                            ?: Instant.now().toEpochMilli()
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute

                        val scheduledText = formatDateTime(dateMillis, hour, minute)
                        val fullMsg = "${message} :  $scheduledText"

                        pickerMode = null

                        scope.launch {
                            delay(2000)
                            NotificationHelper.notify(context, msg = fullMsg)
                        }
                    }
                ) {
                    TimeInput(state = timePickerState)
                    //  TimePicker(state = timePickerState)
                }
            }
        }
    }
}


