package com.example.damiantour.network

import com.squareup.moshi.Json
import java.util.*

/**
 * @author Simon Bettens
 */
data class RouteData(
    val tourName: String,
    val date: Date,
    val distanceInMeters: Int,
    val lineColor: String,
    val coordinates: List<List<Double>>
)