package com.example.dweek10a.example01

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen01(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Button(onClick = {
//            val web = Uri.parse("https://www.naver.com")
//            val webIntent = Intent(Intent.ACTION_VIEW, web)
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.naver.com")
            }
            context.startActivity(webIntent)

        }, modifier = Modifier.width(200.dp)) {
            Text("네이버")
        }

        Button(onClick = {
            val location = Uri.parse("geo:37.543684,127.077130?z=16")
            val mapIntent = Intent(Intent.ACTION_VIEW, location)
            context.startActivity(mapIntent)
        }, modifier = Modifier.width(200.dp)) {
            Text("맵")
        }

        Button(onClick = {
            val message = Uri.parse("sms:010-1234-1234")
            val messageIntent = Intent(Intent.ACTION_SENDTO, message)
            messageIntent.putExtra("sms_body", "집에 가자....")
            context.startActivity(messageIntent)
        }, modifier = Modifier.width(200.dp)) {
            Text("문자보내기")
        }

        Button(onClick = {
            val number = Uri.parse("tel:010-1234-1234")
            val callIntent = Intent(Intent.ACTION_CALL, number)
            context.startActivity(callIntent)
        }, modifier = Modifier.width(200.dp)) {
            Text("전화걸기")
        }

//        Button(onClick = {
//            val number = Uri.parse("tel:010-1234-1234")
//            val callIntent = Intent(Intent.ACTION_CALL, number)
//            context.startActivity(callIntent)
//        }, modifier = Modifier.width(200.dp)) {
//            Text("전화걸기")
//        }
    }
}

@Preview
@Composable
private fun MainScreen01Preview() {
    MainScreen01()
}