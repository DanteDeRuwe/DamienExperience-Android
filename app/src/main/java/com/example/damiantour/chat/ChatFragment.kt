package com.example.damiantour.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.R
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false)
        val bottomNavigationView : BottomNavigationView = root.findViewById(R.id.nav_bar)
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)
        return root
    }
}