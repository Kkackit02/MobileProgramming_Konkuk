package com.example.teamproject_map_login.uicomponents

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapType
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.NaverMap

@OptIn(ExperimentalNaverMapApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NaverMapScreen02(modifier: Modifier = Modifier) {

    var mapType by remember {
        mutableStateOf(MapType.Basic)
    }

    var mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                isLocationButtonEnabled = true,
                isZoomControlEnabled = false,
                isCompassEnabled = true,
                isRotateGesturesEnabled = true,
                logoGravity = android.view.Gravity.TOP or android.view.Gravity.END
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Map") },
                actions = {
                    Button(onClick = {
                        mapType = MapType.Basic
                    }) {
                        Text("1")
                    }
                    Button(onClick = {
                        mapType = MapType.Satellite
                    }) {
                        Text("2")
                    }
                    Button(onClick = {
                        mapType = MapType.Hybrid
                    }) {
                        Text("3")
                    }
                    Button(onClick = {
                        mapType = MapType.Terrain
                    }) {
                        Text("4")
                    }
                }
            )
        }
    ) {
        NaverMap(
            modifier = Modifier.padding(it),
            properties = MapProperties(mapType = mapType),
            uiSettings = mapUiSettings
        )
    }
}
