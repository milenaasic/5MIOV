package com.vertial.fivemiov.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.vertial.fivemiov.model.User

@Dao
interface MyDatabaseDao {

    //USER TABLE
    @Query("SELECT * FROM user_table WHERE id=1")
    fun getUser():LiveData<User>

    @Query("UPDATE user_table SET user_phone=:phoneNb,token=:token WHERE ID=1")
    fun updateUsersPhoneAndToken(phoneNb:String,token:String)

    @Query("SELECT token FROM user_table WHERE id=1")
    fun getToken():String

    @Query("SELECT user_phone FROM user_table WHERE id=1")
    fun getPhone():String

    @Query("UPDATE user_table SET email=:email WHERE ID=1")
    fun updateUserEmail(email:String)

    @Query("UPDATE user_table SET user_phone=:phoneNb,token=:token,email=:email WHERE ID=1")
    fun logout(phoneNb: String,token: String,email: String)


    //PRENUMBER TABLE
    @Query("SELECT prenumber FROM prenumber_table WHERE id=1")
    fun getPrenumber():LiveData<String>


}
