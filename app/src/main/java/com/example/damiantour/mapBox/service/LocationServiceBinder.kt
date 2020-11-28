package com.example.damiantour.mapBox.service

import android.os.Binder
import com.example.damiantour.mapBox.service.LocationService

/**
 * @author Simon Bettens
 */
class LocationServiceBinder(private val locationService: LocationService) : Binder() {

    fun getService(): LocationService {
        return locationService
    }
}