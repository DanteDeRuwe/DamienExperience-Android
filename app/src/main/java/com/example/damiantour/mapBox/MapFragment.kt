package com.example.damiantour.mapBox

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.R
import com.example.damiantour.database.DamianDatabase
import com.example.damiantour.network.DamianApiService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

/// Documentation

//location :
// https://docs.mapbox.com/android/navigation/overview/device-location/
// https://docs.mapbox.com/android/maps/examples/show-a-users-location/
// https://docs.mapbox.com/android/maps/examples/create-a-line-layer/
// https://docs.mapbox.com/android/java/examples/directions-gradient-line/
// https://docs.mapbox.com/android/java/overview/geojson/#geometries
// https://docs.mapbox.com/android/java/examples/show-directions-on-a-map/

/**
 * @author  Jonas and Simon
 *
 */
//Needs refactoring and extracting of methodes to
class MapFragment : Fragment(), PermissionsListener, OnMapReadyCallback {
    private lateinit var markerViewManager: MarkerViewManager
    private var coroutinesActive by Delegates.notNull<Boolean>()

    //the view
    private lateinit var mapView: MapView
    private lateinit var mapViewModel: MapViewModel
    private var locationEngine: LocationEngine? = null

    // every 2 seconds
    private var RECORDTEMPLOCATION_MS = 2000L

    // every 5 minutes
    private var SENDLOCATIONS = 5
    private var WANTSTOSENDLOCATION = true

    private lateinit var permissionsManager: PermissionsManager
    private var timerContinue: Boolean = true
    private var isInBackground : Boolean = false
    private var marker: MarkerView? = null

    private lateinit var _style: Style

    private val apiService: DamianApiService = DamianApiService.create()

    /**
     * @author Simon Bettens & Jonas Haenbalcke & Ruben Naudts & Jordy Van Kerkvoorde
     * on create fragment
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))
        coroutinesActive = false
    }

    /**
     * @author Simon Bettens & Jonas Haenbalcke
     * Creates the view and prepares the fragment and viewmodel
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //get viewmodel from factory
        val application = requireNotNull(this.activity).application
        val dataSource = DamianDatabase.getInstance(application).tupleDatabaseDao
        val viewModelFactory = MapViewModelFactory(dataSource, application)
        mapViewModel = ViewModelProviders.of(this, viewModelFactory).get(MapViewModel::class.java)
        // inflate view
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        //get map
        mapView = root.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        /***
         * Observers
         */
        //shows or hides the waypoints
        mapViewModel.waypoint.observe(viewLifecycleOwner, Observer { waypoint ->
            if (waypoint != null) {
                addMarkersOnMap(waypoint)
            } else {
                removeMarkersOnMap()
            }
        })
        //draws or redraws
        mapViewModel.tempLocations.observe(viewLifecycleOwner, Observer { templocationList ->
            val last = templocationList.size - 1
            if (last >= 0) {
                val location = templocationList[last]
                println("Temp location : " + location.latitude.toString() + " , " + location.longitude.toString())
                drawWalkedLine()
            }
        })
        mapViewModel.locations.observe(viewLifecycleOwner, Observer { locationList ->
            drawWalkedLine()
        })

        /**
         * Stop button observer
         */
        val stopbutton = root.findViewById<Button>(R.id.stop_button)
        stopbutton.setOnClickListener{
           stopTour()
        }


        /***
         * Navigation
         */
        val bottomNavigationView: BottomNavigationView = root.findViewById(R.id.nav_bar)
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)
        return root
    }

    /**
     * Called when the map is ready to be used.
     *
     * @param mapboxMap An instance of MapboxMap associated with the [MapFragment] or
     * [MapView] that defines the callback.
     */
    override fun onMapReady(mapboxMap: MapboxMap) {
        mapViewModel.mapBoxMap = mapboxMap
        mapboxMap.addOnMapClickListener { point ->
            removeMarkersOnMap()
            mapViewModel.onClickMap(point)
            true
        }
        markerViewManager = MarkerViewManager(mapView, mapboxMap)
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            _style = style
            drawWaypointSymbols(style)
            enableLocationComponent(style)
        }
        if (!coroutinesActive) {
            coroutinesActive = true
            println("cocourtine gestart")
            startWriteLocationCoRoutine()
        }
    }

    private suspend fun sendRouteRequest() {
        try {
            val preferences: SharedPreferences =
                requireActivity().getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
            val JWTtoken: String = preferences.getString("TOKEN", null).toString()
            val routeResult = apiService.getRoute(JWTtoken, "TestTourOne")
            mapViewModel.addPath(routeResult)
        } catch (e: Exception) {
            println("Fout" + e.localizedMessage)
        }
    }

    /**
     * @author Simon Bettens & Jonas Haenbalcke
     * draw the Route on the map
     */
    private fun drawRouteLayer() {
        // Create the LineString from the list of coordinates and then make a GeoJSON
        // FeatureCollection so we can add the line to our map as a layer.
        if (this::_style.isInitialized) {
            _style.addSource(
                GeoJsonSource(
                    "line-source",
                    FeatureCollection.fromFeatures(
                        arrayOf(
                            Feature.fromGeometry(
                                mapViewModel.routeCoordinates.value?.let { LineString.fromLngLats(it) }
                            )
                        )
                    )
                )
            )
            // adds styling to the line connecting the coordstuppels
            _style.addLayer(
                LineLayer("linelayer", "line-source").withProperties(
                    lineCap(Property.LINE_CAP_ROUND),
                    lineJoin(Property.LINE_JOIN_ROUND),
                    lineWidth(5f),
                    lineColor(Color.parseColor("#ff0040")),
                    lineSortKey(5f)
                )
            )
        }
    }

    /**
     * @author Simon Bettens & Jonas Haenbalcke
     * draws the waypoints symbol on the map
     */
    private fun drawWaypointSymbols(style: Style) {
        val symbolLayerIconFeatureList = ArrayList<Feature>()
        //this call reads the json and put everthing in een arraylist<Waypoint>
        mapViewModel.readWaypointFile()
        var counter = 0
        //Must be a int
        val listSize = mapViewModel.listSize.value
        //Loops over all the coordinates
        while (counter < listSize!!) {
            //get properties
            val wp = mapViewModel.waypoints.value?.get(counter)
            if (wp != null) {
                //get coords
                val lon: Double = wp.longitude
                val lat: Double = wp.latitude
                val feature = Feature.fromGeometry(
                    Point.fromLngLat(lon, lat)
                )
                symbolLayerIconFeatureList.add(feature)
            }
            counter++
        }
        //add every point to the map
        style.addSource(
            GeoJsonSource(
                "ICONS",
                FeatureCollection.fromFeatures(symbolLayerIconFeatureList)
            )
        )
        // gets the icon
        val icon = drawableToBitmap(getDrawable(requireContext(), R.drawable.ic_map_marker)!!)
        // adds the image to the style
        style.addImage("map_marker", icon, false)
        //adds the icon to the map at every coordstupel
        style.addLayer(
            SymbolLayer("SYMBOL_LAYER_ID", "ICONS").withProperties(
                iconImage("map_marker"),
                iconOffset(arrayOf(0f, -8f)),
                symbolSortKey(5f)
            )
        )
    }

    /**
     * @author Simon Bettens
     * draws the line where the user has walked
     */
    private fun drawWalkedLine() {
        if(!isInBackground) {
            val walkedCoordinatesList = mapViewModel.createLineSourceFromWalkedRoute()
            if (this::_style.isInitialized) {
                try {
                    _style.removeLayer("walkedlinelayer")
                    _style.removeSource("walkedline-source")
                    _style.addSource(
                        GeoJsonSource(
                            "walkedline-source",
                            FeatureCollection.fromFeatures(
                                arrayOf(
                                    Feature.fromGeometry(
                                        walkedCoordinatesList.let { LineString.fromLngLats(it) }
                                    )
                                )
                            )
                        )
                    )
                    // adds styling to the line connecting the coordstuppels
                    _style.addLayer(
                        LineLayer("walkedlinelayer", "walkedline-source").withProperties(
                            lineCap(Property.LINE_CAP_ROUND),
                            lineJoin(Property.LINE_JOIN_ROUND),
                            lineWidth(6f),
                            lineColor(Color.parseColor("#3bb7a9")),
                            lineSortKey(3f)
                        )
                    )
                }catch (e: Exception){
                    println("Exception throw : " + e.localizedMessage)
                }
            }
        }

    }

    /**
     * @author Simon Bettens
     * removes the marker overlay currently on the map
     */
    private fun removeMarkersOnMap() {
        if (marker != null) {
            marker.let {
                markerViewManager.removeMarker(it!!)
            }
            marker = null
        }
    }

    /**
     * @author Simon Bettens
     * adds the markers overlay of the closest waypoint from where the user has ticked
     */
    private fun addMarkersOnMap(wp: Waypoint) {
        val title: String = wp.title
        val description: String = wp.description
        //get coords
        val lon: Double = wp.longitude
        val lat: Double = wp.latitude
        //get and make custom view for markers
        val customView: View = LayoutInflater.from(context).inflate(
            R.layout.marker_view_bubble, null
        )
        customView.layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        // title for the view
        val titleTextView: TextView = customView.findViewById(R.id.marker_window_title)
        titleTextView.text = title
        // description for the view
        val snippetTextView: TextView = customView.findViewById(R.id.marker_window_snippet)
        snippetTextView.text = description
        // make the view
        marker = MarkerView(LatLng(lat, lon), customView)
        //add it to the map
        marker.let {
            if (it != null) {
                markerViewManager.addMarker(it)
            }
        }
    }

    /**
     * @author Simon Bettens
     * start the coroutine
     */
    private fun startWriteLocationCoRoutine() {
        lifecycleScope.launch {
            sendRouteRequest()
            drawRouteLayer()
            writeLocationCoRoutine()
        }
    }

    /**
     * @author Simon Bettens
     * this runs on a separate thread
     * gets location every 2 seconds and places it on the list
     * every 1 min 6 locations will be placed in the database and the list is cleared
     * SENDLOCATIONS specifies the amount of the before the locations is send to the backend
     */
    private suspend fun writeLocationCoRoutine() {
        var time = 1
        // should not happen here
        mapViewModel.deleteLocations()
        while (timerContinue) {
            var counter = 0
            while (counter <= 60) {
                println("How many secs passed : $counter")
                delay(RECORDTEMPLOCATION_MS)
                mapViewModel.setCurrentTempLocation()
                counter += 2
            }
            println("1 min passed")
            mapViewModel.setCurrentLocationList()
            time += 1
            if (WANTSTOSENDLOCATION && time == SENDLOCATIONS) {
                //TODO
                println("5 min passed")
                //mapViewModel.resetCurrentTempLocations()
                println("API POST")
                time = 1
            }
        }
    }


    /**
     * @author Simon & Jonas
     * Gets the icon for the marker
     */
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    /**
     * enables location component
     */
    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(requireContext())
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(requireContext(), R.color.mapbox_blue))
                .build()
            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle)
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()
            // Get an instance of the LocationComponent and then adjust its settings
            mapViewModel.mapBoxMap.locationComponent.apply {
                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)
                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true
                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING
                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(activity)
        }
    }

    /**
     * Requests permission from user
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(
            requireContext(),
            R.string.user_location_permission_explanation,
            Toast.LENGTH_LONG
        )
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapViewModel.mapBoxMap.style!!)
        } else {
            Toast.makeText(
                requireContext(),
                R.string.user_location_permission_not_granted,
                Toast.LENGTH_LONG
            )
                .show()
            activity?.finish()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        isInBackground = true
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        isInBackground = false

        mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        markerViewManager.onDestroy()
        mapView.onDestroy()
        coroutinesActive = false
    }

    /**
     * @author: Ruben Naudts & Jordy Van Kerkvoorde
     * Shows confirm dialog when user presses stop tour button
     */
    fun stopTour(){
        AlertDialog.Builder(context)
            .setTitle(R.string.stop_dialog_title)
            .setMessage(R.string.stop_dialog_message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.yes, DialogInterface.OnClickListener { dialog, id ->
                // FIRE ZE MISSILES!
                stopTourConfirmed()
            })
            .setNegativeButton(R.string.no, null).show()
    }

    fun stopTourConfirmed(){
        //TODO : stop tour afwerken...
    }
}