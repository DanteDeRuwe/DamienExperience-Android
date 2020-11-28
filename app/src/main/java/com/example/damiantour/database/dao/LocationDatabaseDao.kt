package com.example.damiantour.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.damiantour.mapBox.model.LocationData
import com.example.damiantour.mapBox.model.Tuple

@Dao
interface LocationDatabaseDao {
    @Insert
    suspend fun insert(location: LocationData)

    @Query("DELETE FROM locationData_table")
    suspend fun clear()

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM locationData_table ORDER BY locationId")
    fun getAllLocationsLiveData(): LiveData<List<LocationData>>

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by id.
     */
    @Query("SELECT * FROM locationData_table ORDER BY locationId")
    fun getAllLocations(): List<LocationData>
}