package com.example.damiantour.mapBox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.damiantour.mapBox.service.LocationService

class StartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ) {
            Intent(context, LocationService::class.java).also {
                it.action = "START"
                context.startService(it)
            }
        }
    }
}