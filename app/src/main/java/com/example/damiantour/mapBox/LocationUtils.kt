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
    //the api
    private val apiService: DamianApiService = DamianApiService.create()
    var dataSource : TupleDatabaseDao? = null
    //the location provider
    var fusedLocationProviderClient: FusedLocationProviderClient?= null
    //the location request
    var locationRequest: LocationRequest?=null
    //callback that is called on locationchange
    var locationCallback: LocationCallback?= null
    // a list of locations
    var _tempLocations = MutableLiveData<MutableList<Tuple>>()
    val tempLocations: LiveData<MutableList<Tuple>>
        get() = _tempLocations

    /**
     * mehod that inits all properties on first start else does nothing
     */
    @SuppressLint("MissingPermission")
    fun start(context :Context){
        if(dataSource==null) {
            GlobalScope.launch {
                dataSource = DamianDatabase.getInstance(context).tupleDatabaseDao
                dataSource?.clear()
            }
        }
        if(fusedLocationProviderClient == null) {
            _tempLocations.value = ArrayList<Tuple>()
            fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)
        }
        if(locationRequest == null){
            locationRequest = LocationRequest().apply {
                // Sets the desired interval for active location updates. This interval is inexact. You
                // may not receive updates at all if no location sources are available, or you may
                // receive them less frequently than requested. You may also receive updates more
                // frequently than requested if other applications are requesting location at a more
                // frequent interval.
                //
                // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
                // targetSdkVersion) may receive updates less frequently than this interval when the app
                // is no longer in the foreground.
                interval = TimeUnit.SECONDS.toMillis(2)

                // Sets the fastest rate for active location updates. This interval is exact, and your
                // application will never receive updates more frequently than this value.
                fastestInterval = TimeUnit.SECONDS.toMillis(1)

                // Sets the maximum time when batched location updates are delivered. Updates may be
                // delivered sooner than this interval.
                maxWaitTime = TimeUnit.MINUTES.toMillis(1)

                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            }
        }
        if(locationCallback == null){
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    if (locationResult?.lastLocation != null) {
                        // save to local list
                        println("write")
                        postNewLocation(locationResult.lastLocation)
                        if(tempLocationsSize()==30){
                            println("database")
                            postLocation()
                        }
                    } else {
                        Timber.d("Location information isn't available.")
                    }
                }
            }
            fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }

    @SuppressLint("MissingPermission")
    fun getTempLocationList() :  LiveData<MutableList<Tuple>> {
        return  _tempLocations
    }
    private fun postNewLocation(loc : Location?){
        val list = _tempLocations.value!!
        if (loc != null) {
            list.add(Tuple(longitude = loc.longitude,latitude = loc.latitude))
        }
        _tempLocations.postValue( list)
    }
    private fun tempLocationsSize(): Int {
        return _tempLocations.value!!.size
    }
    /**
     * @author Simon
     * is called every minute
     * gets 6 records out of the list  at positions (0,5,10,15,20,25)
     * this is than saved in the database and later on send to the backend
     *
     * needs 6 records to have a nice forming line on the screen (angular and android)
     */
    private fun postLocation() {
        GlobalScope.launch {
            val tempLocations = _tempLocations.value
            var counter = 0
            while (counter < 30) {
                val tempLoc = tempLocations!![counter]
                val loc = Tuple(longitude = tempLoc.longitude, latitude = tempLoc.latitude)
                dataSource?.insert(loc)
                counter += 5
            }
            resetCurrentTempLocations()
        }

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

    /**
     * @author Simon
     * clear both the local database
     */
    suspend fun deleteLocations(){
        dataSource?.clear()
    }

}