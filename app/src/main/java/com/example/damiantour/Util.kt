package com.example.damiantour

import com.example.damiantour.mapBox.Waypoint
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement

// http://turfjs.org/docs/#distance
// finds the closest point in a list and within a 1 km
fun findClosestPoint(clickPoint : Point, listOfWayPoints :List<Waypoint>) : Waypoint? {
    var closestPoint : Waypoint? = null
    var closestDistance : Double = Double.MAX_VALUE
    val size = listOfWayPoints.size
    var counter = 0
    while (counter<size) {
        val wp = listOfWayPoints.get(counter)
        val lon: Double = wp.longitude
        val lat: Double = wp.latitude
        val aPoint = Point.fromLngLat(lon, lat)
        val distance = TurfMeasurement.distance(clickPoint,aPoint,"kilometers")
        if(distance<closestDistance){
            closestPoint = wp
            closestDistance = distance
        }
        counter++
    }
    if(closestDistance>0.5){
        closestPoint = null
        closestDistance = 0.0
    }
    return closestPoint
}