package com.example.damiantour

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import org.jetbrains.annotations.NotNull
import org.json.JSONObject
import java.io.InputStream
import java.net.URISyntaxException


/// Documentation

//location :
// https://docs.mapbox.com/android/navigation/overview/device-location/
// https://docs.mapbox.com/android/maps/examples/show-a-users-location/
// https://docs.mapbox.com/android/maps/examples/create-a-line-layer/
// https://docs.mapbox.com/android/java/examples/directions-gradient-line/
// https://docs.mapbox.com/android/java/overview/geojson/#geometries
// https://docs.mapbox.com/android/java/examples/show-directions-on-a-map/

///
class MainActivity : AppCompatActivity(), PermissionsListener, OnMapReadyCallback {
    //the view
    private lateinit var mapView: MapView

    //the object
    private lateinit var mapboxMap: MapboxMap

    //permissions
    private lateinit var permissionsManager: PermissionsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_access_token))
        //komt altijd na getInstance
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

    }


    //Test method
    //Gets the JSON file from assets
    fun readJSONFromAsset(): String? {
        val file_name = "testroute.geojson"
        var json: String? = null
        try {
            val inputStream: InputStream = assets.open(file_name)
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->

            // initRouteCoordinates()
            var obj = JSONObject(readJSONFromAsset())
//            println( ( obj.getJSONArray("features").getJSONObject(0)
//                .getJSONObject("geometry").getJSONArray("coordinates")
//                    ))

            //Get coords from json object, returns jsonarray
            //in the form of [[long,lat],[long,lat], ... , [long,lat]]
            val coordsObject = obj.getJSONArray("features").getJSONObject(0)
                .getJSONObject("geometry").getJSONArray("coordinates")

            val length = coordsObject.length()
            val routeCoordinates = ArrayList<Point>();
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
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.mapbox_blue))
                .build()

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {

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
            permissionsManager.requestLocationPermissions(this)
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
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG)
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}