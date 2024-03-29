package app.adinfinitum.ello.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.adinfinitum.ello.utils.*


@Entity(tableName = "user_table")
data class User (

    @PrimaryKey(autoGenerate = true)
    var id:Long= 0L,

    @ColumnInfo(name="user_phone")
    val userPhone:String= EMPTY_PHONE_NUMBER,

    @ColumnInfo(name= "token")
    val userToken: String = EMPTY_TOKEN,

    @ColumnInfo(name= "email")
    val userEmail: String = EMPTY_EMAIL,

    @ColumnInfo(name= "password")
    val userPassword: String = EMPTY_PASSWORD

)
