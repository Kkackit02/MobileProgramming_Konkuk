package com.example.teamproject_map_login.uicomponents

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.teamproject_map_login.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberMarkerState
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.launch

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun NaverMapMarker01(modifier: Modifier = Modifier) {
    val konkuk = LatLng(37.5408, 127.0793)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(konkuk, 15.0)
    }
    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = rememberMarkerState(position = konkuk),
            icon = MarkerIcons.RED,
            captionText = "건국대학교"
        )
    }
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun NaverMapMarker02(modifier: Modifier = Modifier) {
    val konkuk = LatLng(37.5408, 127.0793)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(konkuk, 15.0)
    }

    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = rememberMarkerState(position = konkuk),
            icon = MarkerIcons.BLACK,
            //iconTintColor = Color.Magenta,
            //iconTintColor = Color(0x88458E6F),
            iconTintColor = Color(red = 0.5f, green = 0.2f, blue = 0.8f, alpha = 1.0f),
            captionText = "건국대학교"
        )
    }
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun NaverMapMarker03(modifier: Modifier = Modifier) {
    val konkuk = LatLng(37.5408, 127.0793)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(konkuk, 15.0)
    }
    var isClicked by remember { mutableStateOf(false) }

    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = rememberMarkerState(position = konkuk),
            icon = if (isClicked) MarkerIcons.RED else MarkerIcons.BLUE,
            captionText = "건국대학교",
            onClick = {
                isClicked = !isClicked
                true
            }
        )
    }
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun NaverMapMarker04(modifier: Modifier = Modifier) {
    val konkuk = LatLng(37.5408, 127.0793)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(konkuk, 15.0)
    }

    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = rememberMarkerState(position = konkuk),
            icon = OverlayImage.fromResource(R.drawable.baseline_gps_fixed_24),
            width = 40.dp,
            height = 40.dp,
            captionText = "건국대학교"
        )
    }
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun NaverMapMarker05() {
    val konkuk = LatLng(37.5408, 127.0793)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(konkuk, 15.0)
    }

    val bounceOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    NaverMap(cameraPositionState = cameraPositionState) {
        Marker(
            state = MarkerState(position = konkuk),
            anchor = Offset(0.5f, 1f + bounceOffset.value / 100f),
            onClick = {
                scope.launch {
                    bounceOffset.animateTo(
                        targetValue = -30f,
                        animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)
                    )
                    bounceOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = spring()
                    )
                }
                true
            }
        )
    }
}


