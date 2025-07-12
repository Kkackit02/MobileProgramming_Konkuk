package com.example.teamproject_map_login.uicomponents.MapComponent

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng

@Composable
fun TestScreen(modifier: Modifier = Modifier) {
    // 선택된 위치 상태 관리
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    // 기본 위치 리스트
    val defaultLocations = listOf(
        "황소상" to LatLng(37.54312, 127.07618),
        "새천년관 앞" to LatLng(37.54313, 127.07739),
        "공학관 앞" to LatLng(37.54168, 127.07867),
        "레이크홀 편의점" to LatLng(37.53937, 127.07706),
        "도서관" to LatLng(37.54160, 127.07356)
    )

    Column(modifier = modifier.fillMaxSize()) {
        // 선택용 지도: 위쪽 절반
        Box(modifier = Modifier
            .weight(2f)
            .fillMaxWidth()) {
            MapMarkerSelectScreen(
                defaultLocations = defaultLocations,
                onLocationSelected = { latLng, name ->
                    selectedLatLng = latLng
                    selectedName = name
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 출력용 지도: 아래쪽 절반
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            selectedLatLng?.let { latLng ->
                MapMarkerDisplayScreen(
                    location = latLng,
                    locationName = selectedName
                )
            }
        }
    }
}
