package com.example.damiantour.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.damiantour.mapBox.model.Route

/**
 * @author: Ruben Naudts
 */
@Dao
interface RouteDatabaseDao {
    @Insert
    suspend fun insert(route: Route)

    @Delete
    suspend fun delete(route: Route)

    @Query("select * from route_table limit 1")
    suspend fun getRoute(): Route

    @Query("DELETE FROM route_table")
    suspend fun clear()
}