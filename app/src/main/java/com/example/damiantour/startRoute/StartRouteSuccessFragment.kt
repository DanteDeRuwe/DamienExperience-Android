package com.example.damiantour.startRoute

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentLoginBinding
import com.example.damiantour.databinding.FragmentStartRouteSuccessBinding
import com.example.damiantour.login.LoginFragmentDirections
import com.example.damiantour.login.LoginViewModel
import com.example.damiantour.network.DamianApiService
import com.example.damiantour.network.model.RegistrationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime


/**
 * @author Jonas Haenebalcke en Jordy Van Kerkvoorde
 */
class StartRouteSuccessFragment : Fragment() {
    private lateinit var binding: FragmentStartRouteSuccessBinding
    private lateinit var preferences: SharedPreferences
    private val apiService : DamianApiService = DamianApiService.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        preferences = requireActivity().getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_start_route_success, container, false)
        initCountdown()
        getRouteRegistration()
        toggleButton(false)
        binding.startRouteButton.setOnClickListener {
            navigateToMapFragment()
        }

        binding.logoutButtonSRS.setOnClickListener{
            logout()
        }

        binding.startTV.setText(R.string.start_route_success_wait)

        return binding.root
    }


    private fun getRouteRegistration(){
        GlobalScope.launch(Dispatchers.IO) {
            var routeRegistration: RegistrationData?
            val JWTtoken = preferences.getString("TOKEN", null).toString()
            try {

                routeRegistration = apiService.getLastRegistration(JWTtoken)
                preferences.edit().putString("currentRouteId",routeRegistration.routeId).apply()
            }catch (e: Exception){
                println(e.stackTrace)
                Handler(Looper.getMainLooper()).postDelayed({
                    showToast(getString(R.string.fout_ophalen_registration))
                }, 3000)
            }
        }
    }
    private fun showToast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

   private fun navigateToMapFragment(){
       lifecycleScope.launch {
           val token = preferences.getString("TOKEN", null).toString()
           try{
               apiService.startWalk(token)
           }catch(e : Exception){
               println(e)
           }
       }

       val starttime = java.util.Calendar.getInstance().timeInMillis
       preferences.edit().putLong("starttime", starttime).apply()

       //check date en location
       val action = StartRouteSuccessFragmentDirections.actionStartRouteSuccessToMapFragment()
       view?.findNavController()?.navigate(action)
   }

    private fun toggleButton(bool: Boolean){
        /* Feedback

        binding.startRouteButton.isClickable = bool
        binding.startRouteButton.isEnabled = bool
        if(bool) binding.startRouteButton.visibility = View.VISIBLE else binding.startRouteButton.visibility = View.INVISIBLE
        */
        binding.startRouteButton.visibility = if(bool) View.VISIBLE else View.INVISIBLE
    }

    private fun initCountdown(){
        //natuurlijk moet de Future nog ingesteld worden op het verschil van de starttijd van de wandeling en de huidige datum (in millis)
        //aangezien dat niet zo tof gaat zijn voor te testen zou ik het gewoon zo hardcoded laten staan en uitleggen aan de klant
        val timer = object: CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.daysTV.setText(Math.floor((millisUntilFinished/ (1000 * 60 * 60 * 24)).toDouble()).toInt().toString())
                binding.hoursTV.setText(Math.floor(((millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)).toDouble()).toInt().toString())
                binding.minutesTV.setText(Math.floor(((millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)).toDouble()).toInt().toString())
                binding.secondsTV.setText(Math.floor((millisUntilFinished % (1000 * 60)).toDouble() / 1000).toInt().toString())
            }
            override fun onFinish() {
                binding.startTV.setText(R.string.start_route_success_start)
                toggleButton(true)
                /*
                lifecycleScope.launch{
                    val token = preferences.getString("TOKEN", null).toString()
                }*/
            }
        }
        timer.start()
    }

    private fun logout(){
        preferences.edit().remove("TOKEN").apply()
        view?.findNavController()?.navigate(R.id.action_startRouteSuccess_to_loginFragment)
    }
}