package com.vertial.sipdnidphone.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vertial.sipdnidphone.api.MyAPIService
import com.vertial.sipdnidphone.api.NetRequest_Authorization
import com.vertial.sipdnidphone.api.NetRequest_Registration
import com.vertial.sipdnidphone.database.MyDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MY_TAG="MY_Repository"
class Repo (val myDatabaseDao: MyDatabaseDao,val myAPIService: MyAPIService){

    //User Live Data
    fun getUserData()=myDatabaseDao.getUser()
    // Live Data
    fun getPremunber()=myDatabaseDao.getPrenumber()

    private val _registrationNetworkError= MutableLiveData<String>()
    val registrationNetworkError: LiveData<String>
        get() = _registrationNetworkError

    private val _registrationSuccess= MutableLiveData<String>()
    val registrationSuccess: LiveData<String>
        get() = _registrationSuccess

    private val _authorizationNetworkError= MutableLiveData<String>()
    val authorizationNetworkError: LiveData<String>
        get() = _authorizationNetworkError

    private val _authorizationSuccess= MutableLiveData<String>()
    val authorizationSuccess: LiveData<String>
        get() = _authorizationSuccess



    suspend fun sendRegistationToServer(phone:String){

        Log.i(MY_TAG,"send registration $phone")
        val defResponse=myAPIService.sendRegistrationToServer(request = NetRequest_Registration(phoneNumber = phone ))
        try{
            val result=defResponse.await()
            Log.i(MY_TAG,"uspesna registracija $result")
            _registrationSuccess.value=result.message
        }
        catch (e:Exception){
            val errorMessage:String?=e.message
            _registrationNetworkError.value=errorMessage
            Log.i(MY_TAG,"greska $errorMessage")
        }

    }

    suspend fun authorizeThisUser(phone:String,smsToken:String){

        Log.i(MY_TAG,"send authorization $phone i $smsToken")
        val defResponse=myAPIService.authorizeUser(request = NetRequest_Authorization(phoneNumber = phone,smstoken = smsToken ))
        try{
            val result=defResponse.await()
            Log.i(MY_TAG,"uspesna autorizacija $result")
            _authorizationSuccess.value=result.message
            insertTokenAndPhoneIntoDatabase(phone,result.authToken)
        }
        catch (e:Exception){
            val errorMessage:String?=e.message
            _authorizationNetworkError.value=errorMessage
            Log.i(MY_TAG,"greska $errorMessage")
        }

    }



    suspend fun insertTokenAndPhoneIntoDatabase(phone:String,token:String) {
        withContext(Dispatchers.IO) {
            myDatabaseDao.updateUsersPhoneAndToken(phone, token)
        }
    }

}