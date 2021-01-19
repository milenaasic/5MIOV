package app.adinfinitum.ello.database

import androidx.lifecycle.LiveData
import androidx.room.*
import app.adinfinitum.ello.model.*
import app.adinfinitum.ello.utils.*

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

    @Query("UPDATE user_table SET email=:email WHERE id=1")
    fun updateUserEmail(email:String)

    @Query("UPDATE user_table SET user_phone=:phoneNb,token=:token,email=:email,password=:password WHERE id=1")
    fun logoutUser(phoneNb: String= EMPTY_PHONE_NUMBER, token: String= EMPTY_TOKEN, email: String= EMPTY_EMAIL,password:String= EMPTY_PASSWORD)


    //PRENUMBER TABLE
    @Query("UPDATE e1_prenumber_table SET e1prenumber=:prenumber,timestamp=:timestamp WHERE id=1")
    fun updatePrenumber(prenumber:String,timestamp:Long)

    @Query("SELECT e1prenumber FROM e1_prenumber_table WHERE id=1")
    fun getPrenumber():LiveData<String>

    @Query("SELECT timestamp FROM e1_prenumber_table WHERE id=1")
    fun getE1Timestamp():Long

    @Query("UPDATE e1_prenumber_table SET e1prenumber=:prenumber,timestamp=:timestamp WHERE id=1")
    fun logoutE1Table(prenumber: String= EMPTY_E1_PRENUMBER, timestamp: Long=0L)

    @Query("SELECT * FROM e1_prenumber_table")
    fun getAllE1(): List<E1Prenumber>




    //SIP ACCOUNT TABLE
    @Query("SELECT mainSipCallerId FROM sip_account_table WHERE id=1")
    fun getmainSipCallerId():String

    @Query("UPDATE sip_account_table SET mainSipCallerId=:sipCallerId WHERE id=1")
    fun updateMainSipCallerId(sipCallerId:String)

    @Query("UPDATE sip_account_table SET mainSipCallerId=:mainsipCallerId,sipUserName=:sipUsername,sipPassword=:sipPassword,sipServer=:sipServer WHERE id=1")
    fun logoutSipAccount(mainsipCallerId:String= EMPTY_MAIN_SIP_CALLER_ID, sipUsername:String= EMPTY_SIP_USERNAME,sipPassword:String= EMPTY_SIP_PASSWORD,sipServer:String= EMPTY_SIP_SERVER)

    @Query("SELECT * FROM sip_account_table")
    fun getAllSip(): List<SipAccount>


    //WebAPI version TABLE
    @Query("UPDATE webapi_version_table SET webApiVersion=:webApiVer WHERE id=1")
    fun updateWebApiVersion(webApiVer:String)

    @Query("SELECT webApiVersion FROM webapi_version_table WHERE id=1")
    fun getWebApiVersion():String

    @Query("UPDATE webapi_version_table SET webApiVersion=:webApiVer WHERE id=1")
    fun logoutWebApiVersion(webApiVer:String="0.0")

    @Query("SELECT * FROM webapi_version_table")
    fun getAllWebAppVer(): List<WebApiVersion>


    //RECENT CALLS TABLE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentCall(vararg recentCall: RecentCall)

    @Delete
    fun deleteRecentCalls(vararg recentCall: RecentCall)

    @Query("SELECT * FROM recent_calls_table")
    fun getAllRecentCalls(): LiveData<List<RecentCall>>

    @Query("DELETE FROM recent_calls_table")
    fun logoutRecentCalls()

    //e1_and_call_verif_enabled_countries_table
    @Query("UPDATE e1_and_call_verif_enabled_countries_table SET e1_enabled_countries=:e1EnabledCountries")
    fun updateE1EnabledCoutries(e1EnabledCountries:String)

    @Query("UPDATE e1_and_call_verif_enabled_countries_table SET call_verification_enabled_countries=:callVerificationEnabledCountries")
    fun updateCallVerificationEnabledCoutries(callVerificationEnabledCountries:String)

    @Query("SELECT * FROM e1_and_call_verif_enabled_countries_table")
    fun getCountriesWithVerificationCallEnabled():E1andCallVerificationEnabledCountries
}

@Dao
interface MyDatabaseUser {

    //USER TABLE
    @Query("SELECT * FROM user_table WHERE id=1")
    fun getUser():LiveData<User>

    @Query("SELECT * FROM user_table WHERE id=1")
    fun getUserNoLiveData():User

    @Query("UPDATE user_table SET user_phone=:phoneNb,token=:token WHERE ID=1")
    fun updateUsersPhoneAndToken(phoneNb:String,token:String)

    @Query("UPDATE user_table SET user_phone=:phoneNb,token=:token,email=:email WHERE ID=1")
    fun updateUsersPhoneTokenEmail(phoneNb:String,token:String,email: String):Int

    @Query("SELECT token FROM user_table WHERE id=1")
    fun getToken():String

    @Query("SELECT user_phone FROM user_table WHERE id=1")
    fun getPhone():String

    @Query("UPDATE user_table SET email=:email WHERE id=1")
    fun updateUserEmail(email:String)

    @Query("UPDATE user_table SET user_phone=:phoneNb,token=:token,email=:email,password=:password WHERE id=1")
    fun logoutUser(phoneNb: String= EMPTY_PHONE_NUMBER, token: String= EMPTY_TOKEN, email: String= EMPTY_EMAIL,password:String= EMPTY_PASSWORD)

}

@Dao
interface MyDatabasePrenumberAndWebApiVersion {

    //PRENUMBER TABLE
    @Query("UPDATE e1_prenumber_table SET e1prenumber=:prenumber,timestamp=:timestamp WHERE id=1")
    fun updatePrenumber(prenumber: String, timestamp: Long)

    @Query("SELECT e1prenumber FROM e1_prenumber_table WHERE id=1")
    fun getPrenumber(): LiveData<String>

    @Query("SELECT timestamp FROM e1_prenumber_table WHERE id=1")
    fun getE1Timestamp(): Long

    @Query("UPDATE e1_prenumber_table SET e1prenumber=:prenumber,timestamp=:timestamp WHERE id=1")
    fun logoutE1Table(prenumber: String = EMPTY_E1_PRENUMBER, timestamp: Long = 0L)

    @Query("SELECT * FROM e1_prenumber_table")
    fun getAllE1(): List<E1Prenumber>


    //WebAPI version TABLE
    @Query("UPDATE webapi_version_table SET webApiVersion=:webApiVer WHERE id=1")
    fun updateWebApiVersion(webApiVer: String)

    @Query("SELECT webApiVersion FROM webapi_version_table WHERE id=1")
    fun getWebApiVersion(): String

    @Query("UPDATE webapi_version_table SET webApiVersion=:webApiVer WHERE id=1")
    fun logoutWebApiVersion(webApiVer: String = "0.0")

    @Query("SELECT * FROM webapi_version_table")
    fun getAllWebAppVer(): List<WebApiVersion>
}


@Dao
interface MyDatabaseRecentCalls{
    //RECENT CALLS TABLE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentCall(vararg recentCall: RecentCall)

    @Delete
    fun deleteRecentCalls(vararg recentCall: RecentCall)

    @Query("SELECT * FROM recent_calls_table")
    fun getAllRecentCalls(): LiveData<List<RecentCall>>

    @Query("DELETE FROM recent_calls_table")
    fun logoutRecentCalls()

}

@Dao
interface MyDatabaseSIPCredentials{

    //SIP ACCOUNT TABLE
    @Query("SELECT mainSipCallerId FROM sip_account_table WHERE id=1")
    fun getmainSipCallerId():String

    @Query("UPDATE sip_account_table SET mainSipCallerId=:sipCallerId WHERE id=1")
    fun updateMainSipCallerId(sipCallerId:String)

    @Query("UPDATE sip_account_table SET mainSipCallerId=:mainsipCallerId,sipUserName=:sipUsername,sipPassword=:sipPassword,sipServer=:sipServer WHERE id=1")
    fun logoutSipAccount(mainsipCallerId:String= EMPTY_MAIN_SIP_CALLER_ID, sipUsername:String= EMPTY_SIP_USERNAME,sipPassword:String= EMPTY_SIP_PASSWORD,sipServer:String= EMPTY_SIP_SERVER)

    @Query("SELECT * FROM sip_account_table")
    fun getAllSip(): List<SipAccount>
}