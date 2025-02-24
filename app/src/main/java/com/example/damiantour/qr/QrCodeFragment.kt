package com.example.damiantour.qr

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.MainActivity
import com.example.damiantour.R
import com.google.android.material.bottomnavigation.BottomNavigationView


/**
 * A simple [Fragment] subclass.
 * Use the [QrCodeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QrCodeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_qr_code, container, false)
        val bottomNavigationView: BottomNavigationView = root.findViewById(R.id.nav_bar)
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)

        val scanQRBtn: Button = root.findViewById(R.id.btn_scan)

        scanQRBtn.setOnClickListener() {
            scanQr();
        }
        return root
    }

    /**
     * @author Jonas Haenebalcke
     */
    fun scanQr() {
        //Calls scanQR from mainActivity
        (activity as MainActivity).scanQr();
    }
}