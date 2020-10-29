package com.example.damiantour

import com.example.damiantour.mapBox.Waypoint
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement

/***
 * @author Simon
 */
// http://turfjs.org/docs/#distance
// finds the closest point in a list and within a 500m
fun findClosestPoint(clickPoint : Point, listOfWayPoints :List<Waypoint>) : Waypoint? {
    var closestPoint : Waypoint? = null
    var closestDistance : Double = Double.MAX_VALUE
    val size = listOfWayPoints.size
    var counter = 0
    //go over all waypoints
    while (counter<size) {
        val wp = listOfWayPoints.get(counter)
        val lon: Double = wp.longitude
        val lat: Double = wp.latitude
        val aPoint = Point.fromLngLat(lon, lat)
        //gets the distance in kilometers between two points (see link above)
        val distance = TurfMeasurement.distance(clickPoint,aPoint,"kilometers")
        //if waypoint is closer than override
        if(distance<closestDistance){
            closestPoint = wp
            closestDistance = distance
        }
        counter++
    }
    //if waypoint is father than 500m then don't show the marker
    if(closestDistance>0.5){
        closestPoint = null
    }
    //return the waypoint (a waypoint or a null value)
    return closestPoint
}