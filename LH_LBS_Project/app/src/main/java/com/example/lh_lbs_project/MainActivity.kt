package com.example.lh_lbs_project

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lh_lbs_project.ui.theme.LH_LBS_ProjectTheme
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private val client = OkHttpClient()
    private val gptUrl = "https://recommendroute-42j6jwyw4q-uc.a.run.app/recommendRoute"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LH_LBS_ProjectTheme {
                var result by remember { mutableStateOf("아직 요청 전입니다.") }
                val scope = rememberCoroutineScope()

                Column(modifier = Modifier.fillMaxSize()) {
                    // GPT 요청 결과 UI
                    GPTRequestScreen(
                        resultText = result,
                        onRequestClick = {
                            scope.launch(Dispatchers.IO) {
                                val response = sendGptRequest()
                                result = response
                            }
                        },
                        modifier = Modifier
                            .weight(1f) // 화면 절반 차지
                            .fillMaxWidth()
                    )

                    // 지도 마커 표시 UI
                    MapMarkerDisplayScreen(
                        location = LatLng(37.54160, 127.07356),
                        locationName = "TEST",
                        modifier = Modifier
                            .weight(1f) // 화면 절반 차지
                            .fillMaxWidth()
                    )
                }
            }
        }
    }

    private fun sendGptRequest(): String {
        val json = JSONObject().apply {
            put("user_preference", JSONObject().apply {
                put("quiet", true)
                put("safe", true)
                put("green", false)
                put("cafes", false)
            })
            put("routes", JSONArray().apply {
                put(
                    JSONObject(
                        mapOf(
                            "id" to 1,
                            "noise_level" to 65,
                            "cctv_count" to 2,
                            "green_area" to 0.1,
                            "cafe_count" to 5
                        )
                    )
                )
                put(
                    JSONObject(
                        mapOf(
                            "id" to 2,
                            "noise_level" to 40,
                            "cctv_count" to 6,
                            "green_area" to 0.05,
                            "cafe_count" to 1
                        )
                    )
                )
            })
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(gptUrl)  // 여기도 https://로 시작하는지 꼭 확인!
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GPT_ERROR", "HTTP 오류: ${response.code}, ${response.message}")
                    "오류 발생: ${response.code} - ${response.message}"
                } else {
                    response.body?.string() ?: "응답 없음"
                }
            }
        } catch (e: Exception) {
            Log.e("GPT_ERROR", "예외 발생: ${e.message}", e)
            "예외 발생: ${e.message}"
        }
    }
}
