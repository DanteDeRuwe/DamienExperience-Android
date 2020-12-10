package com.example.damiantour.mapBox.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.damiantour.database.DateTypeConverter
import java.util.*
/**
 * needed to store the route in database
 * @author Simon Bettens
 */
@Entity(tableName = "route_table")
@TypeConverters(DateTypeConverter::class)
data class Route(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "route_id")
        val id: Long = 0L,
        @ColumnInfo(name = "tourName")
        val tourName: String,
        @ColumnInfo(name = "date")
        val date: Date,
        @ColumnInfo(name = "distanceInMeters")
        val distanceInMeters: Int
) {

}