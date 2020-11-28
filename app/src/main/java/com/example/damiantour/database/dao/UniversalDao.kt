package com.example.damiantour.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface UniversalDao {
    @RawQuery
    fun nukeTable( query: SupportSQLiteQuery) : Int
}