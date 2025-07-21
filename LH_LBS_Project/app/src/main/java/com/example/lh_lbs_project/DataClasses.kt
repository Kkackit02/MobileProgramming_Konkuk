package com.example.lh_lbs_project

import com.naver.maps.geometry.LatLng

// Data class for drainpipe information including water level
data class DrainpipeInfo(
    val address: String,
    val waterLevel: Double,
    var location: LatLng? = null
)

data class GptRouteDecision(
    val decision: String,
    val chosenRouteIndex: Int? = null,
    val waypoints: List<LatLng>? = null
)

// Modified to handle generic hazards
data class RouteInfoForGpt(
    val id: String,
    val lengthKm: Double,
    val hazardSites: List<LatLng>,
    val routeCoordinates: List<LatLng>
)
