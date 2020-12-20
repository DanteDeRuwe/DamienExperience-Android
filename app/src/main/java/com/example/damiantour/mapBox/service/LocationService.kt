package com.example.damiantour.mapBox.service

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
import com.example.damiantour.database.dao.LocationDatabaseDao
import com.example.damiantour.database.dao.RouteDatabaseDao
import com.example.damiantour.database.dao.TupleDatabaseDao
import com.example.damiantour.mapBox.LocationUtils
import com.example.damiantour.mapBox.model.LocationData
import com.example.damiantour.mapBox.model.Tuple
import com.example.damiantour.network.DamianApiService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 * @author Simon Bettens
 */

class LocationService : Service() {
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
    private var dataSource: LocationDatabaseDao? = null
    private var routeDataSource: RouteDatabaseDao? = null
    //the preferences
    private lateinit var preferences: SharedPreferences

    //the location provider
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    //the location request
    //private var locationRequest: LocationRequest? = null

    //callback that is called on locationchange
    //private var locationCallback: LocationCallback? = null

    //private var lm : LocationManager? = null

    private var job: Job? = null
    private var cancellationSource: CancellationTokenSource? = null


    override fun onCreate() {
        //Log.i("LocationService", "onCreate")
        super.onCreate()
        preferences = getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
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
                dataSource = DamianDatabase.getInstance(context).locationDatabaseDao
                routeDataSource = DamianDatabase.getInstance(context).routeDatabaseDao
                dataSource?.clear()
            }
        }
        if (fusedLocationProviderClient == null) {
            LocationUtils.start()
            fusedLocationProviderClient =
                    getFusedLocationProviderClient(context)

        }
        job = GlobalScope.launch {
            writeLocationCoRoutine()
        }
    }

    fun stopService() {
        println("Stopping the foreground service")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            GlobalScope.launch(Dispatchers.IO) {
                routeDataSource?.clear()

            }
            cancellationSource?.cancel()
            stopForeground(true)
            job?.cancel("Stop record location")
            stopSelf()
        } catch (e: Exception) {
            println("Service stopped without being started: ${e.message}")
        }
        timer = false
        isServiceStarted = false
    }

    @SuppressLint("MissingPermission")
    private fun getTempLocationList(): MutableList<LocationData>? {
        return LocationUtils.getTempLocationList().value
    }

    private fun postNewLocation(loc: Location?) {
        LocationUtils.postNewLocation(loc)
    }

    private fun tempLocationsSize(): Int {
        return LocationUtils.tempLocationsSize()
    }


    var timer: Boolean = false

    /**
     * @author Simon Bettens
     *
     * gets location every 2 seconds needs to be accurate
     * saves location in temp list
     *
     * after 1 minute has passed the temp location list will be flattend to only fit 6 locations
     *
     * will send the locations to backend when the user decides
     */
    @SuppressLint("MissingPermission")
    private suspend fun writeLocationCoRoutine() {
        var time = 0
        timer = true
        delay(5000)
        while (timer) {
            var counter = 0
            while (counter <= 60) {
                delay(2000)
                fusedLocationProviderClient?.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationSource?.token)?.addOnSuccessListener {
                    val loc = it
                    postNewLocation(loc)
                }?.addOnCanceledListener {
                    println("cancel")
                }
                counter += 2
                //println(counter)
            }
            if (tempLocationsSize() >= 30) {
                println("database")
                postLocation()
            }
            time += 1
            val sendWhen = preferences.getInt("send_route_call_api", 5)
            //println("time : $time  sendWhen : $sendWhen")
            if (time == sendWhen) {
                //println("callApi")
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
    private suspend fun postLocation() {
        val tempLocations = getTempLocationList()!!
        val tempLocSize = tempLocationsSize()
        val shift = tempLocSize / 6
        var counter = 0
        while (counter < tempLocSize) {
            val tempLoc = tempLocations[counter]
            val loc = LocationData(longitude = tempLoc.longitude, latitude = tempLoc.latitude)
            dataSource?.insert(loc)
            counter += shift
            howManyItemsToSend += 1
        }
        resetCurrentTempLocations()
    }

    /**
     * @author Simon Bettens
     *
     * will send the locations to the backend
     */
    suspend fun updateWalkApi() {
        val token = preferences.getString("TOKEN", "")!!
        GlobalScope.launch {
            val tupelsList: List<LocationData> = dataSource?.getAllLocations()!!
            var size = tupelsList.size
            val startIndex = size - howManyItemsToSend
            val allTuples = ArrayList<ArrayList<Double>>()
            size -= 1
            if(howManyItemsToSend!=0 && startIndex>=0){
                for (x in startIndex..size) {
                    val tuple = tupelsList[x]
                    allTuples.add(tuple.getLocationTuple())
                }
                try {
                    apiService.updateWalk(token, allTuples)

                } catch (e: java.lang.Exception) {
                    println(e.localizedMessage)
                }
                howManyItemsToSend = 0
            }
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
     * when service is destroyed this is called and cleanup happens
     */
    override fun onDestroy() {
        job?.cancel("Stop record location")
        //fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        cancellationSource?.cancel()
        super.onDestroy()
    }
}