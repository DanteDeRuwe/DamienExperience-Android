package com.example.damiantour.stopRoute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.damiantour.database.dao.LocationDatabaseDao
import com.example.damiantour.mapBox.MapViewModel

/**
 *  author: Jordy Van Kerkvoorde
 */
class StoppedRouteViewModelFactory(
        private val locationDatabaseDao: LocationDatabaseDao) : ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoppedRouteViewModel::class.java)) {
            return StoppedRouteViewModel(locationDatabaseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}