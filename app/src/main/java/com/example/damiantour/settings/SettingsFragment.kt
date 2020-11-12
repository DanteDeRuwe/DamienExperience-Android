package com.example.damiantour.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
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

        val navController = findNavController()
        binding.navBar.setupWithNavController(navController)

        binding.logoutButton.setOnClickListener {
            logout()
        }

        binding.sliderid.addOnChangeListener { slider, value, fromUser ->
            //wip
            Toast.makeText(context, "Value:$value", Toast.LENGTH_SHORT).show()
        }

        binding.switchNotificaties.setOnCheckedChangeListener { buttonView, isChecked ->
            //wip
            Toast.makeText(context, "Value:$isChecked", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }

    private fun logout(){
        preferences.edit().remove("TOKEN").apply()
    }

}