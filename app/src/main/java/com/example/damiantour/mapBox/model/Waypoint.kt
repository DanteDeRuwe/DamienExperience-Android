package com.example.damiantour.mapBox.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.damiantour.network.model.LanguagesTextData

@Entity(tableName = "waypoint_table")
class Waypoint(@PrimaryKey(autoGenerate = false)
               @ColumnInfo(name = "waypoint_id")
               val id: String,
               @ColumnInfo(name = "longitude")
               val longitude: Double,
               @ColumnInfo(name = "latitude")
               val latitude: Double,
               @Embedded
               val languagesText : LanguagesText) {
}