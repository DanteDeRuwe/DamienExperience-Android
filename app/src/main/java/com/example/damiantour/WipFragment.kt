package com.example.damiantour

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.databinding.FragmentWipBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * A simple [Fragment] subclass.
 * Use the [WipFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WipFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //val binding : FragmentWipBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_wip, container, false);

        //return binding.root
        val root = inflater.inflate(R.layout.fragment_wip, container, false)
        val bottomNavigationView : BottomNavigationView = root.findViewById(R.id.nav_bar)
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)
        return root
    }
}