package com.vertial.fivemiov.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "webapi_version_table")
data class WebApiVersion (

    @PrimaryKey(autoGenerate = true)
    var id:Int=0,

    @ColumnInfo(name = "webApiVersion")
    val webApiVersion: String="0.0"

)