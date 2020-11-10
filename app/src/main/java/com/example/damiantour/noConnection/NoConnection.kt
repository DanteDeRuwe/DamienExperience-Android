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

class NoConnection : Fragment() {

    private lateinit var binding: FragmentNoConnectionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
                   inflater, R.layout.fragment_no_connection, container, false)

        binding.retryConnectionButton.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_noConnection_to_loginFragment)
        }
        return inflater.inflate(R.layout.fragment_no_connection, container, false)
    }



}