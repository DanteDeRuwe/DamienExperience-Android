package com.example.damiantour.database.repository

import com.example.damiantour.database.dao.LocationDatabaseDao
import com.example.damiantour.database.dao.WaypointDatabaseDao
import com.example.damiantour.mapBox.model.LocationData

class LocationRepository private constructor(private val locationDao: LocationDatabaseDao)
{
    fun getAllLocations() = locationDao.getAllLocations()

    fun getAllLocationsLiveData() = locationDao.getAllLocationsLiveData()

}