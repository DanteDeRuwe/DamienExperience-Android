package com.example.damiantour.mapBox

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
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

class LocationService : Service(){

    val  MY_ACTION :String = "NewLocationRecorded"
    val apiService: DamianApiService = DamianApiService.create()
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

    override fun onCreate() {
        Log.i("LocationService", "onCreate")
        super.onCreate()
        start()
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("LocationService", "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    /**
     * mehod that inits all properties on first start else does nothing
     */

    @SuppressLint("MissingPermission")
    fun start(){
        val context: Context = this
        if(LocationUtils.dataSource ==null) {
            GlobalScope.launch {
                LocationUtils.dataSource = DamianDatabase.getInstance(context).tupleDatabaseDao
                LocationUtils.dataSource?.clear()
            }
        }
        if(LocationUtils.fusedLocationProviderClient == null) {
            LocationUtils._tempLocations.value = ArrayList<Tuple>()
            LocationUtils.fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)
        }
        if(LocationUtils.locationRequest == null){
            LocationUtils.locationRequest = LocationRequest().apply {
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
        if(LocationUtils.locationCallback == null){
            LocationUtils.locationCallback = object : LocationCallback() {
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
            LocationUtils.fusedLocationProviderClient?.requestLocationUpdates(
                LocationUtils.locationRequest,
                LocationUtils.locationCallback, Looper.myLooper()
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun getTempLocationList() :  LiveData<MutableList<Tuple>> {
        return LocationUtils._tempLocations
    }

    private fun postNewLocation(loc: Location?){
        val list = LocationUtils._tempLocations.value!!
        if (loc != null) {
            list.add(Tuple(longitude = loc.longitude, latitude = loc.latitude))
            val intent = Intent()
            intent.action = MY_ACTION
            intent.putExtra("LONGITUDE", loc.longitude)
            intent.putExtra("LATITUDE", loc.latitude)
            sendBroadcast(intent)
        }
        LocationUtils._tempLocations.postValue(list)
    }

    private fun tempLocationsSize(): Int {
        return LocationUtils._tempLocations.value!!.size
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
            val tempLocations = LocationUtils._tempLocations.value
            var counter = 0
            while (counter < 30) {
                val tempLoc = tempLocations!![counter]
                val loc = Tuple(longitude = tempLoc.longitude, latitude = tempLoc.latitude)
                LocationUtils.dataSource?.insert(loc)
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
        val list = LocationUtils._tempLocations.value
        if(list!=null) {
            LocationUtils._tempLocations.value!!.clear()
        }
    }

    /**
     * @author Simon
     * clear both the local database
     */
    suspend fun deleteLocations(){
        LocationUtils.dataSource?.clear()
    }

    /**
     * when service is destroyed
     */
    override fun onDestroy() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        println("aaaaahhhhhh i die")
        super.onDestroy()
    }


}