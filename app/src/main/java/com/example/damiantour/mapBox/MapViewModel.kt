package com.example.damiantour.mapBox

import android.app.Application
import android.location.Location
import androidx.lifecycle.*
import com.example.damiantour.database.TupleDatabaseDao
import com.example.damiantour.findClosestPoint
import com.example.damiantour.network.RouteData
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections.addAll

/***
 * @author Simon
 */
class MapViewModel(private val database: TupleDatabaseDao, application: Application) :
    AndroidViewModel(application) {


    //the object
    lateinit var mapBoxMap: MapboxMap

    // Important !!!
    //temp property
    //contains jsonObject of waypoints
    private lateinit var coordsObject: JSONArray

    // coords to show the line
    private var _routeCoordinates = MutableLiveData<List<Point>>()
    val routeCoordinates: LiveData<List<Point>>
        get() = _routeCoordinates

    //waypoints on the line
    private var _waypoints = MutableLiveData<List<Waypoint>>()
    val waypoints: LiveData<List<Waypoint>>
        get() = _waypoints

    //selected waypoint
    private var _waypoint = MutableLiveData<Waypoint>()
    val waypoint: LiveData<Waypoint>
        get() = _waypoint

    //List of 30 records (coordstuple every 2 seconds)
    private var _tempLocations = MutableLiveData<MutableList<Tuple>>()
    val tempLocations: LiveData<MutableList<Tuple>>
        get() = _tempLocations

    //1 min locations (6 coordstuples every 1 min)
    private var _locations = database.getAllTuples()
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
        _tempLocations.value = ArrayList<Tuple>()
    }

    //adds one line on the map
    fun addPath(routeData : RouteData) {
        val routeCoordinatesList = ArrayList<Point>()
        val coordsList = routeData.coordinates
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
        //sets the value
        _routeCoordinates.value = routeCoordinatesList
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
     * adds coordstuple every 2 seconds to list
     * must check is location is enabled (if not it must wait until it is)
     * gets that location and adds it to the list
     *
     * this is not saved in the local database or will not be send to the backend (this is temporay data)
     * will be clear every minute
     */
    fun setCurrentTempLocation() {
        var isActivated = mapBoxMap.locationComponent.isLocationComponentActivated
        while (!isActivated) {
            println("Wachten op activation van locationComponent")
            isActivated = mapBoxMap.locationComponent.isLocationComponentActivated
        }
        val location: Location? = mapBoxMap.locationComponent.lastKnownLocation
        if (location != null) {
            println("Plaatst een coordstuple op de lijst")
            val list = _tempLocations.value!!
            list.add(Tuple(longitude = location.longitude,latitude = location.latitude))
            _tempLocations.postValue( list)
        } else {
            println("Locatie nog niet gevonden (locatie component)")
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
        resetCurrentTempLocations()
    }

    /**
     * @author Simon
     * clear both the local database
     */
    suspend fun deleteLocations(){
        database.clear()
    }

    /**
     * @author Simon
     * clears the temporay list of locations
     */
    private fun resetCurrentTempLocations() {
        println("reset list")
        val list = _tempLocations.value
        if(list!=null) {
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

    // Important !!!!
    // temp method
    fun readWaypointFile() {
        val obj = JSONObject(getWaypoints())
        coordsObject = obj.getJSONArray("features")
        _listSize.value = coordsObject.length()
        getWaypointData()
    }

    // Important !!!!
    //temp method
    //gets the data in the jsonarray
    private fun getWaypointData() {
        val waypointsList = ArrayList<Waypoint>()
        var counter = 0
        while (counter < listSize.value!!) {
            val waypointObject = coordsObject.getJSONObject(counter)

            val title = waypointObject.get("title") as String
            val description = waypointObject.get("description") as String
            //get coords
            val tupel = waypointObject.getJSONObject("coordinates")
            val lon = tupel.get("longitude") as Double
            val lat = tupel.get("latitude") as Double
            val wp = Waypoint(title, description, lon, lat)
            waypointsList.add(wp)
            counter++
        }
        _waypoints.value = waypointsList
    }

    private fun getWaypoints(): String {
        return "{\n" +
                "\"features\" : [\n" +
                "      {\n" +
                "        \"title\": \"Delhaize Tremelo\",\n" +
                "        \"description\": \"Gratis blikje frisdrank en snoepreep bij vertoon van een geldige coupon.\",\n" +
                "        \"coordinates\": {\n" +
                "          \"longitude\": 4.705204,\n" +
                "          \"latitude\": 50.990410\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"title\": \"Bevoorrading 1\",\n" +
                "        \"description\": \"Hier krijg je een appeltje voor de dorst!\",\n" +
                "        \"coordinates\": {\n" +
                "          \"longitude\": 4.708955,\n" +
                "          \"latitude\": 50.994423\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"title\": \"Voetbalveld\",\n" +
                "        \"description\": \"Wachtpost rode kruis\",\n" +
                "        \"coordinates\": {\n" +
                "          \"longitude\": 4.702002,\n" +
                "          \"latitude\": 50.986546\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}"
    }


}