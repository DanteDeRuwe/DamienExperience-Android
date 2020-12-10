package com.example.damiantour.network.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Simon Bettens & Jordy Van Kerkvoorde
 */
data class WaypointData (
        val id: String,
        val longitude: Double,
        val latitude: Double,
        val languagesText : LanguagesTextData
)