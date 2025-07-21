package com.example.lh_lbs_project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RouteDataDisplayView(routeDisplayInfos: List<RouteDisplayInfo>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("탐색된 경로 데이터", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        if (routeDisplayInfos.isEmpty()) {
            Text("표시할 경로 데이터가 없습니다.")
        } else {
            LazyColumn {
                items(routeDisplayInfos) { routeInfo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("경로 ID: ${routeInfo.id}")
                            Text("길이: %.2f km".format(routeInfo.lengthKm))
                            Text("위험 지점 수: ${routeInfo.hazardSitesCount}")
                            Text("겹치는 위험 지점 수: ${routeInfo.overlappingHazardSitesCount}")
                            routeInfo.distanceToFirstHazard?.let {
                                Text("첫 위험 지점까지의 거리: %.2f m".format(it))
                            }
                            Text("경로 좌표 개수: ${routeInfo.routeCoordinates.size}")
                            routeInfo.routeCoordinates.firstOrNull()?.let { first ->
                                Text("시작 좌표: (%.4f, %.4f)".format(first.latitude, first.longitude))
                            }
                            routeInfo.routeCoordinates.lastOrNull()?.let { last ->
                                Text("끝 좌표: (%.4f, %.4f)".format(last.latitude, last.longitude))
                            }
                        }
                    }
                }
            }
        }
    }
}