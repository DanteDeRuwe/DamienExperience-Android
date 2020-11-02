package com.example.damiantour.network

import com.squareup.moshi.Json
import java.util.*

/**
 * @author Simon Bettens
 */
data class RouteData(
    @Json(name = "TourName") val tourName :String,
    @Json(name = "Date") val date : Date,
    @Json(name = "DistanceInMeters") val distanceInMeters : Int,
    @Json(name = "LineColor") val lineColor : String,
    @Json(name = "Coordinates") val coordinates : List<DoubleArray> )