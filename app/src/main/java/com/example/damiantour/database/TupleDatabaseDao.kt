package com.example.damiantour.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.damiantour.mapBox.Tuple

@Dao
interface TupleDatabaseDao {

    @Insert
    suspend fun insert(tuple: Tuple)

    @Query("DELETE FROM tuple_table")
    suspend fun clear()

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM tuple_table ORDER BY tupleId")
    fun getAllTuplesLiveData(): LiveData<List<Tuple>>

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM tuple_table ORDER BY tupleId")
    fun getAllTuples(): List<Tuple>



}