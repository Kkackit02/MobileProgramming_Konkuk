package com.example.teamproject_map_login.uicomponents.MapComponent

import android.view.Gravity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.*
@OptIn(ExperimentalNaverMapApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapMarkerSelectScreen(
    modifier: Modifier = Modifier,
    defaultLocations: List<Pair<String, LatLng>> = emptyList(),
    onLocationSelected: (LatLng, String?) -> Unit
) {
    val context = LocalContext.current

    // 유저가 마지막에 선택한 위치: LatLng + (정해진 장소 이름 or null)
    var selectedLocation by remember { mutableStateOf<Pair<LatLng, String?>?>(null) }

    // 카메라 초기 위치
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(37.5408, 127.0793), 15.0)
    }

    val uiSettings = remember {
        MapUiSettings(
            isZoomControlEnabled = true,
            isLocationButtonEnabled = true,
            isCompassEnabled = true,
            logoGravity = Gravity.BOTTOM or Gravity.END
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("판매 위치 설정") },
                actions = {
                    TextButton(onClick = {
                        selectedLocation?.let { (latLng, name) ->
                            onLocationSelected(latLng, name)
                            // onLocationSelected로 콜백, 이거 가져다 쓰면됩니다!
                        }
                    }) {
                        Text("선택 완료")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {


            selectedLocation?.first?.let { latLng ->
                Text(
                    text = "위도: %.5f, 경도: %.5f".format(latLng.latitude, latLng.longitude),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            NaverMap(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings,
                onMapClick = { _, coord ->
                    // 사용자가 클릭한 좌표로 마커를 갱신함 (직접 선택)
                    selectedLocation = coord to null
                }
            ) {
                // 주요 위치 마커 표시 (예: "정문", "학생회관")
                defaultLocations.forEach { (name, position) ->
                    val markerState = rememberUpdatedState(position) // marker 갱신용
                    Marker(
                        state = MarkerState(position = markerState.value),
                        captionText = name,
                        onClick = {
                            selectedLocation = position to name
                            true
                        }
                    )
                }

                // 사용자가 선택한 마커 1개만 표시
                selectedLocation?.let { (latLng, name) ->
                    Marker(
                        state = MarkerState(position = latLng),
                        captionText = name ?: "선택됨",
                        iconTintColor = Color.Blue
                    )
                }
            }
        }
    }
}