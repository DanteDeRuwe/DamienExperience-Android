package com.example.damiantour.mapBox

import android.app.Application
import androidx.lifecycle.*
import com.example.damiantour.database.TupleDatabaseDao
import com.example.damiantour.findClosestPoint
import com.example.damiantour.network.RouteData
import com.example.damiantour.network.WaypointData
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.json.JSONArray
import org.json.JSONObject

/***
 * @author Simon
 */
class MapViewModel(private val database: TupleDatabaseDao, application: Application) :
    AndroidViewModel(application) {

    //the object
    lateinit var mapBoxMap: MapboxMap
    private var locationUtils: LocationUtils = LocationUtils

    // Important !!!
    //temp property
    //contains jsonObject of waypoints
    private lateinit var coordsObject: JSONArray

    // coords to show the line
    private var _routeCoordinates = MutableLiveData<List<Point>>()
    val routeCoordinates: LiveData<List<Point>>
        get() = _routeCoordinates

    //waypoints on the line
    private var _waypoints = MutableLiveData<List<WaypointData>>()
    val waypoints: LiveData<List<WaypointData>>
        get() = _waypoints

    //selected waypoint
    private var _waypoint = MutableLiveData<WaypointData>()
    val waypoint: LiveData<WaypointData>
        get() = _waypoint

    //List of 30 records (coordstuple every 2 seconds)
    /*
    private var _tempLocations = MutableLiveData<MutableList<Tuple>>()
    val tempLocations: LiveData<MutableList<Tuple>>
        get() = _tempLocations*/

    //List of 30 records (coordstuple every 2 seconds)
    private var _tempLocations = locationUtils.getTempLocationList()
    val tempLocations: LiveData<MutableList<Tuple>>
        get() = _tempLocations

    //1 min locations (6 coordstuples every 1 min)
    private var _locations = database.getAllTuplesLiveData()
    val locations: LiveData<List<Tuple>>
        get() = _locations

    /**
     * @author Simon
     * a method to mix both lists to form a nice line on the map
     * only used to show the users path
     * needs to check if the list of location of every 1 min is initialized and that it has values
     * needs to check if the list of templocation of every 2 sec is initialized and that it has values
     */
    private fun getMixListLocations(): List<Tuple>? {
        val listTempLoc = _tempLocations.value
        val listLoc = _locations.value
        if(listLoc!=null && listLoc.isNotEmpty()){
            println("listLoc : " + listLoc.size)
            return if(listTempLoc!=null && listTempLoc.isNotEmpty()){
                println("extra listTempLoc : " + listTempLoc.size)
                val mix =  listLoc + listTempLoc
                println("mix list : " +  mix.size)
                mix

            } else{
                listLoc
            }
        }else{
            if(listTempLoc!=null && listTempLoc.isNotEmpty()){
                println("listTempLoc : " + listTempLoc.size)
                return listTempLoc
            }
            return ArrayList()
        }
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
        //adds one line on the map
        fun addPath(routeData: RouteData) {
            val routeCoordinatesList = ArrayList<Point>()
            val coordsList = routeData.path.coordinates
            val length = coordsList.size
            var counter = 0
            //Loops over all the coordinates
            while (counter < length) {
                val tupel = coordsList[counter]
                val lon = tupel[0]
                val lat = tupel[1]
                //adds coordinates to the route
                routeCoordinatesList.add(Point.fromLngLat(lon, lat))
                counter++
            }

            addWaypoints(routeData.waypoints)

            //sets the value
            _routeCoordinates.value = routeCoordinatesList
        }

    private fun addWaypoints(waypoints : List<WaypointData>) {
        _waypoints.postValue(waypoints)
    }

    /**
         * @author Simon
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
         * @author Simon
         * is called every minute
         * gets 6 records out of the list  at positions (0,5,10,15,20,25)
         * this is than saved in the database and later on send to the backend
         *
         * needs 6 records to have a nice forming line on the screen (angular and android)
         */
        suspend fun setCurrentLocationList() {
            val tempLocations = _tempLocations.value
            var counter = 0
            while (counter < 30) {
                val tempLoc = tempLocations!![counter]
                val loc = Tuple(longitude = tempLoc.longitude, latitude = tempLoc.latitude)
                database.insert(loc)
                counter += 5
            }
            //resetCurrentTempLocations()
        }

        /**
         * @author Simon
         * clear both the local database
         */
        suspend fun deleteDatabaseLocations() {
            database.clear()
        }

        /**
         * @author Simon
         * clears the temporay list of locations
         */
        fun resetCurrentTempLocations() {
            println("reset list")
            val list = _tempLocations.value
            if (list != null) {
                _tempLocations.value!!.clear()
            }
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