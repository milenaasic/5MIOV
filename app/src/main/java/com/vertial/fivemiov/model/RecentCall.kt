package com.vertial.fivemiov.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_calls_table")
data class RecentCall (

    @PrimaryKey(autoGenerate = true)
    var id:Long= 0L,

    @ColumnInfo(name="recent_call_contact_name")
    val recentCallName:String,

    @ColumnInfo(name= "recent_call_phone")
    val recentCallPhone: String,

    @ColumnInfo(name= "recent_call_time")
    val recentCallTime: Long

)
