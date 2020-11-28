package com.example.damiantour.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.damiantour.mapBox.model.LocationData
import com.example.damiantour.mapBox.model.Tuple
import com.example.damiantour.mapBox.model.Waypoint
import com.example.damiantour.network.model.WaypointData

@Dao
interface WaypointDatabaseDao {
    @Insert
    suspend fun insert(waypointData: Waypoint)

    @Query("DELETE FROM waypoint_table")
    suspend fun clear()

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM waypoint_table ORDER BY waypoint_id")
    fun getAllWaypointDataLiveData(): LiveData<List<Waypoint>>

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by id.
     */
    @Query("SELECT * FROM waypoint_table ORDER BY waypoint_id")
    fun getAllWaypointData(): List<Waypoint>
}