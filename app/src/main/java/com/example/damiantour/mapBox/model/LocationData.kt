package com.example.damiantour.mapBox.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mapbox.geojson.Point


/**
 * used to record the location
 * @author Simon Bettens & Jordy Van Kerkvoorde
 */
@Entity(tableName = "locationData_table")
class LocationData(
        @PrimaryKey(autoGenerate = true)
        var locationId: Long = 0L,

        @ColumnInfo(name = "longitude")
        val longitude: Double = 0.0,

        @ColumnInfo(name = "latitude")
        val latitude: Double =0.0)
{
    /**
     *     get the tuple in DoubleArray form
     */
    fun getLocationTuple(): ArrayList<Double> {
        val tuple = ArrayList<Double>()
        tuple.add(longitude)
        tuple.add(latitude)
        return tuple
    }

    fun mapToPoint(): Point {
        return Point.fromLngLat(longitude, latitude);
    }
}