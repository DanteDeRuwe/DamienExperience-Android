package com.example.damiantour.network

import com.squareup.moshi.Json
import java.util.*

/**
 * @author Simon Bettens & Jordy Van Kerkvoorde
 */
data class RouteData(
    val tourName: String,
    val date: Date,
    val distanceInMeters: Int,
    val path : PathData,
    val waypoints : List<WaypointData>
)