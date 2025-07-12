package com.example.teamproject_map_login.uicomponents

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.PolygonOverlay
import com.naver.maps.map.compose.PolylineOverlay
import com.naver.maps.map.compose.rememberCameraPositionState

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun NaverMapScreen04(modifier: Modifier = Modifier) {
    val konkuk = LatLng(37.5408, 127.0793)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(konkuk, 15.0)
    }

    val makerPosition = remember { mutableStateListOf<LatLng>() }

    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { _, latlng ->
            makerPosition.add(latlng)
        }
    ) {

        makerPosition.forEach { position ->
            Marker(
                state = MarkerState(position = position)
            )
        }
        if (makerPosition.size >= 3) {
            PolygonOverlay(
                coords = makerPosition.toList(),
                color = Color(red = 0x00, green=0xFF, blue=0x00, alpha = 0x55),
                outlineColor = Color.Blue,
                outlineWidth = 7.dp

            )
        }

//        if (makerPosition.size >= 2) {
//            PolylineOverlay(
//                coords = makerPosition,
//                color = Color.Blue,
//                width = 7.dp
//            )
//        }
    }
}