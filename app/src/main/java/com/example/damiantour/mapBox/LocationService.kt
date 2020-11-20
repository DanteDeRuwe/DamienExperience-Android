package com.example.damiantour.mapBox

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import com.example.damiantour.database.DamianDatabase
import com.example.damiantour.database.TupleDatabaseDao
import com.example.damiantour.network.DamianApiService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.*
import org.json.JSONArray
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 * @author Simon Bettens
 */

class LocationService : Service() {
    //name of the broadcast
    private val MY_ACTION: String = "NewLocationRecorded"

    //singleton class to connect with the viewmodel
    private var locationUtils: LocationUtils = LocationUtils
    private val binder = LocationServiceBinder(this)
    private var isServiceStarted: Boolean = false

    //this is necersary to keep the service a live when the phone is turned off
    // could be verry power intensive
    //needs much testing!
    private var wakeLock: PowerManager.WakeLock? = null

    //api service
    private val apiService: DamianApiService = DamianApiService.create()
    private var howManyItemsToSend: Int = 0

    //database connection
    private var dataSource: TupleDatabaseDao? = null

    //the preferences
    private lateinit var preferences: SharedPreferences

    //the location provider
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    //the location request
    private var locationRequest: LocationRequest? = null

    //callback that is called on locationchange
    private var locationCallback: LocationCallback? = null

    private var lm : LocationManager? = null

    private var job: Job? = null
    private var cancellationSource: CancellationTokenSource? = null


    override fun onCreate() {
        Log.i("LocationService", "onCreate")
        super.onCreate()
        preferences = getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("LocationService", "onStartCommand")
        if (intent != null) {
            val action = intent.action
            println("using an intent with action $action")
            when (action) {
                "START" -> startService()
                "STOP" -> stopService()
                else -> println("This should never happen. No action in the received intent")
            }
        } else {
            println(
                    "with a null intent. It has been probably restarted by the system."
            )
        }

        val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("my_service", "My Background Service")
                } else {
                    // If earlier version channel ID is not used
                    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                    ""
                }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(101, notification)
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    /**
     * mehod that inits all properties on first start else does nothing
     */

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    fun startService() {
        if (isServiceStarted) return
        println("Starting the foreground service task")
        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true

        // we need this lock so our service gets not affected by Doze Mode
        //
        wakeLock =
                (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationService::lock").apply {
                        acquire()
                    }
                }
        cancellationSource = CancellationTokenSource()
        val context: Context = this
        if (dataSource == null) {
            GlobalScope.launch {
                dataSource = DamianDatabase.getInstance(context).tupleDatabaseDao
                dataSource?.clear()
            }
        }
        if (fusedLocationProviderClient == null) {
            locationUtils.start()
            fusedLocationProviderClient =
                    getFusedLocationProviderClient(context)
        }
        if (locationRequest == null) {
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

                smallestDisplacement = 5F

                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            }
        }

        if(locationCallback == null){
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    if (locationResult?.lastLocation != null) {
                        // save to local list
                        /*
                        println("write")
                        postNewLocation(locationResult.lastLocation)
                        if(tempLocationsSize()==30){
                            println("database")
                            postLocation()
                        }
                        */
                        println("update")
                    } else {
                        Timber.d("Location information isn't available.")
                    }
                }
            }
        }

        fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,locationCallback, Looper.myLooper()
        )
        job = GlobalScope.launch {
            writeLocationCoRoutine()
        }
    }

    fun stopService() {
        println("Stopping the foreground service")
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {

            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            cancellationSource?.cancel()
            stopForeground(true)
            fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
            job?.cancel("Stop record location")
            stopSelf()
        } catch (e: Exception) {
            println("Service stopped without being started: ${e.message}")
        }
        timer = false
        isServiceStarted = false
    }

    @SuppressLint("MissingPermission")
    private fun getTempLocationList(): MutableList<Tuple>? {
        return LocationUtils.getTempLocationList().value
    }

    private fun postNewLocation(loc: Location?) {
        LocationUtils.postNewLocation(loc)
    }

    private fun tempLocationsSize(): Int {
        return LocationUtils.tempLocationsSize()
    }


    var timer: Boolean = false

    @SuppressLint("MissingPermission")
    private suspend fun writeLocationCoRoutine() {
        var time = 0
        timer = true
        while (timer) {
            var counter = 0
            while (counter <= 60) {
                println("How many secs passed : $counter")
                delay(2000)
                fusedLocationProviderClient?.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, cancellationSource?.token)?.addOnSuccessListener {
                    val loc = it
                    println("${loc.latitude},${loc.longitude}")
                    postNewLocation(loc)
                }?.addOnCanceledListener {
                    println("cancel")
                }
                counter += 2
                println("counter $counter")
            }
            if (tempLocationsSize() == 30) {
                println("database")
                postLocation()
            }
            time += 1
            val sendWhen = 0 //preferences.getInt("send_route_call_api", 5)
            if (time == sendWhen) {
                updateWalkApi()
                time = 0
            }
        }
    }

    /**
     * @author Simon
     * is called every minute
     * gets 6 records out of the list  at positions (0,5,10,15,20,25)
     * this is than saved in the database and later on send to the backend
     * needs 6 records to have a nice forming line on the screen (angular and android)
     */
    suspend fun postLocation() {
        val tempLocations = getTempLocationList()!!
        val tempLocSize = tempLocationsSize()
        val shift = tempLocSize / 6
        var counter = 0
        while (counter < tempLocSize) {
            val tempLoc = tempLocations[counter]
            val loc = Tuple(longitude = tempLoc.longitude, latitude = tempLoc.latitude)
            dataSource?.insert(loc)
            counter += shift
            howManyItemsToSend += 1
        }
        resetCurrentTempLocations()
    }

    suspend fun updateWalkApi() {
        val token = preferences.getString("TOKEN", "")!!
        GlobalScope.launch {
            val tupelsList: List<Tuple> = dataSource?.getAllTuples()!!
            var size = tupelsList.size
            println("size $size")
            val startIndex = size - howManyItemsToSend
            println("startIndex $startIndex")
            val allTuples = ArrayList<ArrayList<Double>>()
            size = size - 1
            for (x in startIndex..size) {
                println("x $x")
                val tuple = tupelsList[x]
                allTuples.add(tuple.getTuple())
            }
            try {
                val jsonArray = JSONArray(allTuples)
                println(jsonArray)
                apiService.updateWalk(token, allTuples)
            } catch (e: java.lang.Exception) {
                println(e.localizedMessage)
            }
            howManyItemsToSend = 0
        }
    }

    /**
     * @author Simon
     * clears the temporay list of locations
     */
    private fun resetCurrentTempLocations() {
        LocationUtils.resetCurrentTempLocations()
    }

    /**
     * @author Simon
     * clear both the local database
     */
    suspend fun deleteDatabaseLocations() {
        dataSource?.clear()
    }

    /**
     * @author Simon
     * when service is destroyed this is called and cleanup happens
     */
    override fun onDestroy() {
        println("aaaaahhhhhh i die")
        job?.cancel("Stop record location")
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        cancellationSource?.cancel()
        super.onDestroy()
    }
}