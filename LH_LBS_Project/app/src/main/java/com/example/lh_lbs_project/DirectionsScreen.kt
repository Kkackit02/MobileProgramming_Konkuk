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
import kotlin.random.Random
import kotlin.math.sqrt

// Import data classes from the common file
import com.example.lh_lbs_project.DrainpipeInfo
import com.example.lh_lbs_project.GptRouteDecision
import com.example.lh_lbs_project.RouteInfoForGpt
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import com.example.lh_lbs_project.RouteDataDisplayView

data class RouteDisplayInfo(
    val id: String,
    val lengthKm: Double,
    val hazardSitesCount: Int,
    val overlappingHazardSitesCount: Int,
    val distanceToFirstHazard: Double?,
    val routeCoordinates: List<LatLng>
)

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun DirectionsScreen(
    modifier: Modifier = Modifier,
    start: LatLng,
    goal: LatLng,
    incompleteSites: List<LatLng>,
    allDrainpipes: List<DrainpipeInfo>,
    sendGptRequest: suspend (LatLng, LatLng, List<RouteInfoForGpt>) -> GptRouteDecision?
) {
    val cameraPositionState = rememberCameraPositionState()
    var routes by remember { mutableStateOf<List<List<LatLng>>>(emptyList()) }
    var selectedRoute by remember { mutableStateOf<List<LatLng>?>(null) }
    var initialNaverRoute by remember { mutableStateOf<List<LatLng>?>(null) }
    var isLoading by remember { mutableStateOf(true) } // Loading state
    var allSitesOnRoutes by remember { mutableStateOf<Map<Int, List<LatLng>>>(emptyMap()) }

    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0 for Map, 1 for Route Data

    LaunchedEffect(start, goal, incompleteSites, allDrainpipes) {
        isLoading = true
        cameraPositionState.position = CameraPosition(start, 11.0)

        scope.launch(Dispatchers.IO) {
            // Filter for hazardous drainpipes (water level >= 20%)
            val hazardousDrainpipeSites = allDrainpipes
                .filter { it.waterLevel >= 0.20 }
                .mapNotNull { it.location }

            Log.d("DIRECTIONS_DEBUG", "Total drainpipes: ${allDrainpipes.size}")
            Log.d("DIRECTIONS_DEBUG", "Hazardous drainpipes: ${hazardousDrainpipeSites.size}")

            // --- Route Search Phase ---
            var candidateRoutes = getDirections(start, goal) ?: emptyList()
            initialNaverRoute = candidateRoutes.firstOrNull()
            routes = candidateRoutes

            val maxAttempts = 3
            var currentAttempt = 0
            var finalSafeRouteFound = false

            while (currentAttempt < maxAttempts && !finalSafeRouteFound && candidateRoutes.isNotEmpty()) {
                currentAttempt++
                Log.d("ROUTE_SEARCH", "--- Search Attempt: $currentAttempt ---")

                val constructionSitesOnRoutes = findHazardSitesOnRoutes(candidateRoutes, incompleteSites, threshold = 150.0)
                val drainpipeSitesOnRoutes = findHazardSitesOnRoutes(candidateRoutes, hazardousDrainpipeSites, threshold = 10.0)

                allSitesOnRoutes = (constructionSitesOnRoutes.keys + drainpipeSitesOnRoutes.keys).associateWith { routeIndex ->
                    (constructionSitesOnRoutes[routeIndex] ?: emptyList()) + (drainpipeSitesOnRoutes[routeIndex] ?: emptyList())
                }

                Log.d("ROUTE_SEARCH", "Found ${allSitesOnRoutes.values.sumOf { it.size }} total hazards on routes.")

                val bestRoute = candidateRoutes.minByOrNull { route ->
                    val routeIndex = candidateRoutes.indexOf(route)
                    val hazardCount = allSitesOnRoutes[routeIndex]?.size ?: 0
                    val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sumOf { it }
                    hazardCount * 100000.0 + routeLength
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

                    // Always generate a waypoint in a random cardinal direction around the hazard site
                    val randomDirectionWaypoints = listOf(
                        LatLng(firstSite.latitude, firstSite.longitude + detourDistance), // East
                        LatLng(firstSite.latitude, firstSite.longitude - detourDistance), // West
                        LatLng(firstSite.latitude + detourDistance, firstSite.longitude), // North
                        LatLng(firstSite.latitude - detourDistance, firstSite.longitude)  // South
                    )
                    val detourWaypoint = randomDirectionWaypoints[Random.nextInt(randomDirectionWaypoints.size)]
                    waypoints.add(detourWaypoint)
                    Log.d("ROUTE_SEARCH", "Generated 1 detour waypoint in a random cardinal direction around the hazard.")

                    val newDetourRoutes = listOfNotNull(getDirectionsWithWaypoints(start, goal, waypoints))
                    candidateRoutes = newDetourRoutes
                    routes = (routes + newDetourRoutes).distinct()
                }
            }

            if (!finalSafeRouteFound) {
                Log.d("ROUTE_SEARCH", "No safe route found. Consulting GPT.")
                selectedTabIndex = 1 // Switch to Route Data tab
                val top3CandidateRoutesInfoForGpt = candidateRoutes.mapIndexed { index, route ->
                    val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sumOf { it }
                    RouteInfoForGpt(
                        id = "route_${index + 1}",
                        lengthKm = routeLength / 1000.0,
                        hazardSites = allSitesOnRoutes.getOrElse(index) { emptyList() },
                        routeCoordinates = route
                    )
                }
                    .sortedWith(compareBy({ it.hazardSites.size }, { it.lengthKm }))
                    .take(2)

                val gptDecision = sendGptRequest(start, goal, top3CandidateRoutesInfoForGpt)
                when (gptDecision?.decision) {
                    "choose_route" -> {
                        val chosenRouteIndex = gptDecision.chosenRouteIndex
                        if (chosenRouteIndex != null && chosenRouteIndex >= 0 && chosenRouteIndex < top3CandidateRoutesInfoForGpt.size) {
                            val chosenInfo = top3CandidateRoutesInfoForGpt[chosenRouteIndex]
                            selectedRoute = chosenInfo.routeCoordinates
                        } else {
                            selectedRoute = candidateRoutes.firstOrNull()
                        }
                    }
                    "suggest_waypoints" -> {
                        val gptWaypoints = gptDecision.waypoints
                        if (gptWaypoints != null && gptWaypoints.isNotEmpty()) {
                            val gptRecommendedRoute = getDirectionsWithWaypoints(start, goal, gptWaypoints)
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

            // Adjust camera to fit the selected route
            selectedRoute?.let { route ->
                if (route.isNotEmpty()) {
                    val routeBounds = LatLngBounds.Builder().apply { route.forEach { include(it) } }.build()
                    cameraPositionState.move(CameraUpdate.fitBounds(routeBounds, 100))
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("지도") })
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("경로 데이터") })
        }

        when (selectedTabIndex) {
            0 -> { // Map Tab
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

                    Column(modifier = Modifier.fillMaxSize()) {
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
                                    Log.d("DIRECTIONS_DEBUG", "Drainpipe location: ${it.latitude}, ${it.longitude}")
                                    val isHazardous = drainpipe.waterLevel >= 20.0
                                    val color = if (isHazardous) Color.Red else Color.Blue
                                    val caption = if (isHazardous) "위험 하수관로" else "하수관로"

                                    Marker(
                                        state = MarkerState(position = it),
                                        captionText = caption,
                                        iconTintColor = color,
                                        captionColor = color
                                    )

                                    CircleOverlay(
                                        center = it,
                                        radius = if (isHazardous) 10.0 else 5.0, // 10 meters for hazardous, 5 meters for normal
                                        color = color.copy(alpha = 0.2f), // Semi-transparent fill based on marker color
                                        outlineColor = color,
                                        outlineWidth = 1.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            1 -> { // Route Data Tab
                val routeDisplayInfos = routes.mapIndexed { index, route ->
                    val hazardCount = allSitesOnRoutes[index]?.size ?: 0
                    val routeLength = route.zipWithNext { a, b -> haversine(a.latitude, a.longitude, b.latitude, b.longitude) }.sumOf { it }

                    val currentRouteHazards = allSitesOnRoutes[index] ?: emptyList()
                    val otherRoutesHazards = allSitesOnRoutes.filterKeys { it != index }.values.flatten()
                    val overlappingHazardSitesCount = currentRouteHazards.intersect(otherRoutesHazards.toSet()).size

                    val firstHazardSite = currentRouteHazards.firstOrNull()
                    val distanceToFirstHazard = if (firstHazardSite != null && route.isNotEmpty()) {
                        // Find the segment that contains or is closest to the first hazard
                        var dist = 0.0
                        for (i in 0 until route.size - 1) {
                            val segmentStart = route[i]
                            val segmentEnd = route[i + 1]
                            val distToSegmentEnd = haversine(segmentStart.latitude, segmentStart.longitude, segmentEnd.latitude, segmentEnd.longitude)
                            dist += distToSegmentEnd
                            // Simple check: if the hazard is close to the segment end, consider this the point
                            if (haversine(firstHazardSite.latitude, firstHazardSite.longitude, segmentEnd.latitude, segmentEnd.longitude) < 50) { // 50m threshold
                                break
                            }
                        }
                        dist
                    } else {
                        null
                    }

                    RouteDisplayInfo(
                        id = "route_${index + 1}",
                        lengthKm = routeLength / 1000.0,
                        hazardSitesCount = hazardCount,
                        overlappingHazardSitesCount = overlappingHazardSitesCount,
                        distanceToFirstHazard = distanceToFirstHazard,
                        routeCoordinates = route
                    )
                }
                RouteDataDisplayView(routeDisplayInfos)
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
private fun getDirectionsWithWaypoints(start: LatLng, goal: LatLng, waypoints: List<LatLng>): List<LatLng>? {
    val waypointsString = waypoints.take(5).joinToString("_") { "${it.longitude},${it.latitude}" }
    val url = "https://maps.apigw.ntruss.com/map-direction/v1/driving" +
            "?start=${start.longitude},${start.latitude}" +
            "&goal=${goal.longitude},${goal.latitude}" +
            if (waypointsString.isNotEmpty()) "&waypoints=$waypointsString" else ""

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
        Log.e("DIRECTIONS_ERROR", "예외 발생: ${e.message}", e)
        null
    }
}

// Haversine formula to calculate distance between two LatLng points in meters
fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0 // Earth's radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return R * c
}

// Function to find hazard sites on routes
fun findHazardSitesOnRoutes(
    routes: List<List<LatLng>>,
    hazardSites: List<LatLng>,
    threshold: Double
): Map<Int, List<LatLng>> {
    val sitesOnRoutes = mutableMapOf<Int, MutableList<LatLng>>()

    routes.forEachIndexed { routeIndex, route ->
        val hazardsOnCurrentRoute = mutableListOf<LatLng>()
        hazardSites.forEach { hazardSite ->
            // Check if the hazard site is close to any segment of the route
            for (i in 0 until route.size - 1) {
                val startPoint = route[i]
                val endPoint = route[i + 1]

                // Simplified check: distance from hazard to start/end points of segment
                // A more robust solution would involve projecting the point onto the segment
                val distToStart = haversine(hazardSite.latitude, hazardSite.longitude, startPoint.latitude, startPoint.longitude)
                val distToEnd = haversine(hazardSite.latitude, hazardSite.longitude, endPoint.latitude, endPoint.longitude)

                if (distToStart <= threshold || distToEnd <= threshold) {
                    hazardsOnCurrentRoute.add(hazardSite)
                    break // Found a hazard on this route segment, move to next hazard site
                }
            }
        }
        if (hazardsOnCurrentRoute.isNotEmpty()) {
            sitesOnRoutes[routeIndex] = hazardsOnCurrentRoute
        }
    }
    return sitesOnRoutes
}





