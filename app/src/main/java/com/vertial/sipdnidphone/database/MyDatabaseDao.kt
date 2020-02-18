package com.vertial.sipdnidphone.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.vertial.sipdnidphone.model.User

@Dao
interface MyDatabaseDao {

    //USER TABLE
    @Query("SELECT * FROM user_table WHERE id=1")
    fun getUser():LiveData<User>

    @Query("UPDATE user_table SET user_phone=:phoneNb,token=:token WHERE ID=1")
    fun updateUsersPhoneAndToken(phoneNb:String,token:String)


    //PRENUMBER TABLE
    @Query("SELECT prenumber FROM prenumber_table WHERE id=1")
    fun getPrenumber():LiveData<String>


}
