package com.example.damiantour.stopRoute

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.damiantour.R
import com.example.damiantour.database.DamianDatabase
import com.example.damiantour.databinding.FragmentStoppedRouteBinding
import com.example.damiantour.mapBox.MapViewModel
import com.example.damiantour.mapBox.MapViewModelFactory

/**
 * @author: Ruben Naudts & Jordy Van Kerkvoorde
 */
class StoppedRouteFragment : Fragment() {

    private lateinit var binding: FragmentStoppedRouteBinding

    private lateinit var viewModel: StoppedRouteViewModel

    private lateinit var preferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentStoppedRouteBinding.inflate(inflater, container, false)

        //viewModel = ViewModelProvider(this).get(StoppedRouteViewModel::class.java)

        val application = requireNotNull(this.activity).application
        val locationDataSource = DamianDatabase.getInstance(application).locationDatabaseDao

        val viewModelFactory = StoppedRouteViewModelFactory(locationDataSource)
        viewModel = ViewModelProvider(this, viewModelFactory).get(StoppedRouteViewModel::class.java)

        preferences = requireActivity().getSharedPreferences("damian-tours", Context.MODE_PRIVATE)

        binding.goBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_stoppedRouteFragment_to_startRouteNotRegistered)
        }

        viewModel.locations.observe(viewLifecycleOwner, { listOfLocationData ->
            val dist = viewModel.calculateDistance(listOfLocationData)
            binding.distanceTextview.text = String.format("%.3f %s",dist,getString(R.string.distance_unit))

            val speed = viewModel.calculateSpeed(preferences.getLong("starttime", 0L))
            binding.speedTextview.text = String.format("%.2f %s",speed ,getString(R.string.speed_unit))
        })

        // Get name from shared preferences
        val name = preferences.getString("fullName", null).toString()

        binding.congratsTextview.text = String.format("%s %s", getString(R.string.congratulations), name)


        return binding.root
    }


}