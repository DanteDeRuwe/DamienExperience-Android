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
import java.util.*

/**
 *@author: Ruben Naudts & Jordy Van Kerkvoorde
 */
class StoppedRouteViewModel(private val locationDatabaseDao: LocationDatabaseDao) : ViewModel() {
    private var averageSpeed : Double = 4.0
    private var distance: Double = 0.0
    private var calender : Calendar?=null

    private var _locations = locationDatabaseDao.getAllLocationsLiveData()
    val locations: LiveData<List<LocationData>>
        get() = _locations

    fun getAverageSpeed(): Double {
        return averageSpeed
    }
    fun setCalender(c : Calendar){
        calender = c
    }
    fun getCalender():Calendar{
        if(calender==null){
            setCalender(Calendar.getInstance())
        }
        return calender!!
    }
    fun getDistance(): Double {
        return distance
    }
    fun setDistance(dist : Double){
        distance = dist
    }

    suspend fun clearDataLocation(){
        locationDatabaseDao.clear()
    }

    fun calculateDistance(list: List<LocationData>): Double{
        distance = calculateWalkedDistance(list)
        return distance
    }

    fun calculateSpeed(starttime: Long): Double{
        val endtime = getCalender().timeInMillis

        val endtimeInMinutes = (endtime - starttime) / 60000
        val speed = (distance /endtimeInMinutes)*60

        return speed
    }
}