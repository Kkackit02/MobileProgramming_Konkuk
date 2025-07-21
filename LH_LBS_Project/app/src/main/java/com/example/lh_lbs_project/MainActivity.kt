package com.example.lh_lbs_project

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.lh_lbs_project.ui.theme.LH_LBS_ProjectTheme
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject

import com.example.lh_lbs_project.DrainpipeInfo
import com.example.lh_lbs_project.GptRouteDecision
import com.example.lh_lbs_project.RouteInfoForGpt
import com.example.lh_lbs_project.NoiseLevelInfo
import com.example.lh_lbs_project.SensorInfo

sealed class Screen {
    object Title : Screen()
    object DataLoad : Screen()
    data class MapSelection(val incompleteSites: List<LatLng>, val allDrainpipes: List<DrainpipeInfo>, val allNoiseLevels: List<NoiseLevelInfo>, val allSensorInfo: List<SensorInfo>) : Screen()
    data class Directions(val start: LatLng, val goal: LatLng, val incompleteSites: List<LatLng>, val allDrainpipes: List<DrainpipeInfo>, val allNoiseLevels: List<NoiseLevelInfo>, val allSensorInfo: List<SensorInfo>, val includeNoiseLevel: Boolean, val includeDrainpipe: Boolean, val includeConstruction: Boolean) : Screen()
}

class MainActivity : ComponentActivity() {

    private val client = OkHttpClient()
    private val gptUrl = "https://recommendroute-42j6jwyw4q-uc.a.run.app/recommendRoute"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LH_LBS_ProjectTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Title) }
                var incompleteSitesData by remember { mutableStateOf<List<LatLng>>(emptyList()) }
                var allDrainpipesData by remember { mutableStateOf<List<DrainpipeInfo>>(emptyList()) }
                var allNoiseLevelsData by remember { mutableStateOf<List<NoiseLevelInfo>>(emptyList()) }
                var allSensorInfoData by remember { mutableStateOf<List<SensorInfo>>(emptyList()) }

                when (currentScreen) {
                    Screen.Title -> {
                        TitleScreen(onTimeout = {
                            currentScreen = Screen.DataLoad
                        })
                    }
                    Screen.DataLoad -> {
                        DataLoadScreen(onDataLoaded = { incompleteSites, allDrainpipes, allNoiseLevels, allSensorInfo ->
                            incompleteSitesData = incompleteSites
                            allDrainpipesData = allDrainpipes
                            allNoiseLevelsData = allNoiseLevels
                            allSensorInfoData = allSensorInfo
                            currentScreen = Screen.MapSelection(incompleteSites, allDrainpipes, allNoiseLevels, allSensorInfo)
                        })
                    }
                    is Screen.MapSelection -> {
                        val screenState = currentScreen as Screen.MapSelection
                        MapSelectionScreen(
                            onRouteSearch = { start, goal, includeNoiseLevel, includeDrainpipe, includeConstruction ->
                                currentScreen = Screen.Directions(start, goal, screenState.incompleteSites, screenState.allDrainpipes, screenState.allNoiseLevels, screenState.allSensorInfo, includeNoiseLevel, includeDrainpipe, includeConstruction)
                            },
                            allNoiseLevels = screenState.allNoiseLevels,
                            allSensorInfo = screenState.allSensorInfo
                        )
                    }
                    is Screen.Directions -> {
                        val screenState = currentScreen as Screen.Directions
                        DirectionsScreen(
                            modifier = Modifier.fillMaxSize(),
                            start = screenState.start,
                            goal = screenState.goal,
                            incompleteSites = screenState.incompleteSites,
                            allDrainpipes = screenState.allDrainpipes,
                            allNoiseLevels = screenState.allNoiseLevels,
                            allSensorInfo = screenState.allSensorInfo,
                            includeNoiseLevel = screenState.includeNoiseLevel,
                            includeDrainpipe = screenState.includeDrainpipe,
                            includeConstruction = screenState.includeConstruction,
                            sendGptRequest = { start, goal, candidateRoutesInfo ->
                                sendGptRequest(start, goal, candidateRoutesInfo)
                            }
                        )
                    }
                }
            }
        }
    }

    // Firebase Function 호출을 위한 GPT 요청 함수
    private suspend fun sendGptRequest(start: LatLng, goal: LatLng, candidateRoutesInfo: List<RouteInfoForGpt>): GptRouteDecision? {
        val jsonBody = JSONObject().apply {
            put("start", JSONObject().apply { put("lat", start.latitude); put("lng", start.longitude) })
            put("goal", JSONObject().apply { put("lat", goal.latitude); put("lng", goal.longitude) })
            put("candidateRoutesInfo", JSONArray(candidateRoutesInfo.map { routeInfo ->
                JSONObject().apply {
                    put("id", routeInfo.id)
                    put("lengthKm", routeInfo.lengthKm)
                    put("hazardSites", JSONArray(routeInfo.hazardSites.map { site ->
                        JSONObject().apply { put("lat", site.latitude); put("lng", site.longitude) }
                    }))
                    put("routeCoordinates", JSONArray(routeInfo.routeCoordinates.map { coord ->
                        JSONObject().apply { put("lat", coord.latitude); put("lng", coord.longitude) }
                    }))
                }
            }))
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonBody.toString()
        )

        val request = Request.Builder()
            .url(gptUrl)
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GPT_ERROR", "Firebase Function HTTP 오류: ${response.code}, ${response.message}")
                    null
                } else {
                    val responseBody = response.body?.string() ?: return null
                    Log.d("GPT_RESPONSE", responseBody)
                    val jsonResponse = JSONObject(responseBody)

                    val decision = jsonResponse.getString("decision")
                    var chosenRouteIndex: Int? = null
                    var waypoints: List<LatLng>? = null

                    when (decision) {
                        "choose_route" -> {
                            chosenRouteIndex = jsonResponse.getInt("chosenRouteIndex")
                        }
                        "suggest_waypoints" -> {
                            val waypointsArray = jsonResponse.getJSONArray("waypoints")
                            waypoints = (0 until waypointsArray.length()).map { i ->
                                val waypoint = waypointsArray.getJSONObject(i)
                                LatLng(waypoint.getDouble("lat"), waypoint.getDouble("lng"))
                            }
                        }
                    }
                    GptRouteDecision(decision, chosenRouteIndex, waypoints)
                }
            }
        } catch (e: Exception) {
            Log.e("GPT_ERROR", "Firebase Function 호출 예외 발생: ${e.message}", e)
            null
        }
    }
}