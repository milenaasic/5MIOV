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
import kotlinx.coroutines.Dispatchers.IO
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
    private val _registrationNetworkResponseSuccess= MutableLiveData<Event<NetResponse_Registration>>()
    val registrationNetworkResponseSuccess: LiveData<Event<NetResponse_Registration>>
        get() = _registrationNetworkResponseSuccess

    private val _registrationNetworkResponseError= MutableLiveData<Event<String?>>()
    val registrationNetworkResponseError: LiveData<Event<String?>>
        get() = _registrationNetworkResponseError


    //Add Number to Account fragment
    private val _addNumberToAccuntNetworkSuccess= MutableLiveData<Event<NetResponse_AddNumberToAccount>>()
    val addNumberToAccuntNetworkSuccess: LiveData<Event<NetResponse_AddNumberToAccount>>
        get() = _addNumberToAccuntNetworkSuccess

    private val _addNumberToAccuntNetworkError= MutableLiveData<Event<String?>>()
    val addNumberToAccuntNetworkError: LiveData<Event<String?>>
        get() = _addNumberToAccuntNetworkError


    //PhoneNumber exists in DB
    private val _nmbExistsInDBUserHasAccountSuccess= MutableLiveData<Event<NetResponse_NmbExistsInDB>>()
    val nmbExistsInDBUserHasAccountSuccess: LiveData<Event<NetResponse_NmbExistsInDB>>
        get() = _nmbExistsInDBUserHasAccountSuccess

    private val _nmbExistsInDBUserHasAccountError= MutableLiveData<Event<String?>>()
    val nmbExistsInDBUserHasAccountError: LiveData<Event<String?>>
        get() = _nmbExistsInDBUserHasAccountError

    private val _nmbExistsInDB_NoAccountSuccess= MutableLiveData<Event<NetResponse_NmbExistsInDB>>()
    val nmbExistsInDB_NoAccountSuccess: LiveData<Event<NetResponse_NmbExistsInDB>>
        get() = _nmbExistsInDB_NoAccountSuccess

    private val _nmbExistsInDB_NoAccountError= MutableLiveData<Event<String?>>()
    val nmbExistsInDB_NoAccountError: LiveData<Event<String?>>
        get() = _nmbExistsInDB_NoAccountError



    //Authorization fragment
    private val _authorizationNetworkSuccess= MutableLiveData<Event<NetResponse_Authorization>>()
    val authorizationNetworkSuccess: LiveData<Event<NetResponse_Authorization>>
        get() = _authorizationNetworkSuccess

    private val _authorizationNetworkError= MutableLiveData<Event<String?>>()
    val authorizationNetworkError: LiveData<Event<String?>>
        get() = _authorizationNetworkError

    private val _smsResendNetworkSuccess= MutableLiveData<Event<String?>>()
    val smsResendNetworkSuccess: LiveData<Event<String?>>
        get() = _smsResendNetworkSuccess

    private val _smsResendNetworkError= MutableLiveData<Event<String?>>()
    val smsResendNetworkError: LiveData<Event<String?>>
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

                when(smsResend){
                    false->{
                            when(result){
                                is Result.Success->_registrationNetworkResponseSuccess.value=Event(result.data)
                                is Result.Error->_registrationNetworkResponseError.value=Event(result.exception.message)
                            }
                    }
                    true->{
                        when(result){
                            is Result.Success->_smsResendNetworkSuccess.value=Event(result.data.code.toString()+result.data.userMessage)
                            is Result.Error->_smsResendNetworkError.value=Event(result.exception.message?:"")
                        }
                    }

                }

            }catch ( e:Exception){
                Log.i(MY_TAG,"registerButtonClickedError, ${e.message}")
            }

         }
    }


    //add number to existing account fragment
    fun addNumberToAccountButtonClicked(phoneNumber:String,email:String,password:String,smsResend: Boolean=false,verificationMethod: String){
        enteredPhoneNumber=phoneNumber
        enteredEmail=email
        enteredPassword=password

        //send  phone, email and pass to server
        viewModelScope.launch {
            try {

                val result = withContext(Dispatchers.IO) {
                    myRepository.assignPhoneNumberToAccount(
                        phone = phoneNumber,
                        email = email,
                        password = password,
                        smsResend = smsResend,
                        verificationMethod = verificationMethod
                    )
                }

                when (smsResend) {
                    false -> {
                        when (result) {
                            is Result.Success -> _addNumberToAccuntNetworkSuccess.value = Event(result.data)
                            is Result.Error -> _addNumberToAccuntNetworkError.value = Event(result.exception.message)
                        }
                    }
                    true -> {
                        when (result) {
                            is Result.Success -> _smsResendNetworkSuccess.value = Event(result.data.code.toString() + result.data.usermessage)
                            is Result.Error -> _smsResendNetworkError.value = Event(result.exception.message)
                        }
                    }

                }
            }catch (e:Exception){

            }

        }
    }


    //number exists in DB fragment
    fun numberExistsInDBVerifyAccount(email: String,password: String,smsResend: Boolean=false,verificationMethod: String){
        enteredEmail=email
        enteredPassword=password
        if(enteredPhoneNumber!=null) {
            viewModelScope.launch {
              try {
                  val result = withContext(Dispatchers.IO) {
                      myRepository.numberExistsInDBVerifyAccount(
                          enteredPhoneNumber ?: "",
                          email,
                          password,
                          smsResend,
                          verificationMethod = verificationMethod
                      )
                  }

                  when (smsResend) {
                      false -> {
                          when (result) {
                              is Result.Success -> _nmbExistsInDBUserHasAccountSuccess.value = Event(result.data)
                              is Result.Error -> _nmbExistsInDBUserHasAccountError.value = Event(result.exception.message)
                          }
                      }
                      true -> {
                          when (result) {
                              is Result.Success -> _smsResendNetworkSuccess.value =
                                  Event(result.data.code.toString() + result.data.userMessage)
                              is Result.Error -> _smsResendNetworkError.value =
                                  Event(result.exception.message)
                          }
                      }

                  }
              }catch (e:Exception){

              }

            }
        }
    }

    fun numberExistsInDb_NoAccount(smsResend: Boolean=false,verificationMethod: String){
         if(enteredPhoneNumber!=null) {

             viewModelScope.launch {
                try {
                    val result = withContext(IO) {
                        myRepository.numberExistsInDB_NOAccount(
                            enteredPhoneNumber ?: "",
                            smsResend,
                            verificationMethod = verificationMethod
                        )
                    }

                    when (smsResend) {
                        false -> {
                            when (result) {
                                is Result.Success -> _nmbExistsInDB_NoAccountSuccess.value = Event(result.data)
                                is Result.Error -> _nmbExistsInDB_NoAccountError.value = Event(result.exception.message)
                            }
                        }
                        true -> {
                            when (result) {
                                is Result.Success -> _smsResendNetworkSuccess.value =
                                    Event(result.data.code.toString() + result.data.userMessage)
                                is Result.Error -> _smsResendNetworkError.value =
                                    Event(result.exception.message)
                            }
                        }

                    }

                }catch (e:Exception){

                }

             }
        }
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
                    is Result.Success-> _authorizationNetworkSuccess.value=Event(result.data)
                    is Result.Error-> _authorizationNetworkError.value=Event(result.exception.message)
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
                try {
                    val result = withContext(Dispatchers.IO) {
                        myRepository.callSetNewE1(
                            phone = myphoneNumber,
                            token = authData.authToken
                        )
                    }

                    when (result) {
                        is Result.Success -> {
                            if (!result.data.e1prenumber.isNullOrEmpty() && !result.data.e1prenumber.isNullOrBlank()) {
                                myRepository.updatePrenumber(result.data.e1prenumber, System.currentTimeMillis())
                            }
                        }
                        is Result.Error -> {

                        }


                    }
                }catch (e:Exception){

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