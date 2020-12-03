package com.example.damiantour.mapBox

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.damiantour.database.dao.LocationDatabaseDao
import com.example.damiantour.database.dao.RouteDatabaseDao
import com.example.damiantour.database.dao.TupleDatabaseDao
import com.example.damiantour.database.dao.WaypointDatabaseDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the TupleDatabaseDao and context to the ViewModel.
 */
class MapViewModelFactory(
        private val dataSource: TupleDatabaseDao,
        private val waypointDataSource : WaypointDatabaseDao,
        private val locationDatabaseDao: LocationDatabaseDao,
        private val routeDatabaseDao: RouteDatabaseDao,
        private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(dataSource,waypointDataSource, locationDatabaseDao, routeDatabaseDao, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}