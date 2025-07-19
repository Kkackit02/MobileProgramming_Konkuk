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
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun DirectionsScreen(modifier: Modifier = Modifier) {
    val cameraPositionState = rememberCameraPositionState()
    var routes by remember { mutableStateOf<List<List<LatLng>>>(emptyList()) }
    var incompleteSites by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // 출발지/도착지 설정
    val start = LatLng(37.4979502, 127.027637) // 강남역
    val goal = LatLng(37.513262, 127.100139)   // 잠실역

    LaunchedEffect(Unit) {
        cameraPositionState.position = CameraPosition(start, 11.0)

        scope.launch(Dispatchers.IO) {
            // ✅ 1. 기본 대안 경로 요청
            val baseRoutes = getDirections(start, goal) ?: emptyList()
            routes = baseRoutes

            // ✅ 2. 공공데이터 요청 및 파싱
            val seoulRaw = getSeoulData()
            if (seoulRaw != null) {
                val parsedSites = parseIncompleteSites(seoulRaw)
                incompleteSites = parsedSites
                Log.d("SEOUL_INCOMPLETE", "공사현장 ${parsedSites.size}건 표시됨")

                // ✅ 3. 최단 경로(첫 번째)와 겹치는 공사 현장 찾기
                val optimalRoute = baseRoutes.firstOrNull()
                if (optimalRoute != null) {
                    val sitesOnOptimalRoute = findConstructionSitesOnRoutes(listOf(optimalRoute), parsedSites).getOrDefault(0, emptyList())

                    if (sitesOnOptimalRoute.isNotEmpty()) {
                        Log.d("ROUTE_ANALYSIS", "최단 경로에서 ${sitesOnOptimalRoute.size}개의 공사 현장 발견. 우회 경로 탐색 시작.")
                        val firstSite = sitesOnOptimalRoute.first() // 편의상 첫번째 공사 현장만 사용

                        // ✅ 4. 가상 경유지 4개 생성 (동서남북 200m)
                        val detourDistance = 0.002
                        val waypoints = listOf(
                            LatLng(firstSite.latitude, firstSite.longitude + detourDistance), // 동
                            LatLng(firstSite.latitude, firstSite.longitude - detourDistance), // 서
                            LatLng(firstSite.latitude + detourDistance, firstSite.longitude), // 남
                            LatLng(firstSite.latitude - detourDistance, firstSite.longitude)  // 북
                        )

                        // ✅ 5. 각 경유지를 사용해 새로운 경로 4개 요청
                        val detourRoutes = waypoints.mapNotNull {
                            getDirectionsWithWaypoints(start, goal, it)
                        }

                        // ✅ 6. 기존 경로와 새로운 우회 경로를 합쳐서 상태 업데이트
                        routes = baseRoutes + detourRoutes
                        Log.d("ROUTE_ANALYSIS", "총 ${routes.size}개의 경로를 지도에 표시합니다.")
                    }
                }
            }
        }
    }

    // ✅ 3. 지도 UI
    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
    ) {
        // 경로 표시
        routes.forEachIndexed { index, route ->
            val color = when (index) {
                0 -> androidx.compose.ui.graphics.Color.Blue       // 1. 최적 경로
                1 -> androidx.compose.ui.graphics.Color.Green     // 2. 편안한 경로
                2 -> androidx.compose.ui.graphics.Color.Gray      // 3. 무료 경로
                3 -> androidx.compose.ui.graphics.Color.Cyan      // 4. 우회 경로 1 (동)
                4 -> androidx.compose.ui.graphics.Color.Magenta   // 5. 우회 경로 2 (서)
                5 -> androidx.compose.ui.graphics.Color.Yellow    // 6. 우회 경로 3 (남)
                6 -> androidx.compose.ui.graphics.Color.Black     // 7. 우회 경로 4 (북)
                else -> androidx.compose.ui.graphics.Color.DarkGray
            }
            PathOverlay(
                coords = route,
                width = if(index < 3) 5.dp else 3.dp, // 기본 경로는 굵게
                color = color,
                outlineWidth = 1.dp
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
private fun getDirections(start: LatLng, goal: LatLng): List<List<LatLng>>? {
    val url = "https://maps.apigw.ntruss.com/map-direction/v1/driving" +
            "?start=${start.longitude},${start.latitude}" +
            "&goal=${goal.longitude},${goal.latitude}" +
            "&option=traoptimal:tracomfort:traavoidtoll" // 대안 경로 옵션 추가

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
            Log.d("DIRECTIONS_RESPONSE", responseBody) // 전체 응답 로그 출력
            val json = JSONObject(responseBody)
            val routeObject = json.getJSONObject("route")
            val allRoutes = mutableListOf<List<LatLng>>()

            // 모든 경로 옵션 키를 순회 (traoptimal, tracomfort, traavoidtoll 등)
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
                return null
            }
            val responseBody = response.body?.string() ?: return null
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

// 한 점이 경로 위에 있는지 확인 (임계값 이내)
private fun isLocationOnPath(location: LatLng, path: List<LatLng>, threshold: Double = 20.0): Boolean {
    for (i in 0 until path.size - 1) {
        val start = path[i]
        val end = path[i + 1]

        // 간단한 경계 상자 확인으로 성능 최적화
        if (location.latitude < start.latitude.coerceAtMost(end.latitude) - 0.001 ||
            location.latitude > start.latitude.coerceAtLeast(end.latitude) + 0.001 ||
            location.longitude < start.longitude.coerceAtMost(end.longitude) - 0.001 ||
            location.longitude > start.longitude.coerceAtLeast(end.longitude) + 0.001) {
            continue
        }

        val dist = haversine(location.latitude, location.longitude, start.latitude, start.longitude)
        val dist2 = haversine(location.latitude, location.longitude, end.latitude, end.longitude)
        val segmentDist = haversine(start.latitude, start.longitude, end.latitude, end.longitude)

        // 점이 선분 양 끝점 중 하나와 매우 가까운 경우
        if (dist < threshold || dist2 < threshold) return true

        // 점과 선분 사이의 최단 거리 계산 (근사치)
        val s = (dist.pow(2) - dist2.pow(2) + segmentDist.pow(2)) / (2 * segmentDist)
        if (s in 0.0..segmentDist) {
            val h = sqrt(dist.pow(2) - s.pow(2))
            if (h < threshold) return true
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