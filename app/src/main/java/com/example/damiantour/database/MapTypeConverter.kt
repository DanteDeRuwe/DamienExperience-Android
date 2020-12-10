package com.example.damiantour.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object MapTypeConverter {
    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): Map<String?, String?>? {
        val mapType: Type = object : TypeToken<Map<String?, String?>?>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    @JvmStatic
    fun fromStringMap(map: Map<String?, String?>?): String? {
        val gson = Gson()
        return gson.toJson(map)
    }

}