package com.vertial.fivemiov.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vertial.fivemiov.api.*
import com.vertial.fivemiov.database.MyDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MY_TAG="MY_Repository"
class Repo (val myDatabaseDao: MyDatabaseDao,val myAPI: MyAPIService){

    //User Live Data
    fun getUserData()=myDatabaseDao.getUser()


    private val _registrationNetworkError= MutableLiveData<String>()
    val registrationNetworkError: LiveData<String>
        get() = _registrationNetworkError

    private val _registrationSuccessIsNmbAssigned= MutableLiveData<Boolean>()
    val registrationSuccessIsNmbAssigned: LiveData<Boolean>
        get() = _registrationSuccessIsNmbAssigned



    private val _addNumberToAccountNetworkError= MutableLiveData<String>()
    val addNumberToAccountNetworkError: LiveData<String>
        get() = _addNumberToAccountNetworkError

    private val _addNumberToAccountNetworkSuccess= MutableLiveData<String>()
    val addNumberToAccountNetworkSuccess: LiveData<String>
        get() = _addNumberToAccountNetworkSuccess



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


    suspend fun sendRegistationToServer(phone:String){
        Log.i(MY_TAG,"send registration $phone")
        val defResponse=myAPI.sendRegistrationToServer(request = NetRequest_Registration(phoneNumber = phone ))
        try{
            val result=defResponse.await()
            val assigned=result.phoneNumberAlreadyAssigned
            Log.i(MY_TAG,"uspesna registracija $result")
            _registrationSuccessIsNmbAssigned.value=result.phoneNumberAlreadyAssigned
        } catch (e:Throwable){
            val m=e.cause
            val errorMessage:String?=e.message
            _registrationNetworkError.value=e.toString()
            Log.i(MY_TAG,"greska $errorMessage, a cela gresak je $e")
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


    suspend fun assignPhoneNumberToAccount(phone:String, email:String,password:String){

        Log.i(MY_TAG,"add number to account $phone,$email,$password")

        val defResponse=myAPI.sendAddNumberToAccount(request = NetRequest_AddNumberToAccount(phoneNumber = phone,email = email,password = password ))
        try{
            val result=defResponse.await()
            Log.i(MY_TAG,"uspesno dodavanje telefona $result")
            _addNumberToAccountNetworkSuccess.value=result.message
        }
        catch (e:Exception){
            val errorMessage:String?=e.message
            _addNumberToAccountNetworkError.value=errorMessage
            Log.i(MY_TAG,"greska $errorMessage")
            Log.i(MY_TAG,"greska ${e.localizedMessage}")
        }

    }


    suspend fun authorizeThisUser(phone:String,smsToken:String,email: String,password: String){

        Log.i(MY_TAG,"send authorization $phone i $smsToken")
        val defResponse=myAPI.authorizeUser(request = NetRequest_Authorization(phoneNumber = phone,smstoken = smsToken,email = email,password = password ))
        try{
            val result=defResponse.await()
            Log.i(MY_TAG,"uspesna autorizacija $result")
            _authorizationSuccess.value=result.message

            insertTokenAndPhoneIntoDatabase(phone,result.authToken)
            if(email.isNotEmpty()) insertEmailIntoDatabase(email)

        }
        catch (e:Exception){
            val errorMessage:String?=e.message
            _authorizationNetworkError.value=errorMessage
            Log.i(MY_TAG,"greska $errorMessage")
        }

    }

    suspend fun  setAccountEmailAndPasswordForUser(phoneNumber:String,token: String,email: String,password: String){
        Log.i(MY_TAG,"setcredentials $phoneNumber,$token,$email,$password")
        val defResult=myAPI.setAccountEmailAndPasswordForUser(request = NetRequest_SetAccountEmailAndPass(
                                                                                            phoneNumber=phoneNumber,authToken= token,email = email,password = password))
        try{
            val result=defResult.await()
            Log.i(MY_TAG,"uspesno setovanje accounta $result")
            _setAccountEmailAndPassSuccess.value=result.message
            insertEmailIntoDatabase(email)
        }
        catch (e:Exception){
            val errorMessage:String?=e.message
            _setAccountEmailAndPassError.value=errorMessage
            Log.i(MY_TAG,"greska $errorMessage")
        }
    }



    suspend fun numberExistsInDBVerifyAccount(enteredPhoneNumber:String, email:String, password:String){

        val defResult=myAPI.numberExistsInDBVerifyAccount(request = NetRequest_NmbExistsInDB_UserHasAccount(enteredPhoneNumber,email, password))
        try{
            val result=defResult.await()
            Log.i(MY_TAG,"nmb exists in DB user has account $result")
            _nmbExistsInDBUserHasAccountSuccess.value=result.message

        }
        catch (e:Exception){
            val errorMessage:String?=e.message
            _nmbExistsInDBUserHasAccountError.value=errorMessage
            Log.i(MY_TAG,"greska nmb exists in DB user has account$errorMessage")
        }
    }

    suspend fun numberExistsInDB_NOAccount(enteredPhoneNumber:String){

        val defResult=myAPI.numberExistsInDB_NOAccount(request = NetRequest_NmbExistsInDB_NoAccount(phoneNumber = enteredPhoneNumber))
        try{
            val result=defResult.await()
            Log.i(MY_TAG,"nmb exists in DB no account $result")
            _nmbExistsInDB_NoAccountSuccess.value=result.message

        }
        catch (e:Exception){
            val errorMessage:String?=e.message
            _nmbExistsInDB_NoAccountError.value=errorMessage
            Log.i(MY_TAG,"greska nmb exists in DB no account$errorMessage")
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