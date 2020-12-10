package com.example.damiantour.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentChatBinding
import com.example.damiantour.databinding.FragmentLoginBinding
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
* @author Dante De Ruwe and Jordy Van Kerkvoorde
 */
class ChatFragment : Fragment() {

    private val socket : Socket = IO.socket(getString(R.string.chatSocketURI))
    private lateinit var binding: FragmentChatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        val bottomNavigationView : BottomNavigationView = binding.navBar
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)

        joinRoom()

        return binding.root
    }

    private fun joinRoom(){
        socket.connect() //TODO
        val joinData: JoinData = JoinData("String", "string@string.string", "string@string.string")

        socket.emit("join room", joinData)
    }

    fun sendMessage(){
        socket.emit("chat message", "hallo")
    }
}