package com.vertial.fivemiov.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vertial.fivemiov.utils.EMPTY_MAIN_SIP_CALLER_ID

@Entity(tableName = "users_sipcallerIDs_table")
data class UsersSipCallerIDs (

    @PrimaryKey(autoGenerate = true)
    var id:Int,

    @ColumnInfo(name= "userSipCallerId")
    val userSipCallerID: String

)
