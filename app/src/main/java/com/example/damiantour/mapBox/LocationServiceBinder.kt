package com.example.damiantour.mapBox

import android.os.Binder

/**
 * @author Simon Bettens
 */
class LocationServiceBinder(private val locationService: LocationService) : Binder() {

    fun getService(): LocationService {
        return locationService
    }
}