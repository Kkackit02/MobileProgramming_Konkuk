package com.example.dweek10a.example04
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.net.toUri
import com.example.dweek10a.example02.MainScreen02
import com.example.dweek10a.functions.makeCall
import com.example.dweek10a.functions.showCamera
import com.example.dweek10a.uicomponents.RationaleCallDialog
import com.example.dweek10a.uicomponents.SettingsCallDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen05(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as Activity

    var permissions = arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA)

    var showRationalDialog by remember { mutableStateOf(false) }
    var showSettingDialog by remember { mutableStateOf(false) }

    var showRationaleCheck by remember { mutableStateOf(false) }
    var showSettingsCheck by remember { mutableStateOf(false) }

    var requestMultiplePermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {
            when {
                it.all { map ->
                    map.value
                } -> Toast.makeText(context, "모든 권한이 승인됨", Toast.LENGTH_SHORT).show()

                shouldShowRequestPermissionRationale(
                    activity,
                    permissions[0]
                ) || shouldShowRequestPermissionRationale(
                    activity,
                    permissions[1]
                ) -> {
                    showRationalDialog = true
                }

                else -> {
                    showSettingDialog = true

                }
            }

        }


    fun requestPermissions() {
        when {
            permissions.all { p ->
                checkSelfPermission(context, p) ==
                        PackageManager.PERMISSION_GRANTED

            } -> {
                Toast.makeText(context, "모든 권한이 승인됨", Toast.LENGTH_SHORT).show()
            }

            showRationaleCheck -> {
                showRationalDialog = true
            }

            showSettingsCheck -> {
                showSettingDialog = true
            }

            else -> {
                requestMultiplePermissionLauncher.launch(permissions)
            }
        }
    }

    fun callPhonePermissionGranted() = checkSelfPermission(
        context,
        Manifest.permission.CALL_PHONE
    ) == PackageManager.PERMISSION_GRANTED

    fun cameraPermissionGranted() = checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(Unit) {
        requestPermissions()
    }


    if (showRationalDialog) {
        RationaleCallDialog(
            onDissmiss = { showRationalDialog = false },
            onConfirm = {
                showRationalDialog = false
                showRationaleCheck = false
                requestMultiplePermissionLauncher.launch(permissions)
            }
        )
    }

    if (showSettingDialog) {
        SettingsCallDialog(
            onDissmiss = { showSettingDialog = false },
            onGoToSettings = {
                showSettingDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                }
                context.startActivity(intent)
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Button(onClick = {
//            val web = Uri.parse("https://www.naver.com")
//            val webIntent = Intent(Intent.ACTION_VIEW, web)
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = "https://www.naver.com".toUri()
            }
            context.startActivity(webIntent)

        }, modifier = Modifier.width(200.dp)) {
            Text("네이버")
        }

        Button(onClick = {
            val location = "geo:37.543684,127.077130?z=16".toUri()
            val mapIntent = Intent(Intent.ACTION_VIEW, location)
            context.startActivity(mapIntent)
        }, modifier = Modifier.width(200.dp)) {
            Text("맵")
        }

        Button(onClick = {
            val message = "sms:010-1234-1234".toUri()
            val messageIntent = Intent(Intent.ACTION_SENDTO, message)
            messageIntent.putExtra("sms_body", "집에 가자....")
            context.startActivity(messageIntent)
        }, modifier = Modifier.width(200.dp)) {
            Text("문자보내기")
        }


        Button(onClick = {
            if (callPhonePermissionGranted())
                makeCall(context)
            else
                requestPermissions()
        }, modifier = Modifier.width(200.dp)) {
            Text("전화걸기")
        }
        Button(onClick = {
            if (cameraPermissionGranted())
                showCamera(context)
            else
                requestPermissions()
        }, modifier = Modifier.width(200.dp)) {
            Text("카메라")
        }
    }
}



@Preview
@Composable
private fun MainScreen01Preview() {
    MainScreen02()
}