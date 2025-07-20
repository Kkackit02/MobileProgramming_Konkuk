package com.example.lh_lbs_project

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class GptRouteDecision(
    val decision: String,
    val chosenRouteId: String? = null,
    val waypoints: List<LatLng>? = null
)

data class RouteInfoForGpt(
    val id: String,
    val lengthKm: Double,
    val constructionSites: List<LatLng>
)

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun DirectionsScreen(modifier: Modifier = Modifier, sendGptRequest: suspend (LatLng, LatLng, List<RouteInfoForGpt>) -> GptRouteDecision?) {
    val cameraPositionState = rememberCameraPositionState()
    var routes by remember { mutableStateOf<List<List<LatLng>>>(emptyList()) }
    var incompleteSites by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var drainpipeSites by remember { mutableStateOf<List<LatLng>>(emptyList()) } // 하수관로 데이터 추가
    var selectedRoute by remember { mutableStateOf<List<LatLng>?>(null) }
    var initialNaverRoute by remember { mutableStateOf<List<LatLng>?>(null) }
    

    val scope = rememberCoroutineScope()
    val start = LatLng(37.56694,  127.05250)
    val goal = LatLng( 37.59056,  127.03639)

    LaunchedEffect(Unit) {
        cameraPositionState.position = CameraPosition(start, 11.0)

        scope.launch(Dispatchers.IO) {
            // --- 반복적 경로 탐색 알고리즘 시작 ---
            val maxAttempts = 3
            var currentAttempt = 0
            var finalSafeRouteFound = false

            // 1. 최초 대안 경로 요청
            var candidateRoutes = getDirections(start, goal) ?: emptyList()
            initialNaverRoute = candidateRoutes.firstOrNull() // 첫 번째 경로를 초기 네이버 경로로 저장
            Log.d("ROUTE_DISPLAY", "Initial candidate routes count: ${candidateRoutes.size}")
            routes = candidateRoutes

            val parsedSites = getSeoulData()?.let { parseIncompleteSites(it) } ?: emptyList()
            incompleteSites = parsedSites

            // 하수관로 데이터 호출 및 파싱 (임시 비활성화)
            /*
            val allPstnInfos = getDrainpipeData()
            val geocodedDrainpipeSites = mutableListOf<LatLng>()
            allPstnInfos.forEach { address ->
                val latLng = geocodeAddress(address)
                if (latLng != null) {
                    geocodedDrainpipeSites.add(latLng)
                }
            }
            drainpipeSites = geocodedDrainpipeSites
            Log.d("DRAINPIPE_MARKER", "Number of drainpipe sites geocoded: ${drainpipeSites.size}")
            drainpipeSites.forEachIndexed { index, latLng ->
                Log.d("DRAINPIPE_MARKER", "Drainpipe site $index: Lat=${latLng.latitude}, Lng=${latLng.longitude}")
            }
            */

            var sitesOnRoutes: Map<Int, List<LatLng>> = emptyMap() // 스코프 확장

            while (currentAttempt < maxAttempts && !finalSafeRouteFound && candidateRoutes.isNotEmpty()) {
                currentAttempt++
                Log.d("ROUTE_SEARCH", "--- 탐색 시도: $currentAttempt ---")

                sitesOnRoutes = findConstructionSitesOnRoutes(candidateRoutes, parsedSites) // 업데이트

                // 4. 최선 후보 선택 (공사장 수, 경로 길이 기준)
                val bestRoute = candidateRoutes.minByOrNull { route ->
                    val routeIndex = candidateRoutes.indexOf(route)
                    val constructionCount = sitesOnRoutes[routeIndex]?.size ?: 0
                    val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sum()
                    constructionCount * 100000 + routeLength // 공사장 1개를 100km 페널티로 계산
                }!!

                val bestRouteIndex = candidateRoutes.indexOf(bestRoute)
                val sitesOnBestRoute = sitesOnRoutes[bestRouteIndex] ?: emptyList()

                Log.d("ROUTE_SEARCH", "최선 후보: 경로 ${bestRouteIndex+1} (공사장 ${sitesOnBestRoute.size}개)")

                // 5. 최선 후보가 안전한지 검사
                if (sitesOnBestRoute.isEmpty()) {
                    Log.d("ROUTE_SEARCH", "🎉 안전 경로 발견! 탐색을 종료합니다.")
                    Log.d("ROUTE_DISPLAY", "Final safe route found. Routes count: 1")
                    selectedRoute = bestRoute // 안전 경로만 최종 표시
                    finalSafeRouteFound = true
                } else {
                    // 6. 안전하지 않다면, 새로운 우회 경로 생성
                    Log.d("ROUTE_SEARCH", "안전하지 않음. 첫번째 공사장을 기준으로 우회 경로 4개를 생성합니다.")
                    val firstSite = sitesOnBestRoute.first()
                    val detourDistance = 0.0009 * (currentAttempt) // 시도할수록 더 멀리 우회
                    val waypoints = listOf(
                        LatLng(firstSite.latitude, firstSite.longitude + detourDistance),
                        LatLng(firstSite.latitude, firstSite.longitude - detourDistance),
                        LatLng(firstSite.latitude + detourDistance, firstSite.longitude),
                        LatLng(firstSite.latitude - detourDistance, firstSite.longitude)
                    )

                    val newDetourRoutes = waypoints.mapNotNull {
                        getDirectionsWithWaypoints(start, goal, it)
                    }

                    // 7. 다음 탐색을 위해 후보 경로 교체
                    Log.d("ROUTE_DISPLAY", "New detour routes count: ${newDetourRoutes.size}")
                    candidateRoutes = newDetourRoutes
                    routes = (routes + newDetourRoutes).distinct()
                }
            }

            if (!finalSafeRouteFound) {
                Log.d("ROUTE_SEARCH", "최대 시도($maxAttempts) 후에도 안전 경로를 찾지 못했습니다. GPT에게 문의합니다.")

                // GPT에게 보낼 후보 경로 정보 구성
                val candidateRoutesInfoForGpt = candidateRoutes.mapIndexed { index, route ->
                    val constructionCount = sitesOnRoutes[index]?.size ?: 0
                    val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sum()
                    RouteInfoForGpt(
                        id = "route_${index + 1}", // 고유 ID 부여
                        lengthKm = routeLength / 1000.0,
                        constructionSites = sitesOnRoutes[index] ?: emptyList()
                    )
                }

                // GPT 호출
                val gptDecision = sendGptRequest(start, goal, candidateRoutesInfoForGpt)

                when (gptDecision?.decision) {
                    "choose_route" -> {
                        val chosenRouteId = gptDecision.chosenRouteId
                        val chosenRoute = candidateRoutesInfoForGpt.find { it.id == chosenRouteId }?.let { chosenInfo ->
                            candidateRoutes[candidateRoutesInfoForGpt.indexOf(chosenInfo)]
                        }
                        if (chosenRoute != null) {
                            selectedRoute = chosenRoute // GPT가 선택한 경로를 최종 경로로 설정
                            Log.d("ROUTE_DISPLAY", "GPT chosen route. Routes count: 1")
                        } else {
                            Log.d("ROUTE_SEARCH", "GPT가 선택한 경로를 찾을 수 없습니다. 기존 최선 경로를 표시합니다.")
                            val bestRoute = candidateRoutes.minByOrNull { route ->
                                val routeIndex = candidateRoutes.indexOf(route)
                                val constructionCount = sitesOnRoutes[routeIndex]?.size ?: 0
                                val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sum()
                                constructionCount * 100000 + routeLength
                            }
                            selectedRoute = bestRoute
                        }
                    }
                    "suggest_waypoints" -> {
                        val gptWaypoints = gptDecision.waypoints
                        if (gptWaypoints != null && gptWaypoints.isNotEmpty()) {
                            Log.d("ROUTE_SEARCH", "GPT로부터 ${gptWaypoints.size}개의 경유지 추천 받음.")
                            val gptRecommendedRoute = getDirectionsWithWaypoints(start, goal, gptWaypoints.first()) // GPT가 여러개 줘도 일단 첫번째만 사용
                            if (gptRecommendedRoute != null) {
                                selectedRoute = gptRecommendedRoute // GPT 추천 경로를 최종 경로로 설정
                                Log.d("ROUTE_DISPLAY", "GPT recommended route. Routes count: 1")
                            } else {
                                Log.d("ROUTE_SEARCH", "GPT 추천 경유지로 경로를 찾을 수 없습니다. 기존 최선 경로를 표시합니다.")
                                val bestRoute = candidateRoutes.minByOrNull { route ->
                                    val routeIndex = candidateRoutes.indexOf(route)
                                    val constructionCount = sitesOnRoutes[routeIndex]?.size ?: 0
                                    val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sum()
                                    constructionCount * 100000 + routeLength
                                }
                                selectedRoute = bestRoute
                            }
                        }
                    }
                    else -> {
                        Log.d("ROUTE_SEARCH", "GPT 응답이 유효하지 않거나 결정이 없습니다. 기존 최선 경로를 표시합니다.")
                        val bestRoute = candidateRoutes.minByOrNull { route ->
                            val routeIndex = candidateRoutes.indexOf(route)
                            val constructionCount = sitesOnRoutes[routeIndex]?.size ?: 0
                            val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sum()
                            constructionCount * 100000 + routeLength
                        }
                        if(bestRoute != null) selectedRoute = bestRoute
                    }
                }
            }
            // --- 알고리즘 종료 ---
        }

        // 모든 마커를 포함하도록 카메라 이동
        val allMarkers = incompleteSites
        if (allMarkers.isNotEmpty()) {
            val bounds = LatLngBounds.Builder().apply {
                allMarkers.forEach { include(it) }
            }.build()
            cameraPositionState.move(
                CameraUpdate.fitBounds(bounds, 100) // 100dp 패딩
            )
        }
        
    }

    var routeVisibility by remember { mutableStateOf<List<Boolean>>(emptyList()) }

    // routes 상태가 변경될 때 routeVisibility를 초기화합니다.
    LaunchedEffect(routes) {
        routeVisibility = List(routes.size) { true }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 경로 토글 버튼 (2줄 그리드)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // 그리드의 높이를 조절하여 2줄로 보이게 함
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(routes) { index, route ->
                val isVisible = routeVisibility.getOrElse(index) { true }
                val isSelected = route == selectedRoute
                val isInitial = route == initialNaverRoute

                val buttonText = when {
                    isSelected -> "Final"
                    isInitial -> "Initial"
                    else -> "Route ${index + 1}"
                }

                val borderColor = when {
                    isSelected -> Color.Green
                    isInitial -> Color.Red
                    else -> Color.Transparent
                }

                Button(
                    onClick = {
                        routeVisibility = routeVisibility.toMutableList().also {
                            if (index < it.size) {
                                it[index] = !it[index]
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isVisible) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.border(2.dp, borderColor)
                ) {
                    Text(buttonText)
                }
            }
        }

        
            NaverMap(
                modifier = Modifier.weight(1f),
                cameraPositionState = cameraPositionState,
            ) {
                val routeColors = listOf(
                    androidx.compose.ui.graphics.Color.Blue,
                    androidx.compose.ui.graphics.Color.Yellow,
                    androidx.compose.ui.graphics.Color.Cyan,
                    androidx.compose.ui.graphics.Color.Magenta,
                    androidx.compose.ui.graphics.Color.Black,
                    androidx.compose.ui.graphics.Color.DarkGray,
                    androidx.compose.ui.graphics.Color.LightGray
                )

                routes.forEachIndexed { index, route ->
                    if (routeVisibility.getOrElse(index) { true }) {
                        val isSelected = route == selectedRoute
                        val isInitialNaverRoute = route == initialNaverRoute

                        val color = when {
                            isSelected -> androidx.compose.ui.graphics.Color.Green
                            isInitialNaverRoute -> androidx.compose.ui.graphics.Color.Red
                            else -> routeColors[index % routeColors.size]
                        }
                        val pathWidth = when {
                            isSelected -> 8.dp
                            isInitialNaverRoute -> 6.dp // 초기 네이버 경로도 두껍게
                            else -> 3.dp
                        }
                        val outline = when {
                            isSelected -> 1.dp
                            isInitialNaverRoute -> 1.dp // 초기 네이버 경로도 아웃라인
                            else -> 0.dp
                        }

                        PathOverlay(
                            coords = route,
                            width = pathWidth,
                            color = color,
                            outlineWidth = outline
                        )
                    }
                }

                incompleteSites.forEach { site ->
                    Marker(
                        state = MarkerState(position = site),
                        captionText = "공사중",
                        captionColor = androidx.compose.ui.graphics.Color.Magenta, // 겹치는 공사 현장은 자홍색
                        iconTintColor = androidx.compose.ui.graphics.Color.Red // 아이콘 색상도 변경
                    )
                }

                // 하수관로 마커 표시 (임시 비활성화)
                /*
                drainpipeSites.forEach { site ->
                    Marker(
                        state = MarkerState(position = site),
                        captionText = "하수관로",
                        captionColor = androidx.compose.ui.graphics.Color.Blue,
                        iconTintColor = androidx.compose.ui.graphics.Color.Blue
                    )
                }
                */
            }
    }
}

// ... (이하 모든 헬퍼 함수들은 이전과 동일하게 유지) ...

// ✅ 네이버 경로 API 호출
private fun getDirections(start: LatLng, goal: LatLng): List<List<LatLng>>? {
    val url = "https://maps.apigw.ntruss.com/map-direction/v1/driving" +
            "?start=${start.longitude},${start.latitude}" +
            "&goal=${goal.longitude},${goal.latitude}" +
            "&option=traoptimal:tracomfort:traavoidtoll" // 대안 경로 옵션 추가

    Log.d("API_CALL", "Calling getDirections API. URL: $url")

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
                Log.e("API_CALL", "getDirections API Response (Error): ${response.body?.string()}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            Log.d("API_CALL", "getDirections API Response (Success): ${responseBody.take(500)}...") // Log first 500 chars
            val json = JSONObject(responseBody)
            val routeObject = json.getJSONObject("route")
            val allRoutes = mutableListOf<List<LatLng>>()

            routeObject.keys().forEach { key ->
                if (routeObject.has(key) && routeObject.get(key) is JSONArray) {
                    val routeArray = routeObject.getJSONArray(key)
                    if (routeArray.length() > 0) {
                        val pathArray = routeArray.getJSONObject(0).getJSONArray("path")
                        val path = (0 until pathArray.length()).map {
                            val point = pathArray.getJSONArray(it)
                            LatLng(point.getDouble(1), point.getDouble(0))
                        }
                        allRoutes.add(path)
                    }
                }
            }
            allRoutes
        }
    } catch (e: Exception) {
        Log.e("DIRECTIONS_ERROR", "예외 발생: ${e.message}", e)
        null
    }
}

// ✅ 경유지를 포함한 네이버 경로 API 호출
private fun getDirectionsWithWaypoints(start: LatLng, goal: LatLng, waypoint: LatLng): List<LatLng>? {
    val url = "https://maps.apigw.ntruss.com/map-direction/v1/driving" +
            "?start=${start.longitude},${start.latitude}" +
            "&goal=${goal.longitude},${goal.latitude}" +
            "&waypoints=${waypoint.longitude},${waypoint.latitude}"

    Log.d("API_CALL", "Calling getDirectionsWithWaypoints API. URL: $url")
    Log.d("API_CALL", "NAVER_CLIENT_ID: ${BuildConfig.NAVER_CLIENT_ID}")
    Log.d("API_CALL", "NAVER_CLIENT_SECRET: ${BuildConfig.NAVER_CLIENT_SECRET}")

    val request = Request.Builder()
        .url(url)
        .addHeader("X-NCP-APIGW-API-KEY-ID", BuildConfig.NAVER_CLIENT_ID)
        .addHeader("X-NCP-APIGW-API-KEY", BuildConfig.NAVER_CLIENT_SECRET)
        .build()

    val client = OkHttpClient()
    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("DIRECTIONS_ERROR", "[Waypoint] HTTP 오류: ${response.code}, ${response.message}")
                Log.e("API_CALL", "getDirectionsWithWaypoints API Response (Error): ${response.body?.string()}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            Log.d("API_CALL", "getDirectionsWithWaypoints API Response (Success): ${responseBody.take(500)}...") // Log first 500 chars
            val json = JSONObject(responseBody)

            if (json.has("route")) {
                val routeObject = json.getJSONObject("route")
                if (routeObject.has("traoptimal")) {
                    val routeArray = routeObject.getJSONArray("traoptimal")
                    if (routeArray.length() > 0) {
                        val pathArray = routeArray.getJSONObject(0).getJSONArray("path")
                        return (0 until pathArray.length()).map {
                            val point = pathArray.getJSONArray(it)
                            LatLng(point.getDouble(1), point.getDouble(0))
                        }
                    }
                }
            }
            null
        }
    } catch (e: Exception) {
        Log.e("DIRECTIONS_ERROR", "[Waypoint] 예외 발생: ${e.message}", e)
        null
    }
}

// ✅ 서울시 공공데이터 호출
private fun getSeoulData(): String? {
    val url = "http://openapi.seoul.go.kr:8088/${BuildConfig.API_CLIENT_KEY}/xml/ListOnePMISBizInfo/1/1000/"

    Log.d("API_CALL", "Calling getSeoulData API. URL: $url")

    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()
    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("SEOUL_API_ERROR", "HTTP 오류: ${response.code}, ${response.message}")
                Log.e("API_CALL", "getSeoulData API Response (Error): ${response.body?.string()}")
                return null
            }
            val responseBody = response.body?.string()
            Log.d("API_CALL", "getSeoulData API Response (Success): ${responseBody?.take(500)}...") // Log first 500 chars
            responseBody
        }
    } catch (e: Exception) {
        Log.e("SEOUL_API_ERROR", "예외 발생: ${e.message}", e)
        null
    }
}

// ✅ 서울시 하수관로 데이터 호출
private fun getDrainpipeData(): List<String> { // 반환 타입을 List<String>으로 변경
    val allPstnInfos = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyyMMddHH", Locale.getDefault())

    val currentTime = calendar.time
    val currentFormattedTime = dateFormat.format(currentTime)

    calendar.add(Calendar.HOUR_OF_DAY, -1)
    val oneHourAgoTime = calendar.time
    val oneHourAgoFormattedTime = dateFormat.format(oneHourAgoTime)

    val startTime = oneHourAgoFormattedTime
    val endTime = currentFormattedTime

    for (i in 1..25) {
        val seCd = String.format("%02d", i)
        val url = "http://openAPI.seoul.go.kr:8088/${BuildConfig.API_CLIENT_KEY}/xml/DrainpipeMonitoringInfo/1/1000/$seCd/$startTime/$endTime"

        Log.d("API_CALL", "Calling getDrainpipeData API for seCd=$seCd. URL: $url")

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("DRAINPIPE_API_ERROR", "HTTP 오류 (seCd=$seCd): ${response.code}, ${response.message}")
                    Log.e("API_CALL", "getDrainpipeData API Response (Error, seCd=$seCd): ${response.body?.string()}")
                } else {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        Log.d("API_CALL", "getDrainpipeData API Response (Success, seCd=$seCd): ${responseBody.take(500)}...")
                        val parsedAddresses = parseDrainpipeSites(responseBody)
                        Log.d("DRAINPIPE_DEBUG", "seCd=$seCd parsed ${parsedAddresses.size} addresses.") // 추가된 로그
                        allPstnInfos.addAll(parsedAddresses)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DRAINPIPE_API_ERROR", "예외 발생 (seCd=$seCd): ${e.message}", e)
        }
        Log.d("DRAINPIPE_DEBUG", "Total addresses collected so far: ${allPstnInfos.size}") // 추가된 로그
    }
    return allPstnInfos
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

// ✅ XML에서 하수관로 데이터 파싱 (LOC_X, LOC_Y 사용)
fun parseDrainpipeSites(xml: String): List<String> { // 반환 타입을 List<String>으로 변경
    val list = mutableListOf<String>()
    val factory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    parser.setInput(xml.reader())

    var eventType = parser.eventType
    var pstnInfo: String? = null // PSTN_INFO를 저장할 변수
    var tagName: String? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> tagName = parser.name
            XmlPullParser.TEXT -> {
                when (tagName) {
                    "PSTN_INFO" -> pstnInfo = parser.text.trim() // PSTN_INFO 태그의 텍스트를 저장
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "row") { // Assuming each record is within a <row> tag
                    if (pstnInfo != null && pstnInfo.isNotEmpty()) {
                        list += pstnInfo
                    }
                    pstnInfo = null // 초기화
                }
                tagName = null
            }
        }
        eventType = parser.next()
    }
    return list
}

// 두 지점 간의 거리를 미터로 계산 (Haversine 공식)
private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371e3 // 지구 반지름 (미터)
    val phi1 = Math.toRadians(lat1)
    val phi2 = Math.toRadians(lat2)
    val deltaPhi = Math.toRadians(lat2 - lat1)
    val deltaLambda = Math.toRadians(lon2 - lon1)

    val a = sin(deltaPhi / 2).pow(2) +
            cos(phi1) * cos(phi2) *
            sin(deltaLambda / 2).pow(2)
    val c = 2 * asin(sqrt(a))

    return r * c
}
private fun geocodeAddress(address: String): LatLng? {
    val encodedAddress = URLEncoder.encode(address, "UTF-8")
    val url = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=$encodedAddress"

    Log.d("API_CALL", "Calling geocodeAddress API. URL: $url")

    val request = Request.Builder()
        .url(url)
        .addHeader("x-ncp-apigw-api-key-id", BuildConfig.NAVER_CLIENT_ID)
        .addHeader("x-ncp-apigw-api-key", BuildConfig.NAVER_CLIENT_SECRET)
        .addHeader("Accept", "application/json")
        .build()

    val client = OkHttpClient()
    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("GEOCODE_ERROR", "HTTP 오류: ${response.code}, ${response.message}")
                Log.e("API_CALL", "geocodeAddress API Response (Error): ${response.body?.string()}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            Log.d("API_CALL", "geocodeAddress API Response (Success): ${responseBody.take(500)}...") // Log first 500 chars
            val json = JSONObject(responseBody)

            val addressesArray = json.getJSONArray("addresses")
            if (addressesArray.length() > 0) {
                val firstAddress = addressesArray.getJSONObject(0)
                val x = firstAddress.getDouble("x") // longitude
                val y = firstAddress.getDouble("y") // latitude
                LatLng(y, x) // LatLng(latitude, longitude)
            } else {
                null
            }
        }
    } catch (e: Exception) {
        Log.e("GEOCODE_ERROR", "예외 발생: ${e.message}", e)
        return null
    }
}


// 한 점이 경로 위에 있는지 확인 (임계값 이내)
private fun isLocationOnPath(location: LatLng, path: List<LatLng>, threshold: Double = 150.0): Boolean {
    for (i in 0 until path.size - 1) {
        val start = path[i]
        val end = path[i + 1]

        if (location.latitude < start.latitude.coerceAtMost(end.latitude) - 0.001 ||
            location.latitude > start.latitude.coerceAtLeast(end.latitude) + 0.001 ||
            location.longitude < start.longitude.coerceAtMost(end.longitude) - 0.001 ||
            location.longitude > start.longitude.coerceAtLeast(end.longitude) + 0.001) {
            continue
        }

        val dist = haversine(location.latitude, location.longitude, start.latitude, start.longitude)
        val dist2 = haversine(location.latitude, location.longitude, end.latitude, end.longitude)
        val segmentDist = haversine(start.latitude, start.longitude, end.latitude, end.longitude)

        if (dist < threshold || dist2 < threshold) return true

        val s = (dist.pow(2) - dist2.pow(2) + segmentDist.pow(2)) / (2 * segmentDist)
        if (s.isFinite() && s in 0.0..segmentDist) {
            val hSquared = dist.pow(2) - s.pow(2)
            if (hSquared >= 0) {
                val h = sqrt(hSquared)
                if (h < threshold) return true
            }
        }
    }
    return false
}

// 경로 목록과 공사 현장 목록을 비교하여, 각 경로별로 포함된 공사 현장을 반환
private fun findConstructionSitesOnRoutes(routes: List<List<LatLng>>, sites: List<LatLng>): Map<Int, List<LatLng>> {
    val sitesOnRoutes = mutableMapOf<Int, MutableList<LatLng>>()
    routes.forEachIndexed { index, route ->
        sites.forEach { site ->
            if (isLocationOnPath(site, route)) {
                sitesOnRoutes.getOrPut(index) { mutableListOf() }.add(site)
            }
        }
    }
    return sitesOnRoutes
}
