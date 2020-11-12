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

        val bottomNavigationView : BottomNavigationView = binding.root.findViewById(R.id.nav_bar)
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)

        val button : Button = binding.root.findViewById(R.id.logoutButton)
        button.setOnClickListener {
            logout()
        }

//        binding.seekBar2.setOnSeekBarChangeListener(object :
//            SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seek: SeekBar,
//                                           progress: Int, fromUser: Boolean) {
//                // write custom code for progress is changed
//
//            }
//
//            override fun onStartTrackingTouch(seek: SeekBar) {
//                // write custom code for progress is started
//            }
//
//            override fun onStopTrackingTouch(seek: SeekBar) {
//                // write custom code for progress is stopped
//                Toast.makeText(context,
//                    "Progress is: " + seek.progress + "%",
//                    Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        binding.seekBar2.setProgress(0)
//        binding.seekBar2.incrementProgressBy(5)



        return binding.root

    }

    private fun logout(){
        preferences.edit().remove("TOKEN").apply()
    }

}