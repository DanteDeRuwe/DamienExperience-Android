package com.example.damiantour.settings

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.R
import com.example.damiantour.database.DamianDatabase
import com.example.damiantour.database.dao.LocationDatabaseDao
import com.example.damiantour.database.dao.RouteDatabaseDao
import com.example.damiantour.databinding.FragmentSettingsBinding
import com.example.damiantour.mapBox.service.LocationService
import com.example.damiantour.mapBox.service.LocationServiceBinder
import com.example.damiantour.network.DamianApiService
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 *  author: Simon Bettens & Lucas Van der Haegen
 */
class SettingsFragment :Fragment(){
    private lateinit var routeDataSource: RouteDatabaseDao
    private lateinit var locationDataSource: LocationDatabaseDao
    private lateinit var preferences: SharedPreferences
    private lateinit var binding: FragmentSettingsBinding
    //the service
    private lateinit var locationService : LocationService
    //there is a service bound on the object
    private var mBound: Boolean = false
    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocationServiceBinder
            locationService = binder.getService()
            mBound = true
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private val apiService : DamianApiService = DamianApiService.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceStart = Intent(context, LocationService::class.java)
        serviceStart.action = "START"
        context?.startService(serviceStart)
        context?.bindService(serviceStart, connection, Context.BIND_AUTO_CREATE)
    }

    //on the create view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = requireNotNull(this.activity).application
        preferences = requireActivity().getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        locationDataSource = DamianDatabase.getInstance(application).locationDatabaseDao
        routeDataSource = DamianDatabase.getInstance(application).routeDatabaseDao
        val bottomNavigationView : BottomNavigationView = binding.root.findViewById(R.id.nav_bar)
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)

        val button : Button = binding.root.findViewById(R.id.logoutButton)
        button.setOnClickListener {
            logout()
        }

        binding.textUsernaam.text = preferences.getString("fullName", "fout")

        binding.sliderid.value = preferences.getInt("send_route_call_api",5).toFloat()

        binding.switchNotificaties.isChecked = preferences.getBoolean("notifications",false)

        binding.sliderid.addOnChangeListener { _, value, _ ->
            preferences.edit().putInt("send_route_call_api",value.toInt()).apply()
        }

        binding.switchNotificaties.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("notifications",isChecked).apply()
        }
        binding.buttonShareDeelnemerscode.setOnClickListener {
            var url = "https://damianexperience.netlify.app/track"
            val mail = preferences.getString("email", null)
            if(mail!=null){
                url+="?email=${mail}"
            }
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, R.string.text_deellink)
                putExtra(Intent.EXTRA_TEXT, url)
                type = "text/plain"

            }
            val shareIntent = Intent.createChooser(sendIntent, "Share URL")
            startActivity(shareIntent)
        }
        binding.btnHowItWork.setOnClickListener {
            //enter the real link
            val uris = Uri.parse("https://damianexperience.netlify.app/about")
            val intents = Intent(Intent.ACTION_VIEW, uris)
            val b = Bundle()
            b.putBoolean("new_window", true)
            intents.putExtras(b)
            context?.startActivity(intents)
        }

        return binding.root

    }

    private suspend fun setDeelnemerscode() : String {
        val token = preferences.getString("TOKEN", "")
        var deelnemerscode = ""
        try {
            if (token != null) {
                deelnemerscode = apiService.getDeelnemerscode(token)
            }
        } catch (e : Exception) {
            //TODO
        }
        
        preferences.edit().putString("deelnemercode", deelnemerscode).apply()
        return deelnemerscode
    }

    private fun logout(){
        preferences.edit().remove("TOKEN").apply()
        GlobalScope.launch(Dispatchers.IO) {
            locationService.updateWalkApi()
            locationService.stopService()
            routeDataSource.clear()
            locationDataSource.clear()
        }
        view?.findNavController()?.navigate(R.id.action_settingsFragment2_to_loginFragment)
    }

}