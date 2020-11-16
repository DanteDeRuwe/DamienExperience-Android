package com.example.damiantour.mapBox

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.damiantour.database.DamianDatabase
import com.example.damiantour.database.TupleDatabaseDao
import com.example.damiantour.network.DamianApiService
import com.google.android.gms.location.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * @author Simon Bettens
 */
object LocationUtils{

    // a list of locations
    var _tempLocations = MutableLiveData<MutableList<Tuple>>()
    val tempLocations: LiveData<MutableList<Tuple>>
        get() = _tempLocations

    /**
     * mehod that inits the list
     */
    fun start(){
        _tempLocations.value = ArrayList<Tuple>()
    }

    fun getTempLocationList() :  LiveData<MutableList<Tuple>> {
        return  _tempLocations
    }

    fun postNewLocation(loc : Location?){
        val list = _tempLocations.value!!
        if (loc != null) {
            list.add(Tuple(longitude = loc.longitude,latitude = loc.latitude))
        }
        _tempLocations.postValue( list)
    }

    /**
     * @author Simon
     * clears the temporay list of locations
     */
    private fun resetCurrentTempLocations() {
        println("reset list")
        val list = _tempLocations.value
        if(list!=null) {
            _tempLocations.value!!.clear()
        }
    }

    fun tempLocationsSize(): Int {
        return _tempLocations.value!!.size
    }

}