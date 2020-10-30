package com.example.damiantour.mapBox

import android.location.Location
import android.widget.Toast
import androidx.annotation.NonNull
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import java.lang.ref.WeakReference

private class LocationChangeListeningFragmentLocationCallback internal constructor(mapFragment: MapFragment?) :
    LocationEngineCallback<LocationEngineResult?> {
    private val mapFragmentWeakReference = WeakReference<MapFragment>(mapFragment)

    /**
     * The LocationEngineCallback interface's method which fires when the device's location has changed.
     *
     * @param result the LocationEngineResult object which has the last known location within it.
     */
    override fun onSuccess(result: LocationEngineResult?) {
        val fragment: MapFragment? = mapFragmentWeakReference.get()
        if (fragment != null) {
            val location: Location = result?.lastLocation ?: return

            // Create a Toast which displays the new location's coordinates

            print(location.latitude.toString() +" , "+ location.longitude.toString())
            if(location != null) {
                Toast.makeText(
                    fragment.context, String.format(
                        "test = ",
                        java.lang.String.valueOf(location.latitude),
                        java.lang.String.valueOf(location.longitude)
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Pass the new location to the Maps SDK's LocationComponent
            if (result.lastLocation != null) {
                fragment.mapViewModel.mapBoxMap.locationComponent
                    .forceLocationUpdate(result.lastLocation)
            }
        }
    }

    /**
     * The LocationEngineCallback interface's method which fires when the device's location can't be captured
     *
     * @param exception the exception message
     */
    override fun onFailure(@NonNull exception: Exception) {
        val fragment: MapFragment? = mapFragmentWeakReference.get()
        if (fragment != null) {
            Toast.makeText(
                fragment.context, exception.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}