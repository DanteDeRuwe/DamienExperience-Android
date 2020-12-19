package com.example.damiantour.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentChatBinding
import com.example.damiantour.databinding.FragmentLoginBinding
import com.github.nkzawa.engineio.client.transports.WebSocket
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Exception

/**
* @author Dante De Ruwe and Jordy Van Kerkvoorde
 */
class ChatFragment : Fragment() {

    //private val socket : Socket = IO.socket(getString(R.string.chatSocketURI))
    private lateinit var binding: FragmentChatBinding
    //private var socket = IO.socket("https://damienexperience-chat.herokuapp.com")

    private lateinit var mSocket: Socket
    private var url: String = "https://localhost:3000/"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        val bottomNavigationView : BottomNavigationView = binding.navBar
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)
        /*socket.let {
            print("works???????????????????????????????")
            it.connect()
            .on(Socket.EVENT_CONNECT) {
                println("reeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
                Log.d("SignallingClient", "Socket connected!!!!!")
            }
        }*/

        //joinRoom()
        //connect()

        return binding.root
    }

    fun getmSocket(): Socket{
        return mSocket
    }

    /*fun connect(){
        val options = IO.Options()
        options.transports = arrayOf(WebSocket.NAME)
        val socket = IO.socket("https://localhost:3000/", options)
        socket.connect()
                .on(Socket.EVENT_CONNECT) { println("connected") }
                .on(Socket.EVENT_DISCONNECT) { println("disconnected") }

        val joinData = JoinData("String", "string@string.string", "string@string.string")

        socket.emit("join room", joinData)
    }*/

    /*private fun joinRoom(){
        print("joinnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn")
        val joinData = JoinData("String", "string@string.string", "string@string.string")

        socket.emit("join room", joinData)
    }

    fun sendMessage(){
        socket.emit("chat message", "hallo")
    }*/
}