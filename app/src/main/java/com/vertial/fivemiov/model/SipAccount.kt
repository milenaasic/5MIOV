package com.vertial.fivemiov.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vertial.fivemiov.utils.*

@Entity(tableName = "sip_account_table")
data class SipAccount (

    @PrimaryKey(autoGenerate = true)
    var id:Long= 0L,

    @ColumnInfo(name= "mainSipCallerId")
    val mainSipCallerId: String = EMPTY_MAIN_SIP_CALLER_ID,

    @ColumnInfo(name= "sipUsername")
    val sipUsername: String = EMPTY_SIP_USERNAME,

    @ColumnInfo(name= "sipPassword")
    val sipPassword: String = EMPTY_SIP_PASSWORD,

    @ColumnInfo(name= "sipServer")
    val sipServer: String = EMPTY_SIP_SERVER


)