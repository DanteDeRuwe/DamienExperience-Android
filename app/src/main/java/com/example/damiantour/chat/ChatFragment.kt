package com.example.damiantour.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.R
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
* @author Dante De Ruwe and Jordy Van Kerkvoorde
 */
class ChatFragment : Fragment() {

    private val socket : Socket = IO.socket(getString(R.string.chatSocketURI))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false)
        val bottomNavigationView : BottomNavigationView = root.findViewById(R.id.nav_bar)
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)

        socket.connect() //TODO

        return root
    }
}