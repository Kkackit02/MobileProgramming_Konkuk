package com.example.lh_lbs_project

import com.naver.maps.geometry.LatLng
import kotlin.math.sqrt

// Haversine formula to calculate distance between two LatLng points in meters
fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0 // Earth's radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return R * c
}

// Function to find hazard sites on routes
fun findHazardSitesOnRoutes(
    routes: List<List<LatLng>>,
    hazardSites: List<LatLng>,
    threshold: Double
): Map<Int, List<LatLng>> {
    val sitesOnRoutes = mutableMapOf<Int, MutableList<LatLng>>()

    routes.forEachIndexed { routeIndex, route ->
        val hazardsOnCurrentRoute = mutableListOf<LatLng>()
        hazardSites.forEach { hazardSite ->
            // Check if the hazard site is close to any segment of the route
            for (i in 0 until route.size - 1) {
                val startPoint = route[i]
                val endPoint = route[i + 1]

                // Simplified check: distance from hazard to start/end points of segment
                // A more robust solution would involve projecting the point onto the segment
                val distToStart = haversine(hazardSite.latitude, hazardSite.longitude, startPoint.latitude, startPoint.longitude)
                val distToEnd = haversine(hazardSite.latitude, hazardSite.longitude, endPoint.latitude, endPoint.longitude)

                if (distToStart <= threshold || distToEnd <= threshold) {
                    hazardsOnCurrentRoute.add(hazardSite)
                    break // Found a hazard on this route segment, move to next hazard site
                }
            }
        }
        if (hazardsOnCurrentRoute.isNotEmpty()) {
            sitesOnRoutes[routeIndex] = hazardsOnCurrentRoute
        }
    }
    return sitesOnRoutes
}
