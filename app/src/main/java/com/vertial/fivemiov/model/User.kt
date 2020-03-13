package com.vertial.fivemiov.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vertial.fivemiov.utils.EMPTY_PHONE_NUMBER
import com.vertial.fivemiov.utils.EMPTY_TOKEN
import com.vertial.fivemiov.utils.EMPTY_USERNAME

@Entity(tableName = "user_table")
data class User (

    @PrimaryKey(autoGenerate = true)
    var id:Long= 0L,

    /*@ColumnInfo(name="user_name")
    val userName:String= EMPTY_USERNAME,*/

    @ColumnInfo(name="user_phone")
    val userPhone:String= EMPTY_PHONE_NUMBER,

    @ColumnInfo(name= "token")
    val userToken: String = EMPTY_TOKEN,

    @ColumnInfo(name= "email")
    val userEmail: String = EMPTY_TOKEN


)
