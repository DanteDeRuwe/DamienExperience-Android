package com.example.damiantour

import com.example.damiantour.mapBox.model.LanguagesText
import com.example.damiantour.mapBox.model.Route
import com.example.damiantour.mapBox.model.Tuple
import com.example.damiantour.mapBox.model.Waypoint
import com.example.damiantour.network.model.RouteData
import com.example.damiantour.network.model.WaypointData
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement

/**
 * @author Simon
 * finds the closest point in a list and within a 500m
 * http://turfjs.org/docs/#distance
 */
fun findClosestPoint(clickPoint : Point, listOfWayPoints :List<Waypoint>) : Waypoint? {
    var closestPoint: Waypoint? = null
    var closestDistance: Double = Double.MAX_VALUE
    val size = listOfWayPoints.size
    var counter = 0
    //go over all waypoints
    while (counter < size) {
        val wp = listOfWayPoints.get(counter)
        val lon: Double = wp.longitude
        val lat: Double = wp.latitude
        val aPoint = Point.fromLngLat(lon, lat)
        //gets the distance in kilometers between two points (see link above)
        val distance = TurfMeasurement.distance(clickPoint, aPoint, "kilometers")
        //if waypoint is closer than override
        if (distance < closestDistance) {
            closestPoint = wp
            closestDistance = distance
        }
        counter++
    }
    //if waypoint is father than 500m then don't show the marker
    if (closestDistance > 0.5) {
        closestPoint = null
    }
    //return the waypoint (a waypoint or a null value)
    return closestPoint
}

fun mapWaypointDataToWaypoint(waypointdata: WaypointData) : Waypoint{
    val titles  = HashMap<String,String>()
    titles["nl"] = waypointdata.languagesText.title.nl
    titles["fr"] = waypointdata.languagesText.title.fr
    val descs  = HashMap<String,String>()
    descs["nl"] = waypointdata.languagesText.description.nl
    descs["fr"] = waypointdata.languagesText.description.fr
    val languagesText = LanguagesText(title = titles ,description = descs)
    return Waypoint(waypointdata.id,waypointdata.longitude,waypointdata.latitude,languagesText)
}

fun getCoordinatesFromRoute(routeData: RouteData) : List<Tuple>{
    val tupleList = ArrayList<Tuple>()
    routeData.path.coordinates.forEach{ list ->
        tupleList.add(Tuple(longitude = list[0], latitude = list[1]))
    }
    return tupleList
}
fun mapRouteDataToRoute(routeData: RouteData): Route {
    return Route(tourName = routeData.tourName,date = routeData.date,distanceInMeters = routeData.distanceInMeters)
}