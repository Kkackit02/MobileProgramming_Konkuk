package com.example.lh_lbs_project

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import androidx.compose.material3.Checkbox // Import Checkbox
import androidx.compose.material3.OutlinedTextField // Already imported, but good to note

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapSelectionScreen(onRouteSearch: (LatLng, LatLng, Boolean, Boolean, Boolean) -> Unit, allNoiseLevels: List<NoiseLevelInfo>, allSensorInfo: List<SensorInfo>) {
    Log.d("MapSelectionScreen", "MapSelectionScreen loaded. allNoiseLevels size: ${allNoiseLevels.size}")
    var startLocation by remember { mutableStateOf<LatLng?>(null) }
    var goalLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(37.5665, 126.9780), 10.0) // Default to Seoul
    }

    

    // Toggle states for hazard data
    var includeNoiseLevel by remember { mutableStateOf(true) }
    var includeDrainpipe by remember { mutableStateOf(true) }
    var includeConstruction by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        

        // Hazard data toggle options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToggleCard(
                title = "소음도 표시",
                checked = includeNoiseLevel,
                onCheckedChange = { includeNoiseLevel = it }
            )
            ToggleCard(
                title = "하수관로 표시",
                checked = includeDrainpipe,
                onCheckedChange = { includeDrainpipe = it }
            )
            ToggleCard(
                title = "공사장 표시",
                checked = includeConstruction,
                onCheckedChange = { includeConstruction = it }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        NaverMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            onMapClick = { _, latLng ->
                if (startLocation == null) {
                    startLocation = latLng
                } else if (goalLocation == null) {
                    goalLocation = latLng
                } else {
                    startLocation = latLng
                    goalLocation = null
                }
            }
        ) {
            startLocation?.let {
                Marker(state = MarkerState(position = it), captionText = "출발지")
            }
            goalLocation?.let {
                Marker(state = MarkerState(position = it), captionText = "목적지")
            }
//            if (includeNoiseLevel) {
//                Log.d("MapSelectionScreen", "Drawing noise level markers. includeNoiseLevel is true.")
//                allNoiseLevels.forEach { noiseInfo ->
//                    noiseInfo.location?.let { location ->
//                        Log.d("MapSelectionScreen", "Adding noise marker at ${location.latitude}, ${location.longitude} with noise level ${noiseInfo.noiseLevel}")
//                        Marker(state = MarkerState(position = location), captionText = "소음도: ${noiseInfo.noiseLevel}")
//                    }
//                }
//            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { startLocation = null; goalLocation = null; }) {
                Text("초기화")
            }
            Button(
                onClick = { 
                    if (startLocation != null && goalLocation != null) {
                        onRouteSearch(startLocation!!, goalLocation!!, includeNoiseLevel, includeDrainpipe, includeConstruction)
                    } else {
                        Log.d("MapSelectionScreen", "출발지 또는 목적지를 선택해주세요.")
                    }
                },
                enabled = startLocation != null && goalLocation != null
            ) {
                Text("경로 탐색")
            }
        }
    }
}

@Composable
fun ToggleCard(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(120.dp)
            .height(60.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall)
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }
}
