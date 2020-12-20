package com.example.damiantour.mapBox

import android.app.Application
import androidx.lifecycle.*
import com.example.damiantour.database.dao.LocationDatabaseDao
import com.example.damiantour.database.dao.RouteDatabaseDao
import com.example.damiantour.database.dao.TupleDatabaseDao
import com.example.damiantour.database.dao.WaypointDatabaseDao
import com.example.damiantour.findClosestPoint
import com.example.damiantour.mapBox.model.LocationData
import com.example.damiantour.mapBox.model.Tuple
import com.example.damiantour.mapBox.model.Waypoint
import com.example.damiantour.mapRouteDataToRoute
import com.example.damiantour.mapWaypointDataToWaypoint
import com.example.damiantour.network.model.RouteData
import com.example.damiantour.network.model.WaypointData
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/***
 * @author Simon Bettens and Jordy Van Kerkvoorde
 */
class MapViewModel(private val tupleDatabaseDao: TupleDatabaseDao,
                   private val waypointDatabaseDao: WaypointDatabaseDao,
                   private val locationDatabaseDao: LocationDatabaseDao,
                   private val routeDatabaseDao: RouteDatabaseDao,
                   application: Application) :
        AndroidViewModel(application) {

    lateinit var mapBoxMap: MapboxMap
    private var locationUtils: LocationUtils = LocationUtils

    // coords to show the line
    private var _routeCoordinates = tupleDatabaseDao.getAllTuplesLiveData()
    val routeCoordinates: LiveData<List<Tuple>>
        get() = _routeCoordinates

    //waypoints on the line
    private var _waypoints = waypointDatabaseDao.getAllWaypointDataLiveData()
    val waypoints: LiveData<List<Waypoint>>
        get() = _waypoints

    //selected waypoint
    private var _waypoint = MutableLiveData<Waypoint>()
    val waypoint: LiveData<Waypoint>
        get() = _waypoint

    //List of 30 records (coordstuple every 2 seconds)
    private var _tempLocations = locationUtils.getTempLocationList()
    val tempLocations: LiveData<MutableList<LocationData>>
        get() = _tempLocations

    //1 min locations (6 coordstuples every 1 min)
    private var _locations = locationDatabaseDao.getAllLocationsLiveData()
    val locations: LiveData<List<LocationData>>
        get() = _locations

    /**
     * @author Simon
     * a method to mix both lists to form a nice line on the map
     * only used to show the users path
     * needs to check if the list of location of every 1 min is initialized and that it has values
     * needs to check if the list of templocation of every 2 sec is initialized and that it has values
     */
    private fun getMixListLocations(): List<LocationData>? {
        val listTempLoc = _tempLocations.value
        val listLoc = _locations.value
        if (listLoc != null && listLoc.isNotEmpty()) {
            return if (listTempLoc != null && listTempLoc.isNotEmpty()) {
                val mix = listLoc + listTempLoc
                mix
            } else {
                listLoc
            }
        } else {
            if (listTempLoc != null && listTempLoc.isNotEmpty()) {
                return listTempLoc
            }
            return ArrayList()
        }
    }

    fun getRoute() : List<Point>{
        val tupleList = routeCoordinates.value
        val pointList = ArrayList<Point>()
        if(!tupleList.isNullOrEmpty()) {
            for (tuple in tupleList) {
                pointList.add(Point.fromLngLat(tuple.longitude, tuple.latitude))
            }
        }
        return pointList
    }

    //size of the list
    private var _listSize = MutableLiveData<Int>()
    val listSize: LiveData<Int>
        get() = _listSize

    //kinda constructor  but not really
    init {
        _listSize.value = 0
        //_tempLocations.value = ArrayList<Tuple>()
    }

    /**
     * @author Simon & Ruben
     * adds the route
     * gets all the coords of the route
     * sets the waypoints
     */
    fun addPath(routeData: RouteData) {
        insertRouteInDatabase(routeData)
        val routeCoordinatesList = ArrayList<Point>()
        val tupleList = ArrayList<Tuple>()
        val coordsList = routeData.path.coordinates
        val length = coordsList.size
        var counter = 0
        //Loops over all the coordinates
        while (counter < length) {
            val tupel = coordsList[counter]
            val lon = tupel[0]
            val lat = tupel[1]
            //adds coordinates to the route
            tupleList.add(Tuple(longitude = lon,latitude = lat))
            routeCoordinatesList.add(Point.fromLngLat(lon, lat))
            counter++
        }
        insertRouteTupleInDatabase(tupleList)
        addWaypoints(routeData.waypoints)
    }

    private fun insertRouteTupleInDatabase(tuples : List<Tuple>){

        GlobalScope.launch {
            tupleDatabaseDao.clear()
            for (tuple in tuples){
                tupleDatabaseDao.insert(tuple)
            }
        }
    }

    private fun insertRouteInDatabase(routeData: RouteData) {
        GlobalScope.launch(Dispatchers.IO) {
            routeDatabaseDao.clear()
            val route = mapRouteDataToRoute(routeData)
            routeDatabaseDao.insert(route)
        }
    }

    /**
     * @author Jordy Van Kerkvoorde
     */
    private fun addWaypoints(waypoints: List<WaypointData>) {
        _listSize.postValue(waypoints.size)
        GlobalScope.launch(Dispatchers.IO) {
            waypointDatabaseDao.clear()
            for (waypointdata in waypoints) {
                val waypoint = mapWaypointDataToWaypoint(waypointdata)

                waypointDatabaseDao.insert(waypoint)
            }
        }
    }

    /**
     * @author Simon Bettens
     * tries to get select the closest Waypoint from the selected point
     * if the every waypoint is further than 500m the method returns 'null' (in  method findClosestPoint(clickPoint, list))
     * if the closest waypoint is the same as the selected waypoint,the selected waypoint will be deselected
     */
    fun onClickMap(latLng: LatLng) {
        val clickPoint = Point.fromLngLat(latLng.longitude, latLng.latitude)
        val lastwp = _waypoint.value
        val list = _waypoints.value
        if (list != null) {
            val wp = findClosestPoint(clickPoint, list)
            if (lastwp != null && wp != null) {
                if (wp.longitude == lastwp.longitude && wp.latitude == lastwp.latitude) {
                    _waypoint.value = null
                    return
                }
            }
            _waypoint.value = wp
        }
    }

    /**
     * @author Simon Bettens
     * checks if there is a route present
     */
    suspend fun hasNoRoute() : Boolean{
        // niet verbeteren
        //dit moet gebeuren
        return routeDatabaseDao.getRoute() == null
    }
    /**
     * @author Simon
     * clear both the local database
     */
    suspend fun deleteDatabaseLocations() {
        locationDatabaseDao.clear()
    }

    /**
     * @author Simon
     *  return a list of point from the mixlist of locations  that can be mapped on the map
     */
    fun createLineSourceFromWalkedRoute(): ArrayList<Point> {
        val routeList = getMixListLocations()
        val walkedCoordinatesList = ArrayList<Point>()
        var counter = 0
        val length = routeList?.size
        while (counter < length!!) {
            val tupel = routeList[counter]
            val lon = tupel.longitude
            val lat = tupel.latitude
            //adds coordinates to the route
            walkedCoordinatesList.add(Point.fromLngLat(lon, lat))
            counter++
        }
        return walkedCoordinatesList
    }



}