package com.example.damiantour.network

import java.util.*

/**
 * @author Simon Bettens & Jordy Van Kerkvoorde
 */
data class WaypointData (
        val id: String,
        val longitude: Double,
        val latitude: Double,
        val languagesText : LanguagesTextData
)