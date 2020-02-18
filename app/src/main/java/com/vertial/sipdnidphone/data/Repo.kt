package com.vertial.sipdnidphone.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vertial.sipdnidphone.api.MyAPIService
import com.vertial.sipdnidphone.api.NetRequest_Registration
import com.vertial.sipdnidphone.database.MyDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MY_TAG="MY_Repository"
class Repo (val myDatabaseDao: MyDatabaseDao,val myAPIService: MyAPIService){

    //User Live Data
    fun getUserData()=myDatabaseDao.getUser()

    fun getPremunber()=myDatabaseDao.getPrenumber()

    private val _registrationNetworkError= MutableLiveData<String>()
    val registrationNetworkError: LiveData<String>
        get() = _registrationNetworkError



    suspend fun sendRegistationToServer(phone:String){

        Log.i(MY_TAG,"send registration $phone")
        val defResponse=myAPIService.sendRegistrationToServer(NetRequest_Registration(phoneNumber =phone ))
        try{
            val result=defResponse.await()
           insertTokenAndPhoneIntoDatabase(phone,result.token)
        }
        catch (e:Exception){
            val errorMessage:String?=e.message
            _registrationNetworkError.value=errorMessage
        }

    }

    suspend fun insertTokenAndPhoneIntoDatabase(phone:String,token:String) {
        withContext(Dispatchers.IO) {
            myDatabaseDao.updateUsersPhoneAndToken(phone, token)
        }
    }

}