package com.vertial.fivemiov.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.vertial.fivemiov.model.User
import com.vertial.fivemiov.utils.EMPTY_EMAIL
import com.vertial.fivemiov.utils.EMPTY_PHONE_NUMBER
import com.vertial.fivemiov.utils.EMPTY_TOKEN

@Dao
interface MyDatabaseDao {

    //USER TABLE
    @Query("SELECT * FROM user_table WHERE id=1")
    fun getUser():LiveData<User>

    @Query("SELECT * FROM user_table WHERE id=1")
    fun getUserNoLiveData():User

    @Query("UPDATE user_table SET user_phone=:phoneNb,token=:token WHERE ID=1")
    fun updateUsersPhoneAndToken(phoneNb:String,token:String)

    @Query("UPDATE user_table SET user_phone=:phoneNb,token=:token,email=:email WHERE ID=1")
    fun updateUsersPhoneTokenEmail(phoneNb:String,token:String,email: String)

    @Query("SELECT token FROM user_table WHERE id=1")
    fun getToken():String

    @Query("SELECT user_phone FROM user_table WHERE id=1")
    fun getPhone():String

    @Query("UPDATE user_table SET email=:email WHERE ID=1")
    fun updateUserEmail(email:String)

    @Query("UPDATE user_table SET user_phone=:phoneNb,token=:token,email=:email WHERE ID=1")
    fun logout(phoneNb: String= EMPTY_PHONE_NUMBER, token: String= EMPTY_TOKEN, email: String= EMPTY_EMAIL)


    //PRENUMBER TABLE
    @Query("UPDATE e1_prenumber_table SET e1prenumber=:prenumber,timestamp=:timestamp WHERE id=1")
    fun updatePrenumber(prenumber:String,timestamp:Long)

    @Query("SELECT e1prenumber FROM e1_prenumber_table WHERE id=1")
    fun getPrenumber():LiveData<String>


    //SIP ACCOUNT TABLE
    @Query("SELECT sipCallerId FROM sip_account_table WHERE id=1")
    fun getSipCallerId():String

    @Query("UPDATE sip_account_table SET sipCallerId=:sipCallerId WHERE id=1")
    fun updateSipCallerId(sipCallerId:String)





}
