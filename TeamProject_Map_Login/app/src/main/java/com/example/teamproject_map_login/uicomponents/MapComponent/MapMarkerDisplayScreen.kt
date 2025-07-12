package com.example.teamproject_map_login.uicomponents.MapComponent

import android.view.Gravity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapMarkerDisplayScreen(
    location: LatLng,
    locationName: String? = null,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(location) {
        cameraPositionState.position = CameraPosition(location, 15.0)
    }

    val uiSettings = remember {
        MapUiSettings(
            isZoomControlEnabled = false,
            isLocationButtonEnabled = false,
            isCompassEnabled = false,
            logoGravity = Gravity.BOTTOM or Gravity.END,
            isScrollGesturesEnabled = false,
            isZoomGesturesEnabled = false,
            isRotateGesturesEnabled = false,
            isTiltGesturesEnabled = false
        )
    }

    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings
    ) {
        Marker(
            state = MarkerState(position = location),
            captionText = locationName ?: "선택된 위치",
            iconTintColor = Color.Red
        )
    }
}
