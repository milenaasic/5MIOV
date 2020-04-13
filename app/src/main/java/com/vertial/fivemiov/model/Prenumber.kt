package com.vertial.fivemiov.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "e1_prenumber_table")
data class E1Prenumber (

    @PrimaryKey(autoGenerate = true)
    var id:Int,

    @ColumnInfo(name = "e1prenumber")
    val prenumber: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long

)