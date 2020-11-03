package app.adinfinitum.ello.ui.registrationauthorization

import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.api.NetResponse_AddNumberToAccount
import app.adinfinitum.ello.api.NetResponse_Authorization
import app.adinfinitum.ello.api.NetResponse_NmbExistsInDB
import app.adinfinitum.ello.api.NetResponse_Registration
import app.adinfinitum.ello.data.Repo
import app.adinfinitum.ello.data.RepoSIPE1
import app.adinfinitum.ello.data.Result
import app.adinfinitum.ello.database.MyDatabase
import app.adinfinitum.ello.ui.myapplication.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MY_TAG="MY_RegAuthActivVieModel"

class RegAuthActivityViewModel(val myRepository: Repo, val mySIPE1Repo:RepoSIPE1,application: Application) : AndroidViewModel(application) {

    var enteredPhoneNumber: String?=null
    var enteredEmail:String?=null
    var enteredPassword:String?=null
    var signInParameter:Boolean?=null


    val userData=myRepository.getUserData()

    // Registration fragment
    private val _registrationNetworkResponseSuccess= MutableLiveData<NetResponse_Registration?>()
    val registrationNetworkResponseSuccess: LiveData<NetResponse_Registration?>
        get() = _registrationNetworkResponseSuccess

    private val _registrationNetworkResponseError= MutableLiveData<String?>()
    val registrationNetworkResponseError: LiveData<String?>
        get() = _registrationNetworkResponseError


    //Add Number to Account fragment
    private val _addNumberToAccuntNetworkSuccess= MutableLiveData<NetResponse_AddNumberToAccount?>()
    val addNumberToAccuntNetworkSuccess: LiveData<NetResponse_AddNumberToAccount?>
        get() = _addNumberToAccuntNetworkSuccess

    private val _addNumberToAccuntNetworkError= MutableLiveData<String?>()
    val addNumberToAccuntNetworkError: LiveData<String?>
        get() = _addNumberToAccuntNetworkError


    //PhoneNumber exists in DB
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



    //Authorization fragment
    private val _authorizationNetworkSuccess= MutableLiveData<NetResponse_Authorization?>()
    val authorizationNetworkSuccess: LiveData<NetResponse_Authorization?>
        get() = _authorizationNetworkSuccess

    private val _authorizationNetworkError= MutableLiveData<String?>()
    val authorizationNetworkError: LiveData<String?>
        get() = _authorizationNetworkError

    private val _smsResendNetworkSuccess= MutableLiveData<String?>()
    val smsResendNetworkSuccess: LiveData<String?>
        get() = _smsResendNetworkSuccess

    private val _smsResendNetworkError= MutableLiveData<String?>()
    val smsResendNetworkError: LiveData<String?>
        get() = _smsResendNetworkError



    //SMS Retreiver for Activity to observe
    private val _startSMSRetreiver= MutableLiveData<Boolean>()
    val startSMSRetreiver: LiveData<Boolean>
        get() = _startSMSRetreiver

    //SMS Retreiver for Authorization fragment to observe
    private val _verificationTokenForAuthFragment= MutableLiveData<String?>()
    val verificationTokenForAuthFragment: LiveData<String?>
        get() = _verificationTokenForAuthFragment




    init {
        Log.i(MY_TAG,("init"))
    }

    fun resetSignUpParameters(){
        Log.i(MY_TAG,"reset user params BEFORE RESET: $enteredPhoneNumber, $enteredEmail, $enteredPassword, $signInParameter")
        enteredEmail=null
        enteredPassword=null
        signInParameter=null
        Log.i(MY_TAG,"reset user params AFTER RESET $enteredPhoneNumber,$enteredEmail, $enteredPassword, $signInParameter")
    }


    //registration fragment
    fun registerButtonClicked(phoneNumber:String, smsResend:Boolean, verificationMethod:String){
        enteredPhoneNumber=phoneNumber

        //send registrtion phone number to server and go to authorization fragment
        viewModelScope.launch {
            try {

                val result = withContext(Dispatchers.IO) {
                    myRepository.sendRegistationToServer(
                        phone = phoneNumber,
                        smsResend = smsResend,
                        verificationMethod = verificationMethod
                    )
                }

                Log.i(MY_TAG,"registerButtonClicked, ${result}")
                when(smsResend){
                    false->{
                            when(result){
                                is Result.Success->_registrationNetworkResponseSuccess.value=result.data
                                is Result.Error->_registrationNetworkResponseError.value=result.exception.message
                            }
                    }
                    true->{
                        when(result){
                            is Result.Success->_smsResendNetworkSuccess.value=result.data.code.toString()+result.data.userMessage
                            is Result.Error->_smsResendNetworkError.value=result.exception.message
                        }
                    }

                }

            }catch ( e:Exception){
                Log.i(MY_TAG,"registerButtonClickedError, ${e.message}")
            }

         }
    }

   fun resetRegistrationNetSuccess(){
        _registrationNetworkResponseSuccess.value=null
    }

    fun resetRegistrationNetErrorr(){
        _registrationNetworkResponseError.value=null
    }



    //add number to existing account fragment
    fun addNumberToAccountButtonClicked(phoneNumber:String,email:String,password:String,smsResend: Boolean=false,verificationMethod: String){
        enteredPhoneNumber=phoneNumber
        enteredEmail=email
        enteredPassword=password

        //send  phone, email and pass to server
        viewModelScope.launch {
            val result= withContext(Dispatchers.IO){
                        myRepository.assignPhoneNumberToAccount(
                                    phone = phoneNumber,
                                    email = email,
                                    password = password,
                                    smsResend = smsResend,
                                    verificationMethod = verificationMethod
                                    )
            }

            when(smsResend){
                false->{
                    when(result){
                        is Result.Success->_addNumberToAccuntNetworkSuccess.value=result.data
                        is Result.Error->_addNumberToAccuntNetworkError.value=result.exception.message
                    }
                }
                true->{
                    when(result){
                        is Result.Success->_smsResendNetworkSuccess.value=result.data.code.toString()+result.data.usermessage
                        is Result.Error->_smsResendNetworkError.value=result.exception.message
                    }
                }

            }

        }
    }

    fun resetAddNumberToAccountNetSuccess() {
        _addNumberToAccuntNetworkSuccess.value=null
    }

    fun resetAddNumberToAccountNetError(){
        _addNumberToAccuntNetworkError.value=null
    }




    //number exists in DB fragment
    fun numberExistsInDBVerifyAccount(email: String,password: String,smsResend: Boolean=false,verificationMethod: String){
        enteredEmail=email
        enteredPassword=password
        if(enteredPhoneNumber!=null) {
            viewModelScope.launch {
               val result= withContext(Dispatchers.IO) {
                   myRepository.numberExistsInDBVerifyAccount(
                       enteredPhoneNumber ?: "",
                       email,
                       password,
                       smsResend,
                       verificationMethod = verificationMethod
                   )
               }

                when(smsResend){
                    false->{
                        when(result){
                            is Result.Success->_nmbExistsInDBUserHasAccountSuccess.value=result.data
                            is Result.Error->_nmbExistsInDBUserHasAccountError.value=result.exception.message
                        }
                    }
                    true->{
                        when(result){
                            is Result.Success->_smsResendNetworkSuccess.value=result.data.code.toString()+result.data.userMessage
                            is Result.Error->_smsResendNetworkError.value=result.exception.message
                        }
                    }

                }

            }
        }
    }

    fun numberExistsInDb_NoAccount(smsResend: Boolean=false,verificationMethod: String){
         if(enteredPhoneNumber!=null) {
                viewModelScope.launch {
                    val result = myRepository.numberExistsInDB_NOAccount(
                        enteredPhoneNumber ?: "",
                        smsResend,
                        verificationMethod = verificationMethod
                    )

                    when(smsResend){
                        false->{
                            when (result) {
                                is Result.Success -> _nmbExistsInDB_NoAccountSuccess.value = result.data
                                is Result.Error -> _nmbExistsInDB_NoAccountError.value = result.exception.message
                            }
                        }
                        true->{
                            when(result){
                                is Result.Success->_smsResendNetworkSuccess.value=result.data.code.toString()+result.data.userMessage
                                is Result.Error->_smsResendNetworkError.value=result.exception.message
                            }
                        }

                    }

                }
        }
    }

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


    //authorization fragment
    fun submitButtonClicked(smsToken:String){
        if(enteredPhoneNumber!=null) {

            viewModelScope.launch {
                val result = withContext(Dispatchers.IO) {
                    myRepository.authorizeThisUser(
                        enteredPhoneNumber ?: "",
                        smsToken,
                        enteredEmail ?: "",
                        enteredPassword ?: ""
                    )
                }

                when(result){
                    is Result.Success-> _authorizationNetworkSuccess.value=result.data
                    is Result.Error-> _authorizationNetworkError.value=result.exception.message
                }

            }
        }

    }


    //process Authorization data sent from server when user is finally created
    fun processAuthorizationData(authData:NetResponse_Authorization){

        val myphoneNumber = enteredPhoneNumber
        if( myphoneNumber == null || authData.authToken.isEmpty() || authData.authToken.isBlank()) return

        GlobalScope.launch {

            // if Sip Access is not set -invoke it again
            if (authData.sipReady == false) {
                launch(Dispatchers.IO) {
                    myRepository.resetSipAccess(
                        phone = myphoneNumber,
                        authToken = authData.authToken
                    )
                }
            }

            //if E1 is not sent, go fetch it
            if (authData.e1phone.isNullOrEmpty() || authData.e1phone.isBlank()) {
                launch(Dispatchers.IO) {
                    myRepository.callSetNewE1(
                        phone = myphoneNumber,
                        token = authData.authToken
                    )
                }
            }else {
                myRepository.updatePrenumber(
                    authData.e1phone,
                    System.currentTimeMillis()
                )
            }

            //insert webapi version into DB
            if (!authData.appVersion.isNullOrEmpty() && !authData.appVersion.isNullOrBlank()) {
                launch(Dispatchers.IO) {
                    myRepository.updateWebApiVersion(authData.appVersion)
                }
            }

        }

        //after processing E1 prenumber, sipAcces and WebApiVersion - insert email, pass and token or just token in DB
        viewModelScope.launch {

                withContext(Dispatchers.IO) {

                    if (authData.email.isNotEmpty() && authData.email.isNotBlank()) myRepository.updateUsersPhoneTokenEmail(
                        myphoneNumber,
                        authData.authToken,
                        authData.email
                    )
                    else myRepository.updateUsersPhoneAndToken(myphoneNumber, authData.authToken)

                }
        }

    }



    fun resetAuthorization_NetSuccess(){
        _authorizationNetworkSuccess.value=null
    }

    fun resetAuthorization_NetError(){
        _authorizationNetworkError.value=null
    }

    fun resetSMSResend_NetSuccess(){
        _smsResendNetworkSuccess.value=null
    }

    fun resetSMSResend_NetError(){
        _smsResendNetworkError.value=null
    }


    //SMS Retreiver
    fun setSMSVerificationTokenForAuthFragment(verToken:String){
        _verificationTokenForAuthFragment.value=verToken

    }

    fun resetSMSVerificationTOkenForAuthFragToNull(){
        _verificationTokenForAuthFragment.value=null
    }

    // this is called when SMS request is made to server
    fun startSMSRetreiverFunction(){
        _startSMSRetreiver.value=true
    }

    fun smsRetreiverStarted(){
        _startSMSRetreiver.value=false

    }

    override fun onCleared() {
        Log.i(MY_TAG,"ON CLEARED")
        super.onCleared()

    }
}