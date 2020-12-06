package com.example.damiantour.network.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

data class LanguagesTextData (
        val title : LanguagesData,
        val description: LanguagesData
)