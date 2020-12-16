package com.example.damiantour.stopRoute

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.damiantour.R
import com.example.damiantour.calculateWalkedDistance
import com.example.damiantour.database.dao.LocationDatabaseDao
import com.example.damiantour.mapBox.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *@author: Ruben Naudts & Jordy Van Kerkvoorde
 */
class StoppedRouteViewModel(private val locationDatabaseDao: LocationDatabaseDao) : ViewModel() {
    private var averageSpeed : Double = 4.0

    private var _locations = locationDatabaseDao.getAllLocationsLiveData()
    val locations: LiveData<List<LocationData>>
        get() = _locations

    fun getAverageSpeed(): Double {
        return averageSpeed
    }

    /*fun getWalkData(): List<LocationData> {
        return locationDatabaseDao.getAllLocations()
    }*/

    fun getDistance(): Double {
        var distance :Double = 0.0
        GlobalScope.launch(Dispatchers.IO) {
            distance = calculateWalkedDistance(locations.value!!)
        }
        return distance
    }
}