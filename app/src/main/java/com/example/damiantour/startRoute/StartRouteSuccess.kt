package com.example.damiantour.startRoute

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentStartRouteSuccessBinding

class StartRouteSuccess : Fragment() {



    private lateinit var binding: FragmentStartRouteSuccessBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_start_route_success, container, false)

        binding.startRouteButton.setOnClickListener(View.OnClickListener {
                view: View -> Navigation.findNavController(view).navigate(R.id.action_startRouteSuccess_to_mapFragment)
    })

//        setHasOptionsMenu(true)


        return binding.root;
    }

//    private fun navigateToMapFragment(){
//        view?.findNavController()?.navigate(R.id.action_startRouteSuccess_to_mapFragment)
//    }
}