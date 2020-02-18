package com.vertial.sipdnidphone.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prenumber_table")
data class Prenumber (

    @PrimaryKey(autoGenerate = true)
    var id:Int,

    @ColumnInfo(name = "prenumber")
    val prenumber: String
)