package com.example.damiantour.noConnection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentNoConnectionBinding
import com.example.damiantour.databinding.FragmentStartRouteNotRegisteredBinding

class NoConnectionFragment : Fragment() {

    private lateinit var binding: FragmentNoConnectionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNoConnectionBinding.inflate(
                   inflater, container, false)

        binding.retryConnectionButton.setOnClickListener { view ->
            val action = NoConnectionFragmentDirections.actionNoConnectionToLoginFragment()
            view.findNavController().navigate(action)
        }
        return binding.root
    }



}