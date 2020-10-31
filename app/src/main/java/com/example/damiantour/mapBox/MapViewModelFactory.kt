package com.example.damiantour.mapBox

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.damiantour.database.TupleDatabaseDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the SleepDatabaseDao and context to the ViewModel.
 */
class MapViewModelFactory(
    private val dataSource: TupleDatabaseDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}