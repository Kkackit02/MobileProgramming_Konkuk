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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapSelectionScreen(onRouteSearch: (LatLng, LatLng) -> Unit) {
    var startLocation by remember { mutableStateOf<LatLng?>(null) }
    var goalLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(37.5665, 126.9780), 10.0) // Default to Seoul
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { startLocation = null; goalLocation = null }) {
                Text("초기화")
            }
            Button(
                onClick = { 
                    if (startLocation != null && goalLocation != null) {
                        onRouteSearch(startLocation!!, goalLocation!!)
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
