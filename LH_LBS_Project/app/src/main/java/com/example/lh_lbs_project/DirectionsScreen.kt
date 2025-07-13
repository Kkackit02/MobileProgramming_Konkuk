
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

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun DirectionsScreen(modifier: Modifier = Modifier) {
    val cameraPositionState = rememberCameraPositionState()
    var route by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // 고정된 출발지/목적지
    val start = LatLng(37.5666102, 126.9783881) // 서울 시청
    val goal = LatLng(37.5511694, 126.9882266) // 남산 타워

    LaunchedEffect(Unit) {
        cameraPositionState.position = CameraPosition(start, 11.0)
        scope.launch(Dispatchers.IO) {
            Log.d("NAVER_KEY_CHECK", "ID: ${BuildConfig.NAVER_CLIENT_ID}, SECRET: ${BuildConfig.NAVER_CLIENT_SECRET}")

            val directions = getDirections(start, goal)
            if (directions != null) {
                route = directions
            }
        }
    }

    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
    ) {
        if (route.isNotEmpty()) {
            PathOverlay(
                coords = route,
                width = 4.dp,
                color = androidx.compose.ui.graphics.Color.Red
            )
        }
    }
}

private fun getDirections(start: LatLng, goal: LatLng): List<LatLng>? {
    val url = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving" +
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
