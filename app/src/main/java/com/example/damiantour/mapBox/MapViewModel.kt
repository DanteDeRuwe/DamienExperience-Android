package com.example.damiantour.mapBox

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damiantour.findClosestPoint
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
/***
 * @author Simon
 */
class MapViewModel : ViewModel() {

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

    //30 second locations
    private var _tempLocations = MutableLiveData<List<Tuple>>()
    val tempLocations: LiveData<List<Tuple>>
        get() = _tempLocations

    //1 min locations
    private var _locations = MutableLiveData<List<Tuple>>()
    val locations: LiveData<List<Tuple>>
        get() = _locations

    //size of the list
    private var _listSize = MutableLiveData<Int>()
    val listSize: LiveData<Int>
        get() = _listSize

    //kinda constructor  but not really
    init {
        _listSize.value = 0
        _locations.value = ArrayList<Tuple>();
        _tempLocations.value = ArrayList<Tuple>();
    }

    //adds one line on the map
    fun addPath() {
        // initRouteCoordinates()
        val routeCoordinatesList = ArrayList<Point>()
        val obj = JSONObject(getRoute())
        //Get coords from json object, returns jsonarray
        //in the form of [[long,lat],[long,lat], ... , [long,lat]]
        val coordsObject = obj.getJSONArray("features").getJSONObject(0)
            .getJSONObject("geometry").getJSONArray("coordinates")
        val length = coordsObject.length()
        var counter = 0
        //Loops over all the coordinates
        while (counter < length) {
            val tupel = coordsObject.getJSONArray(counter)
            val lon = tupel.get(0) as Double
            val lat = tupel.get(1) as Double
            //adds coordinates to the route
            routeCoordinatesList.add(Point.fromLngLat(lon, lat))
            counter++
        }
        //sets the value
        _routeCoordinates.value = routeCoordinatesList
    }

    //tries to get select the closest Waypoint from the selected point
    //if the every waypoint is further than 500m the method returns 'null' (in  method findClosestPoint(clickPoint, list))
    //if the closest waypoint is the same as the selected waypoint,the selected waypoint will be deselected
    fun onClickMap(latLng: LatLng) {
        val clickPoint = Point.fromLngLat(latLng.longitude, latLng.latitude)
        val lastwp = _waypoint.value
        val list = _waypoints.value
        if (list != null) {
            val wp = findClosestPoint(clickPoint, list)
            if(lastwp != null && wp != null){
                if (wp.longitude == lastwp.longitude && wp.latitude == lastwp.latitude){
                    _waypoint.value = null
                    return
                }
            }
            _waypoint.value = wp
        }

    }

    fun setCurrentTempLocations(){
        val location : Location? = mapBoxMap.locationComponent.lastKnownLocation
        if(location != null){
            println("Doet iets ... ")
            _tempLocations.postValue(_tempLocations.value?.plus(Tuple(location.longitude,location.latitude)))
        }else{
            println("Grote fout \n \n \n woeps")
        }
    }
    fun setCurrentLocations(){
        val location : Location? = mapBoxMap.locationComponent.lastKnownLocation
        if(location != null){
            println("Doet iets ... ")
            _locations.postValue(_locations.value?.plus(Tuple(location.longitude,location.latitude)))
        }else{
            println("Grote fout \n \n \n woeps")
        }
    }

    fun resetCurrentTempLocations(){
        println("reset list")
        _tempLocations.postValue(ArrayList<Tuple>())
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

    // dummy data
    private fun getRoute(): String {
        return "{\n" +
                "    \"type\": \"FeatureCollection\"," +
                "    \"features\": [" +
                "      {" +
                "        \"type\": \"Feature\"," +
                "        \"properties\": {}," +
                "        \"geometry\": {" +
                "          \"type\": \"LineString\"," +
                "          \"coordinates\": [" +
                "            [" +
                "              4.708843231201172," +
                "              50.9944865168635" +
                "            ]," +
                "            [" +
                "              4.703693389892578," +
                "              50.99108304254769" +
                "            ]," +
                "            [" +
                "              4.705195426940918," +
                "              50.99017810856411" +
                "            ],\n" +
                "            [\n" +
                "              4.70386505126953," +
                "              50.989367704946844" +
                "            ]," +
                "            [" +
                "              4.702770709991455," +
                "              50.989772908524195" +
                "            ]," +
                "            [" +
                "              4.702212810516357," +
                "              50.9889895117497" +
                "            ]," +
                "            [" +
                "              4.7017621994018555," +
                "              50.984288853430854" +
                "            ]," +
                "            [" +
                "              4.702920913696289," +
                "              50.985342490650396" +
                "            ]," +
                "            [" +
                "              4.7046589851379395,\n" +
                "              50.986423119367196\n" +
                "            ],\n" +
                "            [\n" +
                "              4.705259799957275,\n" +
                "              50.98732812656122\n" +
                "            ],\n" +
                "            [\n" +
                "              4.705452919006348,\n" +
                "              50.988098044163905\n" +
                "            ],\n" +
                "            [\n" +
                "              4.705753326416016,\n" +
                "              50.988935483898565\n" +
                "            ],\n" +
                "            [\n" +
                "              4.70639705657959,\n" +
                "              50.98958381396164\n" +
                "            ],\n" +
                "            [\n" +
                "              4.707319736480713,\n" +
                "              50.98947575958004\n" +
                "            ],\n" +
                "            [\n" +
                "              4.711203575134277,\n" +
                "              50.988017000807105\n" +
                "            ],\n" +
                "            [\n" +
                "              4.7126305103302,\n" +
                "              50.987578013498705\n" +
                "            ],\n" +
                "            [\n" +
                "              4.714068174362183,\n" +
                "              50.98703096196555\n" +
                "            ],\n" +
                "            [\n" +
                "              4.714701175689697,\n" +
                "              50.98706473076542\n" +
                "            ],\n" +
                "            [\n" +
                "              4.715237617492676,\n" +
                "              50.987517230313394\n" +
                "            ],\n" +
                "            [\n" +
                "              4.715688228607178,\n" +
                "              50.98775360892028\n" +
                "            ],\n" +
                "            [\n" +
                "              4.7151947021484375,\n" +
                "              50.98873287889668\n" +
                "            ],\n" +
                "            [\n" +
                "              4.714980125427246,\n" +
                "              50.988962497832\n" +
                "            ],\n" +
                "            [\n" +
                "              4.7147440910339355,\n" +
                "              50.98909756726333\n" +
                "            ],\n" +
                "            [\n" +
                "              4.714454412460327,\n" +
                "              50.98963784105814\n" +
                "            ],\n" +
                "            [\n" +
                "              4.713563919067383,\n" +
                "              50.98965810120312\n" +
                "            ],\n" +
                "            [\n" +
                "              4.712855815887451,\n" +
                "              50.98979992197026\n" +
                "            ],\n" +
                "            [\n" +
                "              4.7122979164123535,\n" +
                "              50.98987420886582\n" +
                "            ],\n" +
                "            [\n" +
                "              4.711611270904541,\n" +
                "              50.989813428687384\n" +
                "            ],\n" +
                "            [\n" +
                "              4.711031913757324,\n" +
                "              50.989712128213114\n" +
                "            ],\n" +
                "            [\n" +
                "              4.710677862167358,\n" +
                "              50.98963108767451\n" +
                "            ],\n" +
                "            [\n" +
                "              4.7100770473480225,\n" +
                "              50.989691868091725\n" +
                "            ],\n" +
                "            [\n" +
                "              4.709744453430176,\n" +
                "              50.98975264842937\n" +
                "            ],\n" +
                "            [\n" +
                "              4.709347486495972,\n" +
                "              50.98975264842937\n" +
                "            ],\n" +
                "            [\n" +
                "              4.708961248397826,\n" +
                "              50.98973238832567\n" +
                "            ],\n" +
                "            [\n" +
                "              4.708864688873291,\n" +
                "              50.99002953562688\n" +
                "            ],\n" +
                "            [\n" +
                "              4.708617925643921,\n" +
                "              50.99038746053207\n" +
                "            ],\n" +
                "            [\n" +
                "              4.708296060562134,\n" +
                "              50.99072512300668\n" +
                "            ],\n" +
                "            [\n" +
                "              4.7079527378082275,\n" +
                "              50.99092096611637\n" +
                "            ],\n" +
                "            [\n" +
                "              4.70739483833313,\n" +
                "              50.9911235615671\n" +
                "            ],\n" +
                "            [\n" +
                "              4.708349704742432,\n" +
                "              50.99227833874959\n" +
                "            ],\n" +
                "            [\n" +
                "              4.709476232528686,\n" +
                "              50.99192042842648\n" +
                "            ]," +
                "            [" +
                "              4.709728360176086," +
                "              50.992065618795955" +
                "            ]," +
                "            [" +
                "              4.710431098937988," +
                "              50.99198795888739" +
                "            ]," +
                "            [" +
                "              4.711015820503235," +
                "              50.991809002951186" +
                "            ]," +
                "            [" +
                "              4.711251854896545," +
                "              50.992041983185366" +
                "            ]," +
                "            [" +
                "              4.712952375411987," +
                "              50.9918866631592" +
                "            ]," +
                "            [" +
                "              4.713091850280762," +
                "              50.99176510799351" +
                "            ]," +
                "            [" +
                "              4.7132956981658936," +
                "              50.991724589534215" +
                "            ]," +
                "            [" +
                "              4.7137463092803955," +
                "              50.991738095691225" +
                "            ]," +
                "            [" +
                "              4.713928699493408," +
                "              50.99183939174371" +
                "            ]," +
                "            [" +
                "              4.7141969203948975," +
                "              50.99180562641744" +
                "            ]," +
                "            [" +
                "              4.7149693965911865," +
                "              50.991947440622646" +
                "            ]," +
                "            [" +
                "              4.7154951095581055," +
                "              50.99200146496779" +
                "            ]," +
                "            [" +
                "              4.71637487411499," +
                "              50.99187991010278" +
                "            ]," +
                "            [" +
                "              4.716482162475586," +
                "              50.99444600078013" +
                "            ]," +
                "            [" +
                "              4.708929061889648," +
                "              50.99450002221675" +
                "            ]," +
                "            [" +
                "              4.7088465839624405," +
                "              50.99448820503287" +
                "            ]," +
                "            [" +
                "              4.708844237029552," +
                "              50.99448546175759" +
                "            ]" +
                "          ]" +
                "        }" +
                "      }" +
                "    ]" +
                "  }"
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