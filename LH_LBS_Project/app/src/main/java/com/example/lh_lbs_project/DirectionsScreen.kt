package com.example.lh_lbs_project

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun DirectionsScreen(modifier: Modifier = Modifier) {
    val cameraPositionState = rememberCameraPositionState()
    var route by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var incompleteSites by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // 출발지/도착지 설정
    val start = LatLng(37.5666102, 126.9783881) // 서울시청
    val goal = LatLng(37.5511694, 126.9882266) // 남산타워

    LaunchedEffect(Unit) {
        cameraPositionState.position = CameraPosition(start, 11.0)

        scope.launch(Dispatchers.IO) {
            Log.d("NAVER_KEY_CHECK", "ID: ${BuildConfig.NAVER_CLIENT_ID}, SECRET: ${BuildConfig.NAVER_CLIENT_SECRET}")

            // ✅ 1. 경로 요청
            val directions = getDirections(start, goal)
            if (directions != null) route = directions

            // ✅ 2. 공공데이터 요청 및 파싱
            val seoulRaw = getSeoulData()
            if (seoulRaw != null) {
                val parsed = parseIncompleteSites(seoulRaw)
                incompleteSites = parsed
                Log.d("SEOUL_INCOMPLETE", "공사현장 ${parsed.size}건 표시됨")
            }
        }
    }

    // ✅ 3. 지도 UI
    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
    ) {
        // 최단 경로 경로 표시
        if (route.isNotEmpty()) {
            PathOverlay(
                coords = route,
                width = 4.dp,
                color = androidx.compose.ui.graphics.Color.Red
            )
        }

        // 공사 마커 표시
        incompleteSites.forEach { site ->
            Marker(
                state = MarkerState(position = site),
                captionText = "공사중",
                captionColor = androidx.compose.ui.graphics.Color.Blue,
                iconTintColor = androidx.compose.ui.graphics.Color.Yellow
            )
        }
    }
}

// ✅ 네이버 경로 API 호출
private fun getDirections(start: LatLng, goal: LatLng): List<LatLng>? {
    val url = "https://maps.apigw.ntruss.com/map-direction/v1/driving" +
            "?start=${start.longitude},${start.latitude}" +
            "&goal=${goal.longitude},${goal.latitude}"

    val request = Request.Builder()
        .url(url)
        .addHeader("X-NCP-APIGW-API-KEY-ID", BuildConfig.NAVER_CLIENT_ID)
        .addHeader("X-NCP-APIGW-API-KEY", BuildConfig.NAVER_CLIENT_SECRET)
        .build()

    val client = OkHttpClient()
    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("DIRECTIONS_ERROR", "HTTP 오류: ${response.code}, ${response.message}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            val json = JSONObject(responseBody)
            val route = json.getJSONObject("route").getJSONArray("traoptimal")
            val path = route.getJSONObject(0).getJSONArray("path")

            (0 until path.length()).map {
                val point = path.getJSONArray(it)
                LatLng(point.getDouble(1), point.getDouble(0))
            }
        }
    } catch (e: Exception) {
        Log.e("DIRECTIONS_ERROR", "예외 발생: ${e.message}", e)
        null
    }
}

// ✅ 서울시 공공데이터 호출
private fun getSeoulData(): String? {
    val url = "http://openapi.seoul.go.kr:8088/${BuildConfig.API_CLIENT_KEY}/xml/ListOnePMISBizInfo/1/1000/" // 원하는 범위로 수정 가능

    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()
    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("SEOUL_API_ERROR", "HTTP 오류: ${response.code}, ${response.message}")
                return null
            }
            response.body?.string()
        }
    } catch (e: Exception) {
        Log.e("SEOUL_API_ERROR", "예외 발생: ${e.message}", e)
        null
    }
}

// ✅ XML에서 공사 미완료(LAT, LOT)만 파싱
fun parseIncompleteSites(xml: String): List<LatLng> {
    val list = mutableListOf<LatLng>()
    val factory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    parser.setInput(xml.reader())

    var eventType = parser.eventType
    var lat: Double? = null
    var lot: Double? = null
    var isIncomplete = false
    var tagName: String? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> tagName = parser.name
            XmlPullParser.TEXT -> {
                when (tagName) {
                    "CMCN_YN1" -> isIncomplete = parser.text.trim() == "0"
                    "LAT" -> lat = parser.text.toDoubleOrNull()
                    "LOT" -> lot = parser.text.toDoubleOrNull()
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "row") {
                    if (isIncomplete && lat != null && lot != null) {
                        list += LatLng(lat, lot)
                    }
                    isIncomplete = false
                    lat = null
                    lot = null
                }
                tagName = null
            }
        }
        eventType = parser.next()
    }
    return list
}
