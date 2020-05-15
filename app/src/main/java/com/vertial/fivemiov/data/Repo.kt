package com.vertial.fivemiov.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vertial.fivemiov.api.*
import com.vertial.fivemiov.database.MyDatabaseDao
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.lang.Exception


private const val MY_TAG="MY_Repository"
class Repo (val myDatabaseDao: MyDatabaseDao,val myAPI: MyAPIService){

    //User Live Data
    fun getUserData()=myDatabaseDao.getUser()

    // registration fragment
    private val _registrationNetworkError= MutableLiveData<String?>()
    val registrationNetworkError: LiveData<String?>
        get() = _registrationNetworkError

    private val _registrationSuccess= MutableLiveData<NetResponse_Registration?>()
    val registrationSuccess: LiveData<NetResponse_Registration?>
        get() = _registrationSuccess



    // Assign number to existiong account fragment
    private val _addNumberToAccountNetworkError= MutableLiveData<String?>()
    val addNumberToAccountNetworkError: LiveData<String?>
        get() = _addNumberToAccountNetworkError

    private val _addNumberToAccountNetworkSuccess= MutableLiveData<NetResponse_AddNumberToAccount?>()
    val addNumberToAccountNetworkSuccess: LiveData<NetResponse_AddNumberToAccount?>
        get() = _addNumberToAccountNetworkSuccess



    //Number exists in DB fragment (has two buttons)
    private val _nmbExistsInDBUserHasAccountSuccess= MutableLiveData<NetResponse_NmbExistsInDB?>()
    val nmbExistsInDBUserHasAccountSuccess: LiveData<NetResponse_NmbExistsInDB?>
        get() = _nmbExistsInDBUserHasAccountSuccess

    private val _nmbExistsInDBUserHasAccountError= MutableLiveData<String?>()
    val nmbExistsInDBUserHasAccountError: LiveData<String?>
        get() = _nmbExistsInDBUserHasAccountError


    private val _nmbExistsInDB_NoAccountSuccess= MutableLiveData<NetResponse_NmbExistsInDB?>()
    val nmbExistsInDB_NoAccountSuccess: LiveData<NetResponse_NmbExistsInDB?>
        get() = _nmbExistsInDB_NoAccountSuccess

    private val _nmbExistsInDB_NoAccountError= MutableLiveData<String?>()
    val nmbExistsInDB_NoAccountError: LiveData<String?>
        get() = _nmbExistsInDB_NoAccountError



    // authorization fragment
    private val _authorizationNetworkError= MutableLiveData<String?>()
    val authorizationNetworkError: LiveData<String?>
        get() = _authorizationNetworkError

    private val _authorizationSuccess= MutableLiveData<NetResponse_Authorization?>()
    val authorizationSuccess: LiveData<NetResponse_Authorization?>
        get() = _authorizationSuccess

    private val _smsResendNetworkError= MutableLiveData<String?>()
    val smsResendNetworkError: LiveData<String?>
        get() = _smsResendNetworkError

    private val _smsResendSuccess= MutableLiveData<String?>()
    val smsResendSuccess: LiveData<String?>
        get() = _smsResendSuccess



    // Glavni deo App-a
    private val _setAccountEmailAndPassError= MutableLiveData<String?>()
    val setAccountEmailAndPassError: LiveData<String?>
        get() = _setAccountEmailAndPassError

    private val _setAccountEmailAndPassSuccess= MutableLiveData<String?>()
    val setAccountEmailAndPassSuccess: LiveData<String?>
        get() = _setAccountEmailAndPassSuccess




    // Registration fragment
    suspend fun sendRegistationToServer(phone:String,smsResend:Boolean){
        Log.i(MY_TAG,"send registration $phone")
        val defResponse=myAPI.sendRegistrationToServer(request = NetRequest_Registration(phoneNumber = phone ))
        try{

            val result:NetResponse_Registration=defResponse.await()
            Log.i(MY_TAG,"uspesna registracija $result")

            if(smsResend) _smsResendSuccess.value=result.userMessage
            else _registrationSuccess.value=result

        } catch (e:Throwable){
            val errorMessage:String?=e.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,"sendRegistationToServer,$phone,smsResend_$smsResend",e.message.toString()).sendError()
                } }
            if(smsResend) _smsResendNetworkError.value=e.message
            else _registrationNetworkError.value=e.toString()
            Log.i(MY_TAG,"greska $errorMessage, a cela gresak je $e")
        }

    }

    //Registration fragment  funkcije resetovanja stanja
    fun resetRegistrationNetSuccess(){
        _registrationSuccess.value=null
    }

    fun resetRegistrationNetError(){
        _registrationNetworkError.value=null
    }





    // ASSIGN NUMBER TO ACCOUNT
    suspend fun assignPhoneNumberToAccount(phone:String, email:String,password:String,smsResend: Boolean=false){

        Log.i(MY_TAG,"add number to account $phone,$email,$password")

        val defResponse=myAPI.sendAddNumberToAccount(request = NetRequest_AddNumberToAccount(phoneNumber = phone,email = email,password = password ))
        try{
            val result=defResponse.await()
            Log.i(MY_TAG,"uspesno dodavanje telefona $result")

            if(smsResend) _smsResendSuccess.value=result.usermessage
            else _addNumberToAccountNetworkSuccess.value=result

        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,"assignPhoneNumberToAccount $phone, $email,$password smsResend $smsResend",e.message.toString()).sendError()
                } }
            if(smsResend)_smsResendNetworkError.value=errorMessage
            else _addNumberToAccountNetworkError.value=errorMessage
            Log.i(MY_TAG,"cela greska ${e}")
        }

    }

    //ASSIGN NUMBER TO ACCOUNT fragment  funkcije resetovanja stanja
    fun resetAssignPhoneNumberToAccountNetSuccess(){
        _addNumberToAccountNetworkSuccess.value=null
    }

    fun resetAssignPhoneNumberToAccountNetError(){
        _addNumberToAccountNetworkError.value=null
    }


    //NUMBER EXISTS IN DB  fragment
    suspend fun numberExistsInDBVerifyAccount(enteredPhoneNumber:String, email:String, password:String,smsResend: Boolean=false){

        val defResult=myAPI.numberExistsInDBVerifyAccount(request = NetRequest_NmbExistsInDB_UserHasAccount(enteredPhoneNumber,email, password))
        try{
            val result=defResult.await()
            Log.i(MY_TAG,"nmb exists in DB user has account $result")

            if(smsResend) _smsResendSuccess.value=result.userMessage
            else _nmbExistsInDBUserHasAccountSuccess.value=result
        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,"numberExistsInDBVerifyAccount $enteredPhoneNumber $email,$password smsResend $smsResend",e.message.toString()).sendError()
                } }
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

            if(smsResend) _smsResendSuccess.value=result.userMessage
            else _nmbExistsInDB_NoAccountSuccess.value=result
        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,"numberExistsInDB_NOAccount $enteredPhoneNumber, smsResend $smsResend",e.message.toString()).sendError()
                } }
            _nmbExistsInDB_NoAccountError.value=errorMessage
            Log.i(MY_TAG,"greska nmb exists in DB no account $e")
        }
    }

    //funkcije za resetovanje stanja za Number Exists in DB

    fun resetNmbExistsInDB_VerifyAccount_NetSuccess(){
        _nmbExistsInDBUserHasAccountSuccess.value=null
    }

    fun resetNmbExistsInDB_VerifyAccount_NetError(){
        _nmbExistsInDBUserHasAccountError.value=null
    }

    fun resetNmbExistsInDB_NOAccount_NetSuccess(){
        _nmbExistsInDB_NoAccountSuccess.value=null
    }

    fun resetNmbExistsInDB_NOAccount_NetError(){
        _nmbExistsInDB_NoAccountError.value=null
    }



    //Authorization fragment
    suspend fun authorizeThisUser(phone:String,smsToken:String,email: String,password: String){

        Log.i(MY_TAG,"send authorization $phone i $smsToken")
        val defResponse=myAPI.authorizeUser(request = NetRequest_Authorization(phoneNumber = phone,smstoken = smsToken,email = email,password = password ))
        try{
            val result=defResponse.await()
            Log.i(MY_TAG,"uspesna autorizacija $result")
            if(result.success==true) {
                val uncancelableJob = UncancelableJob(
                    phone=phone,
                    resultAuthorization = result,
                    resultSetAccountEmailAndPass = null,
                    myDatabaseDao = myDatabaseDao,
                    myAPI = myAPI
                )

                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        //delay(3000)
                        uncancelableJob.startAuthorizationJob()

                    }
                }

                withContext(Dispatchers.IO) {
                    if (result.email.isNotEmpty()
                        && result.email.isNotBlank()
                        && result.authToken.isNotEmpty()
                        && result.authToken.isNotBlank()
                        ) myDatabaseDao.updateUsersPhoneTokenEmail(phone, result.authToken, result.email)
                    else if(result.authToken.isNotEmpty() && result.authToken.isNotBlank()) myDatabaseDao.updateUsersPhoneAndToken(phone, result.authToken)
                }
            }

            _authorizationSuccess.value=result

        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,"authorizeThisUser,$phone,$smsToken,$email,$password",e.message.toString()).sendError()
                } }
            _authorizationNetworkError.value=errorMessage
            Log.i(MY_TAG,"greska je $e")
        }

    }

    //Authorization fragment reset funcije
    fun resetAuthorization_NetSuccess(){
        _authorizationSuccess.value=null
    }

    fun resetAuthorization_NetError(){
        _authorizationNetworkError.value=null
    }

    fun resetSMSResend_NetSuccess(){
        _smsResendSuccess.value=null
    }

    fun resetSMSResend_NetError(){
        _smsResendNetworkError.value=null
    }


    /*suspend fun resendSMS(phone: String){
        val defResponse=myAPI.sendRegistrationToServer(request = NetRequest_Registration(phoneNumber = phone ))
        try{
            val result=defResponse.await()
            _smsResendSuccess.value=result.message
        } catch (e:Throwable){
            val errorMessage:String?=e.message
            _smsResendNetworkError.value=e.message
            Log.i(MY_TAG,"greska $errorMessage, a cela gresak je $e")
        }

    }*/





    // glavni deo app-a, SetAccountAndEmail Fragment
    suspend fun  setAccountEmailAndPasswordForUser(phoneNumber:String,token: String,email: String,password: String){
        Log.i(MY_TAG,"setcredentials $phoneNumber,$token,$email,$password")

        val defResult=myAPI.setAccountEmailAndPasswordForUser(request = NetRequest_SetAccountEmailAndPass(
            phoneNumber=phoneNumber,authToken= token,email = email,password = password))
        try {
            val result = defResult.await()
            Log.i(MY_TAG, "uspesno setovanje accounta $result")

                if (result.success == true) {
                    val myUncancelableJob: UncancelableJob = UncancelableJob(
                        phone = phoneNumber,
                        resultAuthorization = null,
                        resultSetAccountEmailAndPass = result,
                        myDatabaseDao = myDatabaseDao,
                        myAPI = myAPI
                    )

                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            //delay(3000)
                            myUncancelableJob.startSetAccountEmailAndPassUncancelableJob(token)
                        }
                    }

                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            //delay(3000)
                            Log.i(MY_TAG, "prosao delay pre upisa u bazu email-a")
                            if (result.email.isNotEmpty() && result.email.isNotBlank()) myDatabaseDao.updateUserEmail(result.email)

                        }
                    }

                }
                _setAccountEmailAndPassSuccess.value = result.userMsg

        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,"setAccountEmailAndPasswordForUser $phoneNumber, $token, $email, $password",e.message.toString()).sendError()
                } }
            _setAccountEmailAndPassError.value=errorMessage
            Log.i(MY_TAG,"greska $e")
        }
    }

    //Reset Funkcije
    fun resetSetAccountEmailAndPassNetSuccess(){
        _setAccountEmailAndPassSuccess.value=null
    }

    fun resetSetAccountEmailAndPassNetError(){
        _setAccountEmailAndPassError.value=null
    }

    suspend fun getUser()=withContext(Dispatchers.IO){
        myDatabaseDao.getUserNoLiveData()

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

class UncancelableJob(
                        val phone: String,
                        val resultAuthorization:NetResponse_Authorization?,
                        val resultSetAccountEmailAndPass:NetResponse_SetAccountEmailAndPass?,
                        val myDatabaseDao: MyDatabaseDao,
                        val myAPI: MyAPIService){

    val MY_TAG="MY_klasaUncanceJOb"

    suspend fun startAuthorizationJob(){
        Log.i(MY_TAG,"unceleable job je started $resultAuthorization")
        if(resultAuthorization!=null){

            if(resultAuthorization.authToken.isNotEmpty() && resultAuthorization.authToken.isNotBlank()) {

                if(resultAuthorization.sipReady==false) resetSipAccess(resultAuthorization.authToken)

                if (resultAuthorization.e1phone.isNullOrEmpty() || resultAuthorization.e1phone.isBlank()) callSetNewE1(
                    phone = phone,
                    token = resultAuthorization.authToken
                )
                else {
                    myDatabaseDao.updatePrenumber(
                        resultAuthorization.e1phone,
                        System.currentTimeMillis()
                    )
                    Log.i(MY_TAG, "authorize updating e1 phone version in DB")
                }

                if (!resultAuthorization.appVersion.isNullOrEmpty() && !resultAuthorization.appVersion.isNullOrBlank()) {
                    myDatabaseDao.updateWebApiVersion(resultAuthorization.appVersion)
                    Log.i(MY_TAG, "authorize updating web api version in DB")
                }
            }
        }
    }


    suspend fun startSetAccountEmailAndPassUncancelableJob(token: String){

        if(resultSetAccountEmailAndPass!=null){

            if(resultSetAccountEmailAndPass.e1phone.isNullOrEmpty() || resultSetAccountEmailAndPass.e1phone.isNullOrBlank() ) callSetNewE1(phone=phone,token=token)
            else  { myDatabaseDao.updatePrenumber(resultSetAccountEmailAndPass.e1phone,System.currentTimeMillis())
                Log.i(MY_TAG, "set account updating e1 phone version in DB") }

            if(!resultSetAccountEmailAndPass.appVersion.isNullOrEmpty() || !resultSetAccountEmailAndPass.appVersion.isNullOrBlank()) {
                myDatabaseDao.updateWebApiVersion(resultSetAccountEmailAndPass.appVersion)
                Log.i(MY_TAG, "set account updating web api version in DB")}
        }

    }

    suspend fun startRefreshE124HPassed(token: String){
        callSetNewE1(phone=phone,token = token)

    }

    private suspend fun resetSipAccess(
        authToken: String
    ) {
        Log.i(MY_TAG,"usao u resetSipAccess")
        val defResult= myAPI.resetSipAccess(request = NetRequest_ResetSipAccess(authToken=authToken,phoneNumber = phone))
        try {
            val result=defResult.await()
            if(result.authTokenMismatch==true) logoutAll(myDatabaseDao)
        }catch (e:Throwable){
            Log.i(MY_TAG,"greska resetSipAccess ${e.message}")
        }
    }

    suspend fun callSetNewE1(phone: String,token: String) {
        Log.i(MY_TAG, "usao u callSetNewE1")

        val defResult = myAPI.setNewE1(
            request = NetRequest_SetE1Prenumber(
                authToken = token,
                phoneNumber = phone
            )
        )
        try {
            val result = defResult.await()

            if (result.authTokenMismatch == true) logoutAll(myDatabaseDao)
            else{
                    if (!result.e1prenumber.isNullOrEmpty() && !result.e1prenumber.isNullOrBlank()) {
                        myDatabaseDao.updatePrenumber(result.e1prenumber, System.currentTimeMillis())
                    }

                    if (!result.appVersion.isNullOrEmpty() && !result.appVersion.isNullOrBlank()) {
                        myDatabaseDao.updateWebApiVersion(result.appVersion)
                    }
            }
        }catch (e:Throwable){
            Log.i(MY_TAG,"greska callSetNEw E1 ${e.message}")
        }

    }


}

