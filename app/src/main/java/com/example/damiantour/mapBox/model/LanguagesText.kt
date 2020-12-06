package com.example.damiantour.mapBox.model

import androidx.room.*
import com.example.damiantour.database.MapTypeConverter
import com.example.damiantour.network.model.LanguagesData

class LanguagesText(
        @ColumnInfo(name = "title")
        val title : Map<String, String>,
        @ColumnInfo(name = "description")
        val description: Map<String, String>
) {

}