package com.example.damiantour.mapBox

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Simon Bettens
 */
@Entity(tableName = "tuple_table")
data class Tuple(
    @PrimaryKey(autoGenerate = true)
    var tupleId: Long = 0L,

    @ColumnInfo(name = "longitude")
    val longitude: Double = 0.0,

    @ColumnInfo(name = "latitude")
    val latitude: Double =0.0
) {
    /**
     *     get the tuple in DoubleArray form
     */
    fun getTuple(): ArrayList<Double> {
        val tuple = ArrayList<Double>()
        tuple.add(longitude)
        tuple.add(latitude)
        return tuple
    }
}