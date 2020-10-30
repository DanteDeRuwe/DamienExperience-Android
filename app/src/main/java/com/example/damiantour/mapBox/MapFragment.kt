package com.example.damiantour.mapBox

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Looper.getMainLooper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mapbox.android.core.location.*
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
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

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

    //the view
    private lateinit var mapView: MapView
    lateinit var mapViewModel: MapViewModel
    private var locationEngine: LocationEngine? = null

    // every 30 seconds
    private var RECORDTEMPLOCATION_MS = 30000L

    // every 1 minute
    private var RECORDLOCATION_MS = 60000L

    //every 5 minute
    private var WRITELOCATION_MS = 300000L
    private lateinit var permissionsManager: PermissionsManager
    private var timerContinue: Boolean = true;
    private var marker: MarkerView? = null

    //on create fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))
    }

    //on the create view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = root.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        mapViewModel.waypoint.observe(viewLifecycleOwner, Observer { waypoint ->
            if (waypoint != null) {
                addMarkersView(waypoint)
            } else {
                removeMarkersView()
            }
        })
        mapViewModel.tempLocations.observe(viewLifecycleOwner, Observer { templocationList ->
            val last = templocationList.size - 1
            if (last >= 0) {
                val location = templocationList[last];
                println("Temp location : " + location.latitude.toString() + " , " + location.longitude.toString())
            }
        })
        mapViewModel.locations.observe(viewLifecycleOwner, Observer { locationList ->
            val last = locationList.size - 1
            if (last >= 0) {
                val location = locationList[last];
                println("Location : " + location.latitude.toString() + " , " + location.longitude.toString())
            }
        })
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
            removeMarkersView()
            mapViewModel.onClickMap(point)
            true
        }
        markerViewManager = MarkerViewManager(mapView, mapboxMap)
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            addLayer(style)
            addSymbols(style)
            enableLocationComponent(style)
        }
        Log.i("MapFragment", "Going in writeLocation")
        startWriteLocationCoRoutine()
    }

    //adds the layer
    private fun addLayer(style: Style) {
        mapViewModel.addPath()
        // Create the LineString from the list of coordinates and then make a GeoJSON
        // FeatureCollection so we can add the line to our map as a layer.
        style.addSource(
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
        style.addLayer(
            LineLayer("linelayer", "line-source").withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
            )
        )
    }

    private fun addSymbols(style: Style) {
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
                iconOffset(arrayOf(0f, -8f))
            )
        )
    }

    private fun removeMarkersView() {
        if (marker != null) {
            marker.let {
                markerViewManager.removeMarker(it!!)
            }
            marker = null
        }
    }

    private fun addMarkersView(wp: Waypoint) {
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

    private fun startWriteLocationCoRoutine() {
        Log.i("MapFragment", "Before launch")
        GlobalScope.launch {
            Log.i("MapFragment", "In launch")
            writeLocationCoRoutine()
        }
    }

    private suspend fun writeLocationCoRoutine() {
        var time = 1;
        while (timerContinue) {

            //TODO
            //argument for change instead of two times 30 sec, one time 1 min (drop everything with templocation)

            delay(RECORDTEMPLOCATION_MS)
            println("30 sec passed")
            mapViewModel.setCurrentTempLocations()
            delay(RECORDTEMPLOCATION_MS)
            println("1 min passed")
            mapViewModel.setCurrentLocations()
            mapViewModel.setCurrentTempLocations()
            time++
            if (time == 5) {
                //TODO
                println("5 min passed")
                mapViewModel.resetCurrentTempLocations()
                time = 1
            }
        }
    }


    // Gets the icon
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

    // ------------- Permissions
    //activate location
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

    //Requests permission from user
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


    // ------------- life cycle methods
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
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
        mapView.onDestroy()
    }


}