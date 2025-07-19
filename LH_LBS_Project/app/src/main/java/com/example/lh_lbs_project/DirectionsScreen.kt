package com.example.lh_lbs_project

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
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
    val scope = rememberCoroutineScope()

    val start = LatLng(37.510257428761, 127.04391561527) // 선정릉역
    val goal = LatLng(37.513262, 127.100139)   // 잠실역

    LaunchedEffect(Unit) {
        cameraPositionState.position = CameraPosition(start, 11.0)

        scope.launch(Dispatchers.IO) {
            // --- 반복적 경로 탐색 알고리즘 시작 ---
            val maxAttempts = 3
            var currentAttempt = 0
            var finalSafeRouteFound = false

            // 1. 최초 대안 경로 요청
            var candidateRoutes = getDirections(start, goal) ?: emptyList()
            routes = candidateRoutes

            val parsedSites = getSeoulData()?.let { parseIncompleteSites(it) } ?: emptyList()
            incompleteSites = parsedSites

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
                    routes = listOf(bestRoute) // 안전 경로만 최종 표시
                    finalSafeRouteFound = true
                } else {
                    // 6. 안전하지 않다면, 새로운 우회 경로 생성
                    Log.d("ROUTE_SEARCH", "안전하지 않음. 첫번째 공사장을 기준으로 우회 경로 4개를 생성합니다.")
                    val firstSite = sitesOnBestRoute.first()
                    val detourDistance = 0.002 * (currentAttempt) // 시도할수록 더 멀리 우회
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
                    candidateRoutes = newDetourRoutes
                    routes = newDetourRoutes // 지도에는 현재 탐색중인 우회 경로들만 표시
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
                            routes = listOf(chosenRoute) // GPT가 선택한 경로만 최종 표시
                            Log.d("ROUTE_SEARCH", "GPT가 경로 ${chosenRouteId}를 선택했습니다. 지도에 표시합니다.")
                        } else {
                            Log.d("ROUTE_SEARCH", "GPT가 선택한 경로를 찾을 수 없습니다. 기존 최선 경로를 표시합니다.")
                            val bestRoute = candidateRoutes.minByOrNull { route ->
                                val routeIndex = candidateRoutes.indexOf(route)
                                val constructionCount = sitesOnRoutes[routeIndex]?.size ?: 0
                                val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sum()
                                constructionCount * 100000 + routeLength
                            }
                            if(bestRoute != null) routes = listOf(bestRoute)
                        }
                    }
                    "suggest_waypoints" -> {
                        val gptWaypoints = gptDecision.waypoints
                        if (gptWaypoints != null && gptWaypoints.isNotEmpty()) {
                            Log.d("ROUTE_SEARCH", "GPT로부터 ${gptWaypoints.size}개의 경유지 추천 받음.")
                            val gptRecommendedRoute = getDirectionsWithWaypoints(start, goal, gptWaypoints.first()) // GPT가 여러개 줘도 일단 첫번째만 사용
                            if (gptRecommendedRoute != null) {
                                routes = listOf(gptRecommendedRoute) // GPT 추천 경로만 최종 표시
                                Log.d("ROUTE_SEARCH", "GPT 추천 경로를 지도에 표시합니다.")
                            } else {
                                Log.d("ROUTE_SEARCH", "GPT 추천 경유지로 경로를 찾을 수 없습니다. 기존 최선 경로를 표시합니다.")
                                val bestRoute = candidateRoutes.minByOrNull { route ->
                                    val routeIndex = candidateRoutes.indexOf(route)
                                    val constructionCount = sitesOnRoutes[routeIndex]?.size ?: 0
                                    val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sum()
                                    constructionCount * 100000 + routeLength
                                }
                                if(bestRoute != null) routes = listOf(bestRoute)
                            }
                        } else {
                            Log.d("ROUTE_SEARCH", "GPT로부터 경유지 추천을 받지 못했습니다. 기존 최선 경로를 표시합니다.")
                            val bestRoute = candidateRoutes.minByOrNull { route ->
                                val routeIndex = candidateRoutes.indexOf(route)
                                val constructionCount = sitesOnRoutes[routeIndex]?.size ?: 0
                                val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sum()
                                constructionCount * 100000 + routeLength
                            }
                            if(bestRoute != null) routes = listOf(bestRoute)
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
                        if(bestRoute != null) routes = listOf(bestRoute)
                    }
                }
            }
            // --- 알고리즘 종료 ---
        }
    }

    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
    ) {
        routes.forEachIndexed { index, route ->
            val color = if (routes.size == 1) androidx.compose.ui.graphics.Color.Green
                        else androidx.compose.ui.graphics.Color.Gray
            val pathWidth = if (routes.size == 1) 5.dp else 3.dp
            val outline = if (routes.size == 1) 1.dp else 0.dp

            PathOverlay(
                coords = route,
                width = pathWidth,
                color = color,
                outlineWidth = outline
            )
        }

        incompleteSites.forEach { site ->
            Marker(
                state = MarkerState(position = site),
                captionText = "공사중",
                captionColor = androidx.compose.ui.graphics.Color.Magenta, // 겹치는 공사 현장은 자홍색
                iconTintColor = androidx.compose.ui.graphics.Color.Red // 아이콘 색상도 변경
            )
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
private fun isLocationOnPath(location: LatLng, path: List<LatLng>, threshold: Double = 50.0): Boolean {
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
