package com.example.damiantour.stopRoute

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.damiantour.R

/**
 *@author: Ruben Naudts
 */
class StoppedRouteViewModel : ViewModel() {
    private var distanceWalked : Double = 2.0
    private var averageSpeed : Double = 4.0

    fun getDistanceWalked(): Double {
        return distanceWalked
    }

    fun getAverageSpeed(): Double {
        return averageSpeed
    }
}