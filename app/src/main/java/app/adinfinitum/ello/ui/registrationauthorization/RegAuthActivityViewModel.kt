package app.adinfinitum.ello.ui.registrationauthorization

import android.app.Application
import android.text.TextUtils.split
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.api.NetResponse_AddNumberToAccount
import app.adinfinitum.ello.api.NetResponse_Authorization
import app.adinfinitum.ello.api.NetResponse_NmbExistsInDB
import app.adinfinitum.ello.api.NetResponse_Registration
import app.adinfinitum.ello.data.LogStateOrErrorToServer
import app.adinfinitum.ello.data.Repo
import app.adinfinitum.ello.data.Result
import app.adinfinitum.ello.ui.myapplication.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MY_TAG="MY_RegAuthActivVieModel"

class RegAuthActivityViewModel(val myRepository: Repo, application: Application) : AndroidViewModel(application) {

    var enteredPhoneNumber: String?=null
    var enteredEmail:String?=null
    var enteredPassword:String?=null
    var signInParameter:Boolean?=null

    //by default it is false, because SMS always works
    var isVerificationByCallEnabled:Boolean=false

    private var timeLatestSMSRetreiverStarted:Long=0L
    private val TIME_BETWEEN_TWO_SMS_RETREIVERS_IN_SEC=120

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
    private val _startSMSRetreiver= MutableLiveData<Event<Boolean>>()
    val startSMSRetreiver: LiveData<Event<Boolean>>
        get() = _startSMSRetreiver

    //SMS Retreiver for Authorization fragment to observe
    private val _verificationTokenForAuthFragment= MutableLiveData<Event<String>>()
    val verificationTokenForAuthFragment: LiveData<Event<String>>
        get() = _verificationTokenForAuthFragment


    init {
        Log.i(MY_TAG,("init"))
        getConfigurationInfo(myRepository)
    }

    private fun getConfigurationInfo(mRepository: Repo) {
        getApplication<MyApplication>().applicationScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val result = mRepository.getConfigurationInfo()
                    when (result) {
                        is Result.Success -> {
                            if(result.data.callVerificationEnabledCountryList.isNotEmpty() && result.data.callVerificationEnabledCountryList.isNotBlank())
                                mRepository.updateCallVerificationEnabledCountries(result.data.callVerificationEnabledCountryList)

                            if(result.data.e1EnabledCountryList.isNotEmpty() && result.data.e1EnabledCountryList.isNotBlank())
                                mRepository.updateE1EnabledCountries(result.data.e1EnabledCountryList)
                        }

                        is Result.Error -> {
                                mRepository.logStateOrErrorToOurServer(enteredPhoneNumber?:"",
                                    myoptions =
                                    mapOf(
                                        Pair("process", "getConfigurationInfo()"),
                                        Pair("error", result.exception.message ?: "")
                                    )
                                )
                            }
                        }
                    }

            } catch (e: Exception) {

            }

        }
    }

    fun resetSignUpParameters(){
        Log.i(MY_TAG,"reset user params BEFORE RESET: $enteredPhoneNumber, $enteredEmail, $enteredPassword, $signInParameter")
        enteredEmail=null
        enteredPassword=null
        signInParameter=null
        Log.i(MY_TAG,"reset user params AFTER RESET $enteredPhoneNumber,$enteredEmail, $enteredPassword, $signInParameter")
    }

    fun setIfVerificationByCallIsEnabled(normalizedEnteredPhoneNumber:String){
        viewModelScope.launch {
            try {
                val listOFCountries = withContext(IO) {
                    myRepository.getCountriesWhereVerificationByCallIsEnabled()
                }.let {
                    it.split(",",).map { it.trim() }
                }

                var result=false
                mloop@for(item in listOFCountries){
                    if(normalizedEnteredPhoneNumber.startsWith(item,ignoreCase = true)) {
                        result=true
                        break@mloop
                    }
                }
                isVerificationByCallEnabled=result

            }catch(e:Exception){


            }

         }

    }




    //registration fragment
    fun registerButtonClicked(phoneNumber:String, smsResend:Boolean=false, verificationMethod:String){
        enteredPhoneNumber=phoneNumber

        //send registrtion phone number to server
        viewModelScope.launch {
            try {

                val result = withContext(Dispatchers.IO) {
                    myRepository.sendRegistationToServer(
                        phone = phoneNumber,
                        verificationMethod = verificationMethod
                    )
                }

                when(smsResend){
                    false->{
                            when(result){

                                is Result.Success->_registrationNetworkResponseSuccess.value=Event(result.data)

                                is Result.Error->{
                                                _registrationNetworkResponseError.value=Event(result.exception.message?:"")
                                                withContext(Dispatchers.IO) {

                                                       myRepository.logStateOrErrorToOurServer(enteredPhoneNumber?:"",
                                                       myoptions =
                                                           mapOf(
                                                               Pair("process", "registration"),
                                                               Pair("smsResend", smsResend.toString()),
                                                               Pair("error", result.exception.message ?: "")
                                                           )
                                                       )

                                                }
                                }
                            }
                    }
                    true->{
                        when(result){
                            is Result.Success->_smsResendNetworkSuccess.value=Event(result.data.code.toString()+result.data.userMessage)
                            is Result.Error->{
                                                _smsResendNetworkError.value=Event(result.exception.message?:"")
                                                withContext(Dispatchers.IO){
                                                    myRepository.logStateOrErrorToOurServer(enteredPhoneNumber?:"",
                                                        mapOf(
                                                            Pair("process","registration"),
                                                            Pair("smsResend",smsResend.toString()),
                                                            Pair("error",result.exception.message?:"")
                                                        )
                                                    )

                                                }
                            }
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
                        verificationMethod = verificationMethod
                    )
                }

                when (smsResend) {
                    false -> {
                        when (result) {
                            is Result.Success -> _addNumberToAccuntNetworkSuccess.value = Event(result.data)
                            is Result.Error ->{
                                        _addNumberToAccuntNetworkError.value = Event(result.exception.message)
                                        withContext(Dispatchers.IO){
                                            myRepository.logStateOrErrorToOurServer(enteredPhoneNumber?:"",
                                                mapOf(
                                                    Pair("process","assignPhoneNumberToAccount"),
                                                    Pair("smsResend",smsResend.toString()),
                                                    Pair("error",result.exception.message?:"")
                                                )
                                            )

                                        }
                            }
                        }
                    }
                    true -> {
                        when (result) {
                            is Result.Success -> _smsResendNetworkSuccess.value = Event(result.data.code.toString() + result.data.usermessage)
                            is Result.Error -> {
                                                _smsResendNetworkError.value = Event(result.exception.message)
                                                withContext(Dispatchers.IO){
                                                    myRepository.logStateOrErrorToOurServer(enteredPhoneNumber?:"",
                                                        mapOf(
                                                            Pair("process","assignPhoneNumberToAccount"),
                                                            Pair("smsResend",smsResend.toString()),
                                                            Pair("error",result.exception.message?:"")
                                                        )
                                                    )

                                                }
                            }
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
                          verificationMethod = verificationMethod
                      )
                  }

                  when (smsResend) {
                      false -> {
                          when (result) {
                              is Result.Success -> _nmbExistsInDBUserHasAccountSuccess.value = Event(result.data)
                              is Result.Error -> {
                                            _nmbExistsInDBUserHasAccountError.value = Event(result.exception.message)
                                            withContext(Dispatchers.IO){
                                                  myRepository.logStateOrErrorToOurServer(enteredPhoneNumber?:"",
                                                      mapOf(
                                                          Pair("process","numberExistsInDBVerifyAccount"),
                                                          Pair("smsResend",smsResend.toString()),
                                                          Pair("error",result.exception.message?:"")
                                                      )
                                                  )

                                            }
                              }
                          }
                      }
                      true -> {
                          when (result) {
                              is Result.Success -> _smsResendNetworkSuccess.value =
                                  Event(result.data.code.toString() + result.data.userMessage)
                              is Result.Error -> {
                                                _smsResendNetworkError.value = Event(result.exception.message)
                                                withContext(Dispatchers.IO){
                                                      myRepository.logStateOrErrorToOurServer(enteredPhoneNumber?:"",
                                                          mapOf(
                                                              Pair("process","numberExistsInDBVerifyAccount"),
                                                              Pair("smsResend",smsResend.toString()),
                                                              Pair("error",result.exception.message?:"")
                                                          )
                                                      )

                                                }
                              }
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
                            verificationMethod = verificationMethod
                        )
                    }

                    when (smsResend) {
                        false -> {
                            when (result) {
                                is Result.Success -> _nmbExistsInDB_NoAccountSuccess.value = Event(result.data)
                                is Result.Error -> {
                                            _nmbExistsInDB_NoAccountError.value = Event(result.exception.message)
                                            withContext(Dispatchers.IO){
                                                myRepository.logStateOrErrorToOurServer(enteredPhoneNumber?:"",
                                                    mapOf(
                                                        Pair("process","numberExistsInDB_NOAccount"),
                                                        Pair("smsResend",smsResend.toString()),
                                                        Pair("error",result.exception.message?:"")
                                                    )
                                                )

                                            }
                                }
                            }
                        }
                        true -> {
                            when (result) {
                                is Result.Success -> _smsResendNetworkSuccess.value =
                                    Event(result.data.code.toString() + result.data.userMessage)
                                is Result.Error -> {
                                        _smsResendNetworkError.value = Event(result.exception.message)
                                        withContext(Dispatchers.IO){
                                            myRepository.logStateOrErrorToOurServer(enteredPhoneNumber?:"",
                                                mapOf(
                                                    Pair("process","numberExistsInDB_NOAccount"),
                                                    Pair("smsResend",smsResend.toString()),
                                                    Pair("error",result.exception.message?:"")
                                                )
                                            )

                                        }
                                }
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
                    is Result.Error->{
                                 _authorizationNetworkError.value=Event(result.exception.message)
                                withContext(Dispatchers.IO){
                                    myRepository.logStateOrErrorToOurServer(enteredPhoneNumber?:"",
                                        mapOf(
                                            Pair("process","authorizeThisUser"),
                                            Pair("error",result.exception.message?:"")
                                        )
                                    )

                                }
                    }
                }

            }
        }

    }


    //process Authorization data sent from server when user is finally created
    fun processAuthorizationData(authData:NetResponse_Authorization, mRepository: Repo=myRepository){

        val myphoneNumber = enteredPhoneNumber
        if( myphoneNumber == null || authData.authToken.isEmpty() || authData.authToken.isBlank()) return

        getApplication<MyApplication>().applicationScope.launch {

            try {
                withContext(Dispatchers.IO) {


                    // if Sip Access is not set -invoke it again
                    if (authData.sipReady == false) {
                        mRepository.resetSipAccess(
                            phone = myphoneNumber,
                            authToken = authData.authToken
                        )
                    }


                    //if E1 is not sent, go fetch it
                    if (authData.e1phone.isNullOrEmpty() || authData.e1phone.isBlank()) {
                        try {
                            val result =
                                mRepository.callSetNewE1(
                                    phone = myphoneNumber,
                                    token = authData.authToken
                                )

                            when (result) {
                                is Result.Success -> {
                                    Log.i(MY_TAG, "inside application scope processing auth data fetch e1 $result")
                                    if (!result.data.e1prenumber.isNullOrEmpty() && !result.data.e1prenumber.isNullOrBlank()) {
                                        mRepository.updatePrenumber(
                                            result.data.e1prenumber,
                                            System.currentTimeMillis()
                                        )
                                    }
                                }
                                is Result.Error -> {

                                }

                            }
                        } catch (e: Exception) {
                            Log.i(MY_TAG, "e1 ${e.message}")
                        }
                    } else {
                        mRepository.updatePrenumber(
                            authData.e1phone,
                            System.currentTimeMillis()
                        )
                    }

                    //insert webapi version into DB
                    if (!authData.appVersion.isNullOrEmpty() && !authData.appVersion.isNullOrBlank()) {
                        mRepository.updateWebApiVersion(authData.appVersion)
                        Log.i(MY_TAG, "myRepository.updateWebApiVersion")
                    }
                }
            }catch (e:Exception){
                Log.i(MY_TAG, "application scope launch ${e.message}")
            }
        }


        //after processing E1 prenumber, sipAcces and WebApiVersion - insert email, pass and token or just token in DB
        viewModelScope.launch {

                withContext(Dispatchers.IO) {
                    Log.i(MY_TAG, "viewModelScope.launch updateUsersPhoneAndToken ")
                        if (authData.email.isNotEmpty() && authData.email.isNotBlank()) mRepository.updateUsersPhoneTokenEmail(
                            myphoneNumber,
                            authData.authToken,
                            authData.email
                        )
                        else mRepository.updateUsersPhoneAndToken(myphoneNumber, authData.authToken)

                }
        }

    }


    //SMS Retreiver
    fun setSMSVerificationTokenForAuthFragment(verToken:String){
        _verificationTokenForAuthFragment.value=Event(verToken)
    }


    // this is called when SMS request is made to server
    fun startSMSRetreiverFunction(timeThisSMSRetreiverStarted:Long){
        Log.i(MY_TAG,"startSMSRetreiverFunction this $timeThisSMSRetreiverStarted, latest $timeLatestSMSRetreiverStarted,${TIME_BETWEEN_TWO_SMS_RETREIVERS_IN_SEC*1000}")

            if((timeThisSMSRetreiverStarted-timeLatestSMSRetreiverStarted)>TIME_BETWEEN_TWO_SMS_RETREIVERS_IN_SEC*1000){
                    Log.i(MY_TAG,"startSMSRetreiverFunction  ${timeLatestSMSRetreiverStarted-timeThisSMSRetreiverStarted}")
                        _startSMSRetreiver.value=Event(true)
                        timeLatestSMSRetreiverStarted=timeThisSMSRetreiverStarted
        }
    }


    override fun onCleared() {
        Log.i(MY_TAG,"ON CLEARED")
        super.onCleared()

    }
}