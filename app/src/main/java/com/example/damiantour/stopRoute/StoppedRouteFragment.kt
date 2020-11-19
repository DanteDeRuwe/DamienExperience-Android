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
import com.example.damiantour.databinding.FragmentStoppedRouteBinding

/**
 * @author: Ruben Naudts & Jordy Van Kerkvoorde <3
 */
class StoppedRouteFragment : Fragment() {

    private lateinit var binding: FragmentStoppedRouteBinding

    private lateinit var viewModel: StoppedRouteViewModel

    private lateinit var preferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentStoppedRouteBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(StoppedRouteViewModel::class.java)

        preferences = requireActivity().getSharedPreferences("damian-tours", Context.MODE_PRIVATE)

        binding.goBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_stoppedRouteFragment_to_startRouteNotRegistered)
        }

        binding.distanceTextview.text = String.format("%.3f %s",viewModel.getDistanceWalked() ,getString(R.string.distance_unit))
        binding.speedTextview.text = String.format("%.2f %s",viewModel.getAverageSpeed() ,getString(R.string.speed_unit))

        // Get name from shared preferences
        val name = "Ruben"//preferences.getString("NAME", null).toString()

        binding.congratsTextview.text = String.format("%s %s", getString(R.string.congratulations), name)


        return binding.root
    }


}