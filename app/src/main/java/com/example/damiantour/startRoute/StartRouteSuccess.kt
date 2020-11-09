package com.example.damiantour.startRoute

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentLoginBinding
import com.example.damiantour.databinding.FragmentStartRouteSuccessBinding
import com.example.damiantour.login.LoginViewModel
import com.example.damiantour.network.DamianApiService
import kotlinx.coroutines.launch

class StartRouteSuccess : Fragment() {
    private lateinit var binding: FragmentStartRouteSuccessBinding
    private lateinit var preferences: SharedPreferences
    private val apiService : DamianApiService = DamianApiService.create()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        preferences = requireActivity().getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_start_route_success, container, false)

        binding.startRouteButton.setOnClickListener(View.OnClickListener {
                navigateToMapFragment()
        })

        return binding.root;
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

       //check date en location
       view?.findNavController()?.navigate(R.id.action_startRouteSuccess_to_mapFragment)
   }
}