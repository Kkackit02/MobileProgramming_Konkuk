package com.example.teamproject_map_login.uicomponents

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberMarkerState

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun NaverMapScreen(modifier: Modifier = Modifier)
{
    val konkuk = LatLng(37.5408, 127.0793)
    val cameraPositionState = rememberCameraPositionState{

        position = CameraPosition(konkuk, 15.0)
    }

    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ){
        Marker(
            state = rememberMarkerState(position = konkuk),
            captionText = "건국대학교"
        )
    }

}

