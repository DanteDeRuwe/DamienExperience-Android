package com.example.damiantour.startRoute

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentStartRouteNotRegisteredBinding


/**
 * @author Jonas Haenebalcke en Jordy Van Kerkvoorde
 */
class StartRouteNotRegisteredFragment : Fragment() {

    private lateinit var binding: FragmentStartRouteNotRegisteredBinding

    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        preferences = requireActivity().getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
        binding = FragmentStartRouteNotRegisteredBinding.inflate(inflater, container, false)
        binding.registerButton.setOnClickListener(View.OnClickListener {
            navigateToWeb()
        })

        binding.logoutButtnSRNR.setOnClickListener {
            logout()
        }

        return binding.root
    }

    fun navigateToWeb(){
        val packageManager = activity?.packageManager!!
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, "https://damianexperience.netlify.app")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun logout(){
        preferences.edit().remove("TOKEN").apply()
        view?.findNavController()?.navigate(R.id.action_startRouteNotRegistered_to_loginFragment)
    }



}