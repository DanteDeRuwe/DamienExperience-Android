package com.example.damiantour

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.json.JSONObject
import java.io.InputStream
import java.nio.channels.FileChannel.open

/// Documentation

//location :
// https://docs.mapbox.com/android/navigation/overview/device-location/
// https://docs.mapbox.com/android/maps/examples/show-a-users-location/
// https://docs.mapbox.com/android/maps/examples/create-a-line-layer/
// https://docs.mapbox.com/android/java/examples/directions-gradient-line/
// https://docs.mapbox.com/android/java/overview/geojson/#geometries
// https://docs.mapbox.com/android/java/examples/show-directions-on-a-map/

///
class MapFragment : Fragment(), PermissionsListener, OnMapReadyCallback {

    //the view
    private lateinit var mapView: MapView

    //the object
    private lateinit var mapBoxMap: MapboxMap

    private lateinit var permissionsManager: PermissionsManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("Fragment" ,"Goes in onCreateView")
        val root = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = root.findViewById(R.id.mapView)

        mapView.onCreate(savedInstanceState)
        Log.i("Fragment" ,"getMapAsync")
        mapView.getMapAsync(this)

        return root

    }

    /**
     * Called when the map is ready to be used.
     *
     * @param mapboxMap An instance of MapboxMap associated with the [MapFragment] or
     * [MapView] that defines the callback.
     */
    override fun onMapReady(mapboxMap: MapboxMap) {
        Log.i("Fragment" ,"Goes in onMapReady")
        this.mapBoxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->

            // initRouteCoordinates()
            val obj = JSONObject(readJSONFromAsset())
//            println( ( obj.getJSONArray("features").getJSONObject(0)
//                .getJSONObject("geometry").getJSONArray("coordinates")
//                    ))

            //Get coords from json object, returns jsonarray
            //in the form of [[long,lat],[long,lat], ... , [long,lat]]
            val coordsObject = obj.getJSONArray("features").getJSONObject(0)
                .getJSONObject("geometry").getJSONArray("coordinates")

            val length = coordsObject.length()
            val routeCoordinates = ArrayList<Point>()
            var counter = 0
            //Loops over all the coordinates
            while (counter < length) {
                val tupel = coordsObject.getJSONArray(counter)

                val lon = tupel.get(0) as Double
                val lat = tupel.get(1) as Double
                println(lon)
                //adds coordinates to the route
                routeCoordinates.add(Point.fromLngLat(lon, lat))
                counter++
            }

            // Create the LineString from the list of coordinates and then make a GeoJSON
            // FeatureCollection so we can add the line to our map as a layer.
            style.addSource(
                GeoJsonSource(
                    "line-source",
                    FeatureCollection.fromFeatures(
                        arrayOf(
                            Feature.fromGeometry(
                                LineString.fromLngLats(routeCoordinates)
                            )
                        )
                    )
                )
            )

            // The layer properties for our line. This is where we make the line dotted, set the
            // color, etc.
            style.addLayer(
                LineLayer("linelayer", "line-source").withProperties(
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineWidth(5f),
                    PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
                )
            )
            enableLocationComponent(style)
        }
    }
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
            mapBoxMap.locationComponent.apply {

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
        Toast.makeText(requireContext(), R.string.user_location_permission_explanation, Toast.LENGTH_LONG)
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapBoxMap.style!!)
        } else {
            Toast.makeText(requireContext(), R.string.user_location_permission_not_granted, Toast.LENGTH_LONG)
                .show()
            activity?.finish()
        }
    }
    //Test method
    //Gets the JSON file from assets
    fun readJSONFromAsset(): String {
        val file_name = "testroute.geojson"
        val json :String
        try {
            val assets = activity?.assets
            if (assets != null)
            {
            val inputStream: InputStream =  assets.open(file_name)
                json = inputStream.bufferedReader().use { it.readText() }
            } else {
                throw Exception("Cannot get assets")
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            return ""
        }
        return json
    }
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