package com.example.damiantour.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentSettingsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsFragment :Fragment(){
    private lateinit var preferences: SharedPreferences
    private lateinit var binding: FragmentSettingsBinding

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


        return binding.root

    }
    private fun logout(){
        preferences.edit().remove("TOKEN").apply()
        //navigate to login
    }
}