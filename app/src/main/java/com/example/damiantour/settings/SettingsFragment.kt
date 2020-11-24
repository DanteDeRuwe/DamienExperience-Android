package com.example.damiantour.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentSettingsBinding
import com.example.damiantour.network.DamianApiService
import com.example.damiantour.network.LoginData
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.lang.Exception

class SettingsFragment :Fragment(){
    private lateinit var preferences: SharedPreferences
    private lateinit var binding: FragmentSettingsBinding
    private var text_deelnemerscode : String = "Deelnemerscode"

    private val apiService : DamianApiService = DamianApiService.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //on the create view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        preferences = requireActivity().getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

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

        binding.sliderid.addOnChangeListener { slider, value, fromUser ->
            preferences.edit().putInt("send_route_call_api",value.toInt()).apply()
        }

        binding.switchNotificaties.setOnCheckedChangeListener { buttonView, isChecked ->
            preferences.edit().putBoolean("notifications",isChecked).apply()
        }
        binding.buttonShareDeelnemerscode.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text_deelnemerscode)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share deelnemerscode")
            startActivity(shareIntent)
        }
        binding.btnHowItWork.setOnClickListener {
            //enter the real link
            val uris = Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ&ab_channel=RickAstleyVEVO")
            val intents = Intent(Intent.ACTION_VIEW, uris)
            val b = Bundle()
            b.putBoolean("new_window", true)
            intents.putExtras(b)
            context?.startActivity(intents)
        }

        return binding.root

    }

    private fun getDeelnemerscode(){
        var deelnemerscode = preferences.getString("deelnemerscode", null)
        if (deelnemerscode == null){
            lifecycleScope.launch {
                deelnemerscode = setDeelnemerscode()
            }
        }

        binding.textDeelnemerscode.text = deelnemerscode
    }

    private suspend fun setDeelnemerscode() : String {
        var token = preferences.getString("TOKEN", "")
        var deelnemerscode = ""
        try {
            if (token != null) {
                deelnemerscode = apiService.getDeelnemerscode(token)
            }
        } catch (e : Exception) {
            //TODO
        }
        
        preferences.edit().putString("deelnemercode", deelnemerscode)
        return deelnemerscode
    }

    private fun logout(){
        preferences.edit().remove("TOKEN").apply()
        //wip
        view?.findNavController()?.navigate(R.id.action_settingsFragment2_to_loginFragment)
    }

}