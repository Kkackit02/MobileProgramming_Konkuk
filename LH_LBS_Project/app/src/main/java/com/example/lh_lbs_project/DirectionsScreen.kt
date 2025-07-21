package com.example.lh_lbs_project

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.geometry.LatLngBounds
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
import kotlin.random.Random

// Data class for drainpipe information including water level
data class DrainpipeInfo(
    val address: String,
    val waterLevel: Double,
    var location: LatLng? = null
)

data class GptRouteDecision(
    val decision: String,
    val chosenRouteId: String? = null,
    val waypoints: List<LatLng>? = null
)

// Modified to handle generic hazards
data class RouteInfoForGpt(
    val id: String,
    val lengthKm: Double,
    val hazardSites: List<LatLng> // Generic term for all kinds of hazards
)

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun DirectionsScreen(modifier: Modifier = Modifier, sendGptRequest: suspend (LatLng, LatLng, List<RouteInfoForGpt>) -> GptRouteDecision?) {
    val cameraPositionState = rememberCameraPositionState()
    var routes by remember { mutableStateOf<List<List<LatLng>>>(emptyList()) }
    var incompleteSites by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var allDrainpipes by remember { mutableStateOf<List<DrainpipeInfo>>(emptyList()) } // Store all drainpipes
    var selectedRoute by remember { mutableStateOf<List<LatLng>?>(null) }
    var initialNaverRoute by remember { mutableStateOf<List<LatLng>?>(null) }
    var isLoading by remember { mutableStateOf(true) } // Loading state

    val scope = rememberCoroutineScope()
    val start = LatLng(37.56694,  127.05250)
    val goal = LatLng( 37.59056,  127.03639)

    LaunchedEffect(Unit) {
        isLoading = true
        cameraPositionState.position = CameraPosition(start, 11.0)

        scope.launch(Dispatchers.IO) {
            // --- Data Loading Phase ---
            Log.d("LOADING", "Starting data loading...")
            val parsedConstructionSites = getSeoulData()?.let { parseIncompleteSites(it) } ?: emptyList()
            incompleteSites = parsedConstructionSites

            val allDrainpipeInfo = getDrainpipeData()
            allDrainpipeInfo.forEach { info ->
                val latLng = geocodeAddress(info.address)
                if (latLng != null) {
                    info.location = latLng
                }
            }
            allDrainpipes = allDrainpipeInfo.filter { it.location != null } // Store all geocoded drainpipes

            // Filter for hazardous drainpipes (water level >= 20%)
            val hazardousDrainpipeSites = allDrainpipes
                .filter { it.waterLevel >= 0.20 }
                .map { it.location!! }

            Log.d("LOADING", "Data loading finished.")
            Log.d("HAZARD_DEBUG", "Construction sites: ${parsedConstructionSites.size}, All drainpipes: ${allDrainpipes.size}, Hazardous drainpipes: ${hazardousDrainpipeSites.size}")

            // --- Route Search Phase (starts after loading) ---
            var candidateRoutes = getDirections(start, goal) ?: emptyList()
            initialNaverRoute = candidateRoutes.firstOrNull()
            routes = candidateRoutes

            val maxAttempts = 10
            var currentAttempt = 0
            var finalSafeRouteFound = false
            var allSitesOnRoutes: Map<Int, List<LatLng>> = emptyMap() // Declare here

            while (currentAttempt < maxAttempts && !finalSafeRouteFound && candidateRoutes.isNotEmpty()) {
                currentAttempt++
                Log.d("ROUTE_SEARCH", "--- Search Attempt: $currentAttempt ---")

                val constructionSitesOnRoutes = findHazardSitesOnRoutes(candidateRoutes, parsedConstructionSites, threshold = 150.0)
                val drainpipeSitesOnRoutes = findHazardSitesOnRoutes(candidateRoutes, hazardousDrainpipeSites, threshold = 10.0)

                allSitesOnRoutes = (constructionSitesOnRoutes.keys + drainpipeSitesOnRoutes.keys).associateWith {
                    (constructionSitesOnRoutes[it] ?: emptyList()) + (drainpipeSitesOnRoutes[it] ?: emptyList())
                }

                Log.d("ROUTE_SEARCH", "Found ${allSitesOnRoutes.values.sumBy { it.size }} total hazards on routes.")

                val bestRoute = candidateRoutes.minByOrNull { route ->
                    val routeIndex = candidateRoutes.indexOf(route)
                    val hazardCount = allSitesOnRoutes[routeIndex]?.size ?: 0
                    val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sum()
                    hazardCount * 100000 + routeLength
                }!!

                val bestRouteIndex = candidateRoutes.indexOf(bestRoute)
                val sitesOnBestRoute = allSitesOnRoutes[bestRouteIndex] ?: emptyList()

                Log.d("ROUTE_SEARCH", "Best candidate: Route ${bestRouteIndex + 1} (Hazards: ${sitesOnBestRoute.size})")

                if (sitesOnBestRoute.isEmpty()) {
                    Log.d("ROUTE_SEARCH", "🎉 Safe route found!")
                    selectedRoute = bestRoute
                    finalSafeRouteFound = true
                } else {
                    val firstSite = sitesOnBestRoute.first()
                    Log.d("ROUTE_SEARCH", "Unsafe route. First hazard at: ${firstSite.latitude}, ${firstSite.longitude}")

                    val detourDistance = if (hazardousDrainpipeSites.contains(firstSite)) {
                        Log.d("ROUTE_SEARCH", "Hazard is a drainpipe. Detouring by 20m.")
                        0.00018 // Approx 20 meters
                    } else {
                        Log.d("ROUTE_SEARCH", "Hazard is a construction site. Detouring by ${100 * currentAttempt}m.")
                        0.0027 // Approx 300 meters
                    }

                    val waypoints = mutableListOf<LatLng>()
                    waypoints.add(firstSite) // Add the hazard location itself as a waypoint

                    val deltaLatToStart = start.latitude - firstSite.latitude
                    val deltaLngToStart = start.longitude - firstSite.longitude
                    val magnitudeToStart = sqrt(deltaLatToStart * deltaLatToStart + deltaLngToStart * deltaLngToStart)

                    val behindWaypoint: LatLng

                    if (magnitudeToStart < 1e-6) { // If start and firstSite are the same, fallback to random cardinal
                        val randomDirectionWaypoints = listOf(
                            LatLng(firstSite.latitude, firstSite.longitude + detourDistance), // East
                            LatLng(firstSite.latitude, firstSite.longitude - detourDistance), // West
                            LatLng(firstSite.latitude + detourDistance, firstSite.longitude), // North
                            LatLng(firstSite.latitude - detourDistance, firstSite.longitude)  // South
                        )
                        behindWaypoint = randomDirectionWaypoints[Random.nextInt(randomDirectionWaypoints.size)]
                        Log.d("ROUTE_SEARCH", "Start and hazard are same, generated random cardinal waypoint.")
                    } else {
                        val normDeltaLat = deltaLatToStart / magnitudeToStart
                        val normDeltaLng = deltaLngToStart / magnitudeToStart

                        val behindLat = firstSite.latitude + normDeltaLat * 0.00045 // Approx 50 meters
                        val behindLng = firstSite.longitude + normDeltaLng * 0.00045 // Approx 50 meters
                        behindWaypoint = LatLng(behindLat, behindLng)
                        Log.d("ROUTE_SEARCH", "Generated waypoint 50m behind hazard relative to start.")
                    }
                    waypoints.add(behindWaypoint)

                    Log.d("ROUTE_SEARCH", "Generated 2 waypoints: hazard location + 50m behind point.")

                    val newDetourRoutes = waypoints.mapNotNull { getDirectionsWithWaypoints(start, goal, it) }
                    candidateRoutes = newDetourRoutes
                    routes = (routes + newDetourRoutes).distinct()
                }
            }

            if (!finalSafeRouteFound) {
                Log.d("ROUTE_SEARCH", "No safe route found. Consulting GPT.")
                val candidateRoutesInfoForGpt = candidateRoutes.mapIndexed { index, route ->
                    val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sum()
                    RouteInfoForGpt(
                        id = "route_${index + 1}",
                        lengthKm = routeLength / 1000.0,
                        hazardSites = allSitesOnRoutes.getOrElse(index) { emptyList() } // Corrected access
                    )
                }
                val gptDecision = sendGptRequest(start, goal, candidateRoutesInfoForGpt)
                when (gptDecision?.decision) {
                    "choose_route" -> {
                        val chosenRouteId = gptDecision.chosenRouteId
                        val chosenRoute = candidateRoutesInfoForGpt.find { it.id == chosenRouteId }?.let { chosenInfo ->
                            candidateRoutes[candidateRoutesInfoForGpt.indexOf(chosenInfo)]
                        }
                        selectedRoute = chosenRoute ?: candidateRoutes.firstOrNull()
                    }
                    "suggest_waypoints" -> {
                        val gptWaypoints = gptDecision.waypoints
                        if (gptWaypoints != null && gptWaypoints.isNotEmpty()) {
                            val gptRecommendedRoute = getDirectionsWithWaypoints(start, goal, gptWaypoints.first())
                            selectedRoute = gptRecommendedRoute ?: candidateRoutes.firstOrNull()
                        } else {
                            selectedRoute = candidateRoutes.firstOrNull()
                        }
                    }
                    else -> {
                        selectedRoute = candidateRoutes.firstOrNull()
                    }
                }
            }

            // --- UI Update ---
            isLoading = false // Hide loading indicator
            val allMarkers = incompleteSites + allDrainpipes.mapNotNull { it.location }
            if (allMarkers.isNotEmpty()) {
                val bounds = LatLngBounds.Builder().apply { allMarkers.forEach { include(it) } }.build()
                cameraPositionState.move(CameraUpdate.fitBounds(bounds, 100))
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
            Text("경로를 탐색중입니다...", modifier = Modifier.padding(top = 60.dp))
        }
    } else {
        var routeVisibility by remember { mutableStateOf<List<Boolean>>(emptyList()) }

        LaunchedEffect(routes) {
            routeVisibility = List(routes.size) { true }
        }

        Column(modifier = modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(120.dp).padding(8.dp),
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
                                if (index < it.size) it[index] = !it[index]
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
                val routeColors = listOf(Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta, Color.Black, Color.DarkGray, Color.LightGray)

                routes.forEachIndexed { index, route ->
                    if (routeVisibility.getOrElse(index) { true }) {
                        val isSelected = route == selectedRoute
                        val isInitialNaverRoute = route == initialNaverRoute
                        val color = when {
                            isSelected -> Color.Green
                            isInitialNaverRoute -> Color.Red
                            else -> routeColors[index % routeColors.size]
                        }
                        val pathWidth = if (isSelected) 8.dp else if (isInitialNaverRoute) 6.dp else 3.dp
                        val outline = if (isSelected || isInitialNaverRoute) 1.dp else 0.dp

                        PathOverlay(coords = route, width = pathWidth, color = color, outlineWidth = outline)
                    }
                }

                incompleteSites.forEach { site ->
                    Marker(state = MarkerState(position = site), captionText = "공사중", iconTintColor = Color.Magenta)
                    CircleOverlay(
                        center = site,
                        radius = 150.0, // 150 meters for construction sites
                        color = Color.Magenta.copy(alpha = 0.2f), // Semi-transparent fill
                        outlineColor = Color.Magenta,
                        outlineWidth = 1.dp
                    )
                }

                // Display all drainpipes with color coding and radius
                allDrainpipes.forEach { drainpipe ->
                    drainpipe.location?.let {
                        val isHazardous = drainpipe.waterLevel >= 20.0
                        val color = if (isHazardous) Color.Red else Color.Blue
                        val caption = if (isHazardous) "위험 하수관로" else "하수관로"

                        Marker(
                            state = MarkerState(position = it),
                            captionText = caption,
                            iconTintColor = color,
                            captionColor = color
                        )

                        if (isHazardous) {
                            CircleOverlay(
                                center = it,
                                radius = 10.0, // 10 meters for hazardous drainpipes
                                color = Color.Red.copy(alpha = 0.2f), // Semi-transparent red fill
                                outlineColor = Color.Red,
                                outlineWidth = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

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

// ✅ Fetch Seoul drainpipe data
private fun getDrainpipeData(): List<DrainpipeInfo> {
    val allDrainpipeInfos = mutableSetOf<DrainpipeInfo>()
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
                    Log.e("DRAINPIPE_API_ERROR", "HTTP Error (seCd=$seCd): ${response.code}, ${response.message}")
                } else {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val parsedInfos = parseDrainpipeSites(responseBody)
                        Log.d("DRAINPIPE_DEBUG", "seCd=$seCd parsed ${parsedInfos.size} drainpipes.")
                        allDrainpipeInfos.addAll(parsedInfos)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DRAINPIPE_API_ERROR", "Exception (seCd=$seCd): ${e.message}", e)
        }
    }
    Log.d("DRAINPIPE_DEBUG", "Total unique drainpipes collected: ${allDrainpipeInfos.size}")
    return allDrainpipeInfos.toList()
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

// ✅ Parse drainpipe data from XML
fun parseDrainpipeSites(xml: String): List<DrainpipeInfo> {
    val list = mutableListOf<DrainpipeInfo>()
    val factory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    parser.setInput(xml.reader())

    var eventType = parser.eventType
    var pstnInfo: String? = null
    var meaWal: Double? = null
    var tagName: String? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> tagName = parser.name
            XmlPullParser.TEXT -> {
                when (tagName) {
                    "PSTN_INFO" -> pstnInfo = parser.text.trim()
                    "MEA_WAL" -> meaWal = parser.text.toDoubleOrNull()
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "row") {
                    if (pstnInfo != null && meaWal != null) {
                        list.add(DrainpipeInfo(address = pstnInfo, waterLevel = meaWal))
                    }
                    pstnInfo = null
                    meaWal = null
                }
                tagName = null
            }
            else -> {}
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
private fun isLocationOnPath(location: LatLng, path: List<LatLng>, threshold: Double): Boolean {
    for (i in 0 until path.size - 1) {
        val start = path[i]
        val end = path[i + 1]

        // Broad-phase check
        if (location.latitude < start.latitude.coerceAtMost(end.latitude) - 0.002 ||
            location.latitude > start.latitude.coerceAtLeast(end.latitude) + 0.002 ||
            location.longitude < start.longitude.coerceAtMost(end.longitude) - 0.002 ||
            location.longitude > start.longitude.coerceAtLeast(end.longitude) + 0.002) {
            continue
        }

        // Narrow-phase check (distance to segment)
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

// Finds hazard sites on routes with a given threshold
private fun findHazardSitesOnRoutes(routes: List<List<LatLng>>, sites: List<LatLng>, threshold: Double): Map<Int, List<LatLng>> {
    val sitesOnRoutes = mutableMapOf<Int, MutableList<LatLng>>()
    routes.forEachIndexed { index, route ->
        sites.forEach { site ->
            if (isLocationOnPath(site, route, threshold)) {
                sitesOnRoutes.getOrPut(index) { mutableListOf() }.add(site)
            }
        }
    }
    return sitesOnRoutes
}
