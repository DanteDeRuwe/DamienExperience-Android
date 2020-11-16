package com.example.damiantour.stopRoute

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentStartRouteNotRegisteredBinding
import com.example.damiantour.databinding.FragmentStoppedRouteBinding

/**
 * @author: Ruben Naudts & Jordy Van Kerkvoorde <3
 */
class StoppedRouteFragment : Fragment() {

    private lateinit var binding: FragmentStoppedRouteBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentStoppedRouteBinding.inflate(inflater, container, false)

        binding.goBackButton.setOnClickListener({
            findNavController().navigate(R.id.action_stoppedRouteFragment_to_startRouteNotRegistered)
        })

        return binding.root


    }


}