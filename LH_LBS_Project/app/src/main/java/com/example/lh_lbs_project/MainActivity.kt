package com.example.lh_lbs_project

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                var showDirections by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxSize()) {
                    // GPTRequestScreen(
                //     resultText = result,
                //     onRequestClick = {
                //         scope.launch(Dispatchers.IO) {
                //             val response = sendGptRequest()
                //             result = response
                //         }
                //     },
                //     modifier = Modifier
                //         .weight(1f)
                //         .fillMaxWidth()
                // )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { showDirections = false }) {
                            Text("마커 표시")
                        }
                        Button(onClick = { showDirections = true }) {
                            Text("경로 표시")
                        }
                    }

                    if (showDirections) {
                        DirectionsScreen(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            sendGptRequest = { start, goal, blockedAreas ->
                                sendGptRequest(start, goal, blockedAreas)
                            }
                        )
                    } else {
                        MapMarkerDisplayScreen(
                            location = LatLng(37.54160, 127.07356),
                            locationName = "TEST",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Firebase Function 호출을 위한 GPT 요청 함수
    suspend fun sendGptRequest(start: LatLng, goal: LatLng, candidateRoutesInfo: List<RouteInfoForGpt>): GptRouteDecision? {
        val jsonBody = JSONObject().apply {
            put("start", JSONObject().apply { put("lat", start.latitude); put("lng", start.longitude) })
            put("goal", JSONObject().apply { put("lat", goal.latitude); put("lng", goal.longitude) })
            put("candidateRoutesInfo", JSONArray(candidateRoutesInfo.map { routeInfo ->
                JSONObject().apply {
                    put("id", routeInfo.id)
                    put("lengthKm", routeInfo.lengthKm)
                    put("constructionSites", JSONArray(routeInfo.constructionSites.map { site ->
                        JSONObject().apply { put("lat", site.latitude); put("lng", site.longitude) }
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
                    var chosenRouteId: String? = null
                    var waypoints: List<LatLng>? = null

                    when (decision) {
                        "choose_route" -> {
                            chosenRouteId = jsonResponse.getString("chosenRouteId")
                        }
                        "suggest_waypoints" -> {
                            val waypointsArray = jsonResponse.getJSONArray("waypoints")
                            waypoints = (0 until waypointsArray.length()).map { i ->
                                val waypoint = waypointsArray.getJSONObject(i)
                                LatLng(waypoint.getDouble("lat"), waypoint.getDouble("lng"))
                            }
                        }
                    }
                    GptRouteDecision(decision, chosenRouteId, waypoints)
                }
            }
        } catch (e: Exception) {
            Log.e("GPT_ERROR", "Firebase Function 호출 예외 발생: ${e.message}", e)
            null
        }
    }
}
