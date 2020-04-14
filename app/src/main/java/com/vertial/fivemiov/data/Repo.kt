package com.vertial.fivemiov.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vertial.fivemiov.api.*
import com.vertial.fivemiov.database.MyDatabaseDao
import kotlinx.coroutines.*


private const val MY_TAG="MY_Repository"
class Repo (val myDatabaseDao: MyDatabaseDao,val myAPI: MyAPIService){

    //User Live Data
    fun getUserData()=myDatabaseDao.getUser()

    // registration fragment
    private val _registrationNetworkError= MutableLiveData<String>()
    val registrationNetworkError: LiveData<String>
        get() = _registrationNetworkError

    private val _registrationSuccessIsNmbAssigned= MutableLiveData<Boolean>()
    val registrationSuccessIsNmbAssigned: LiveData<Boolean>
        get() = _registrationSuccessIsNmbAssigned



    // Assign number to existiong account fragment
    private val _addNumberToAccountNetworkError= MutableLiveData<String>()
    val addNumberToAccountNetworkError: LiveData<String>
        get() = _addNumberToAccountNetworkError

    private val _addNumberToAccountNetworkSuccess= MutableLiveData<String>()
    val addNumberToAccountNetworkSuccess: LiveData<String>
        get() = _addNumberToAccountNetworkSuccess



    //Number exists in DB fragment (has two buttons)
    private val _nmbExistsInDBUserHasAccountSuccess= MutableLiveData<String>()
    val nmbExistsInDBUserHasAccountSuccess: LiveData<String>
        get() = _nmbExistsInDBUserHasAccountSuccess

    private val _nmbExistsInDBUserHasAccountError= MutableLiveData<String>()
    val nmbExistsInDBUserHasAccountError: LiveData<String>
        get() = _nmbExistsInDBUserHasAccountError


    private val _nmbExistsInDB_NoAccountSuccess= MutableLiveData<String>()
    val nmbExistsInDB_NoAccountSuccess: LiveData<String>
        get() = _nmbExistsInDB_NoAccountSuccess

    private val _nmbExistsInDB_NoAccountError= MutableLiveData<String>()
    val nmbExistsInDB_NoAccountError: LiveData<String>
        get() = _nmbExistsInDB_NoAccountError



    // authorization fragment
    private val _authorizationNetworkError= MutableLiveData<String>()
    val authorizationNetworkError: LiveData<String>
        get() = _authorizationNetworkError

    private val _authorizationSuccess= MutableLiveData<String>()
    val authorizationSuccess: LiveData<String>
        get() = _authorizationSuccess

    private val _smsResendNetworkError= MutableLiveData<String>()
    val smsResendNetworkError: LiveData<String>
        get() = _smsResendNetworkError

    private val _smsResendSuccess= MutableLiveData<String>()
    val smsResendSuccess: LiveData<String>
        get() = _smsResendSuccess



    private val _setAccountEmailAndPassError= MutableLiveData<String>()
    val setAccountEmailAndPassError: LiveData<String>
        get() = _setAccountEmailAndPassError

    private val _setAccountEmailAndPassSuccess= MutableLiveData<String>()
    val setAccountEmailAndPassSuccess: LiveData<String>
        get() = _setAccountEmailAndPassSuccess


    // Registration fragment
    suspend fun sendRegistationToServer(phone:String,smsResend:Boolean){
        Log.i(MY_TAG,"send registration $phone")
        val defResponse=myAPI.sendRegistrationToServer(request = NetRequest_Registration(phoneNumber = phone ))
        try{
            val result=defResponse.await()
            Log.i(MY_TAG,"uspesna registracija $result")
            if(smsResend) _smsResendSuccess.value=result.message
            else _registrationSuccessIsNmbAssigned.value=result.phoneNumberAlreadyAssigned
        } catch (e:Throwable){
            val m=e.cause
            val errorMessage:String?=e.message
            if(smsResend) _smsResendNetworkError.value=e.message
            else _registrationNetworkError.value=e.toString()
            Log.i(MY_TAG,"greska $errorMessage, a cela gresak je $e")
        }

    }




    suspend fun assignPhoneNumberToAccount(phone:String, email:String,password:String,smsResend: Boolean=false){

        Log.i(MY_TAG,"add number to account $phone,$email,$password")

        val defResponse=myAPI.sendAddNumberToAccount(request = NetRequest_AddNumberToAccount(phoneNumber = phone,email = email,password = password ))
        try{
            val result=defResponse.await()
            Log.i(MY_TAG,"uspesno dodavanje telefona $result")

            if(smsResend) _smsResendSuccess.value=result.usermessage
            else{
                    if(result.success==true) _addNumberToAccountNetworkSuccess.value=result.usermessage
                    else  _addNumberToAccountNetworkError.value=result.message
            }

        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            if(smsResend)_smsResendNetworkError.value=errorMessage
            else _addNumberToAccountNetworkError.value=errorMessage
            Log.i(MY_TAG,"greska $errorMessage")
            Log.i(MY_TAG,"cela greska ${e}")
        }

    }

    //Authorization fragment
    suspend fun authorizeThisUser(phone:String,smsToken:String,email: String,password: String){

        Log.i(MY_TAG,"send authorization $phone i $smsToken")
        val defResponse=myAPI.authorizeUser(request = NetRequest_Authorization(phoneNumber = phone,smstoken = smsToken,email = email,password = password ))
        try{
            val result=defResponse.await()
            Log.i(MY_TAG,"uspesna autorizacija $result")

          withContext(Dispatchers.IO){
              if(result.email.isNotEmpty())myDatabaseDao.updateUsersPhoneTokenEmail(phone, result.authToken,result.email)
              else myDatabaseDao.updateUsersPhoneAndToken(phone, result.authToken)
            }

            doUncancelableJobInBacgroundAuthorization(result,myDatabaseDao,myAPI)

            _authorizationSuccess.value=result.message

        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            _authorizationNetworkError.value=errorMessage
            Log.i(MY_TAG,"greska je $e")
        }

    }

    private suspend fun doUncancelableJobInBacgroundAuthorization(result: NetResponse_Authorization, myDatabaseDao: MyDatabaseDao, myAPI: MyAPIService) {
        GlobalScope.launch {
            Log.i(MY_TAG,"doUncancelableJobInBacground")
            withContext(Dispatchers.IO) {

                resetSipAccess(result.authToken)
                Log.i(MY_TAG,"posle sip poziva")
                if(result.e1phone.isNullOrEmpty()) callSetNewE1(result.authToken)
                else myDatabaseDao.updatePrenumber(result.e1phone,System.currentTimeMillis())

                //TODO URADI FUNKCIJU callSetSipCallerID()
                /*if(result.sipCallerId.isEmpty()) callSetSipCallerID()
                else myDatabaseDao.updateSipCallerId(result.sipCallerId)*/

            }
        }
    }

    /*private suspend fun callSetSipCallerID() {
        Log.i(MY_TAG,"usao u callSetSipCallerID()")
        val defResult=myAPI.setSipCallerId(request =NetRequest_SetSipCallerId())
        try {
            val result=defResult.await()
        }catch (e:Throwable){
            Log.i(MY_TAG,"greska callSetSipCallerID() ${e.message}")
        }


    }*/


    private suspend fun resetSipAccess(authToken: String) {
        Log.i(MY_TAG,"usao u resetSipAccess")
            val defResult=myAPI.resetSipAccess(request = NetRequest_ResetSipAccess(authToken))
            try {
                val result=defResult.await()
            }catch (e:Throwable){
                Log.i(MY_TAG,"greska resetSipAccess ${e.message}")
            }
    }

    private suspend fun callSetNewE1(token: String){
        Log.i(MY_TAG,"usao u callSetNewE1")

        val defResult=myAPI.setNewE1(request = NetRequest_SetE1Prenumber(token))
                try {
                        val result=defResult.await()
                        if(!result.e1prenumber.isNullOrEmpty()){
                            withContext(Dispatchers.IO){
                                myDatabaseDao.updatePrenumber(result.e1prenumber,System.currentTimeMillis())
                            }
                        }else{}
                }catch (e:Throwable){
                    Log.i(MY_TAG,"greska ${e.message}")
                }


    }

    suspend fun resendSMS(phone: String){
        val defResponse=myAPI.sendRegistrationToServer(request = NetRequest_Registration(phoneNumber = phone ))
        try{
            val result=defResponse.await()
            _smsResendSuccess.value=result.message
        } catch (e:Throwable){
            val errorMessage:String?=e.message
            _smsResendNetworkError.value=e.message
            Log.i(MY_TAG,"greska $errorMessage, a cela gresak je $e")
        }

    }





    //Number exists in DB
    suspend fun numberExistsInDBVerifyAccount(enteredPhoneNumber:String, email:String, password:String,smsResend: Boolean=false){

        val defResult=myAPI.numberExistsInDBVerifyAccount(request = NetRequest_NmbExistsInDB_UserHasAccount(enteredPhoneNumber,email, password))
        try{
            val result=defResult.await()
            Log.i(MY_TAG,"nmb exists in DB user has account $result")

            if(smsResend) _smsResendSuccess.value=result.message
            else{

                if(result.success==true) _nmbExistsInDBUserHasAccountSuccess.value=result.message
                else _nmbExistsInDBUserHasAccountError.value=result.message

            }

        }
        catch (e:Throwable){
            val errorMessage:String?=e.message

            if(smsResend) _smsResendNetworkError.value=e.message
            else _nmbExistsInDBUserHasAccountError.value=errorMessage
            Log.i(MY_TAG,"greska nmb exists in DB user has account$e")
        }
    }

    suspend fun numberExistsInDB_NOAccount(enteredPhoneNumber:String,smsResend: Boolean=false){

        val defResult=myAPI.numberExistsInDB_NOAccount(request = NetRequest_NmbExistsInDB_NoAccount(phoneNumber = enteredPhoneNumber))
        try{
            val result=defResult.await()
            Log.i(MY_TAG,"nmb exists in DB no account $result")

            if(smsResend) _smsResendSuccess.value=result.message
            else{
                if(result.success==true)_nmbExistsInDB_NoAccountSuccess.value=result.message
                else  _nmbExistsInDB_NoAccountError.value=result.message
            }

        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            _nmbExistsInDB_NoAccountError.value=errorMessage
            Log.i(MY_TAG,"greska nmb exists in DB no account $e")
        }
    }


    // glavni deo app-a, SetAccountAndEmail Fragment
    suspend fun  setAccountEmailAndPasswordForUser(phoneNumber:String,token: String,email: String,password: String){
        Log.i(MY_TAG,"setcredentials $phoneNumber,$token,$email,$password")
        val defResult=myAPI.setAccountEmailAndPasswordForUser(request = NetRequest_SetAccountEmailAndPass(
            phoneNumber=phoneNumber,authToken= token,email = email,password = password))
        try{
            val result=defResult.await()
            Log.i(MY_TAG,"uspesno setovanje accounta $result")
            doUncancelableJobInBacgroundSetAccountCredentials(result,myDatabaseDao,myAPI)
            _setAccountEmailAndPassSuccess.value=result.message
        }
        catch (e:Exception){
            val errorMessage:String?=e.message
            _setAccountEmailAndPassError.value=errorMessage
            Log.i(MY_TAG,"greska $errorMessage")
        }
    }

    private fun doUncancelableJobInBacgroundSetAccountCredentials(result: NetResponse_SetAccountEmailAndPass, myDatabaseDao: MyDatabaseDao, myAPI: MyAPIService) {
        GlobalScope.launch {
            Log.i(MY_TAG,"doUncancelableJobInBacground SetAccountCredentials")
            withContext(Dispatchers.IO) {
                myDatabaseDao.updateUserEmail(result.email)
                val user= runBlocking {
                    myDatabaseDao.getUserNoLiveData()
                 }
                Log.i(MY_TAG,"token za usera je ${user.userToken}")

                if(result.e1phone.isNullOrEmpty()) {
                        Log.i(MY_TAG,"usao u e1 phone is not empty")
                        callSetNewE1(user.userToken)
                } else myDatabaseDao.updatePrenumber(result.e1phone,System.currentTimeMillis())


            }
        }

    }


    fun getPhoneNumberFromDB():String{
        return myDatabaseDao.getPhone()
    }


    fun getTokenFromDB():String{
        return myDatabaseDao.getToken()

    }


    suspend fun insertEmailIntoDatabase(email: String){
        Log.i(MY_TAG,"insert email into DB $email")
        withContext(Dispatchers.IO) {
            myDatabaseDao.updateUserEmail(email)
        }
    }


    suspend fun insertTokenAndPhoneIntoDatabase(phone:String,token:String) {
        withContext(Dispatchers.IO) {
            myDatabaseDao.updateUsersPhoneAndToken(phone, token)
        }
    }

}