package app.adinfinitum.ello.ui.registrationauthorization

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils.split
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.data.IRepo
import app.adinfinitum.ello.data.LogStateOrErrorToServer
import app.adinfinitum.ello.data.Repo
import app.adinfinitum.ello.data.Result
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.ui.registrationauthorization.models.*
import app.adinfinitum.ello.utils.VERIFICATION_METHOD_CALL
import app.adinfinitum.ello.utils.VERIFICATION_METHOD_SMS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MY_TAG="MY_RegAuthActivVieModel"


class RegAuthActivityViewModel(val myRepository: IRepo, application: Application) : AndroidViewModel(application) {

    val signInForm = SignInForm()
    val signInProcessAuxData = SignInProcessAuxData()
    val NAVIGATE_TO_AUTHORIZATION_FRAGMENT = 1
    val NAVIGATE_TO_NMB_EXISTS_IN_DB_FRAGMENT = 2

    private var timeLatestSMSRetreiverStarted: Long = 0L
    private val TIME_BETWEEN_TWO_SMS_RETREIVERS_IN_SEC = 120

    val userData = myRepository.getUserData()


    //Registration fragment reorganized
    private val _registrationResult = MutableLiveData<Event<RegistrationResult>>()
    val registrationResult: LiveData<Event<RegistrationResult>>
        get() = _registrationResult

    //Add Number to Account fragment reorganized
    private val _addNumberToAccountResult = MutableLiveData<Event<AddNumberToAccountResult>>()
    val addNumberToAccountResult: LiveData<Event<AddNumberToAccountResult>>
        get() = _addNumberToAccountResult

    //PhoneNumber exists in DB reorganized
    private val _nmbExistsInDBResult = MutableLiveData<Event<NumberExistsInDBResult>>()
    val nmbExistsInDBResult: LiveData<Event<NumberExistsInDBResult>>
        get() = _nmbExistsInDBResult

    //Authorization fragment reorganized
    private val _authorizationResult = MutableLiveData<Event<AuthorizationResult>>()
    val authorizationResult: LiveData<Event<AuthorizationResult>>
        get() = _authorizationResult


    //SMS Retreiver for Activity to observe
    private val _startSMSRetreiver = MutableLiveData<Event<Boolean>>()
    val startSMSRetreiver: LiveData<Event<Boolean>>
        get() = _startSMSRetreiver

    //SMS Retreiver for Authorization fragment to observe
    private val _verificationTokenForAuthFragment = MutableLiveData<Event<String>>()
    val verificationTokenForAuthFragment: LiveData<Event<String>>
        get() = _verificationTokenForAuthFragment


    init {
        Log.i(MY_TAG, ("init"))

    }


    fun reinitializeSignInForm(inForm:SignInForm){
        signInForm.apply {
            phoneNmb=inForm.phoneNmb
            email=inForm.email
            password=inForm.password
         }
    }

    fun resetSignInFormEmailAndPassword(){
        signInForm.apply {
            email=null
            password=null
        }

    }

    fun reinitializeSignInProcessAuxData(inForm:SignInProcessAuxData){
        signInProcessAuxData.apply {
            signInValue_whenPhoneNumberAlreadyExists=inForm.signInValue_whenPhoneNumberAlreadyExists
            verificationByCallEnabled=inForm.verificationByCallEnabled
            callerId=inForm.callerId

         }
    }

    fun resetSignInProcessAuxData(){
        signInProcessAuxData.apply {
            signInValue_whenPhoneNumberAlreadyExists=false
            verificationByCallEnabled=false
            callerId=null

        }
    }

    fun afterPhoneNumberEditTextChanged(currentPhoneNumber: String) {
        signInForm.phoneNmb = currentPhoneNumber
    }

    fun afterEmailEditTextChanged(currentEmail:String){
        signInForm.email=currentEmail
    }

    fun afterPasswordEditTextChanged(currentPassword:String){
        signInForm.password=currentPassword
    }

    fun getFormAuxDataCallerID(): String? {
        return signInProcessAuxData.callerId
    }


    //registration fragment reorganized
    fun registerButtonClicked() {

        when (signInForm.isPhoneNumberValid) {

            false -> _registrationResult.value =
                Event(RegistrationResult(enteredPhoneError = R.string.not_valid_phone_number))

            true -> _registrationResult.value =
                Event(RegistrationResult(showTermsOfUseDialog = true))
        }
    }

    fun sendRegistrationToServer(){
                //todo startuj SMS retreiver
                viewModelScope.launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            myRepository.signUpToServer(
                                NetRequest_SignUp(
                                    phoneNumber = signInForm.normalizedPhoneNmb ?: throw Exception("normalized phone number is null"),
                                    verificationMethod = signInProcessAuxData.verificationMethod
                                )
                            )
                        }

                        when (result) {
                            is Result.Success -> {
                                val response = result.data
                                //set variable to define if registration process should use call or sms verification
                                if(response.callVerificationEnabled){
                                    signInProcessAuxData.verificationByCallEnabled=response.callVerificationEnabled
                                    signInProcessAuxData.callerId=response.verificationCallerId
                                }

                                //for testing
                                //todo remove this later
                                signInProcessAuxData.verificationByCallEnabled=false //for testing
                                signInProcessAuxData.callerId=null //for testing

                                if ((signInProcessAuxData.verificationByCallEnabled && checkForPermission()) || !signInProcessAuxData.verificationByCallEnabled) {
                                    when {
                                        response.success == true && response.phoneNumberAlreadyAssigned == false -> {
                                            _registrationResult.value = Event(
                                                RegistrationResult(
                                                    navigateToFragment = NAVIGATE_TO_AUTHORIZATION_FRAGMENT,
                                                    showToastMessage = response.userMessage,
                                                    userMessageServerCode = response.code
                                                )
                                            )

                                        }

                                        response.success == true && response.phoneNumberAlreadyAssigned == true -> {
                                            _registrationResult.value = Event(
                                                RegistrationResult(
                                                    navigateToFragment = NAVIGATE_TO_NMB_EXISTS_IN_DB_FRAGMENT,
                                                    showToastMessage = response.userMessage,
                                                    userMessageServerCode = response.code
                                                )
                                            )

                                        }

                                        response.success == false -> {
                                            _registrationResult.value = Event(
                                                RegistrationResult(
                                                    navigateToFragment = NAVIGATE_TO_NMB_EXISTS_IN_DB_FRAGMENT,
                                                    showSnackBarMessage = response.userMessage,
                                                    userMessageServerCode = response.code
                                                )
                                            )
                                        }

                                    }

                                } else {
                                    _registrationResult.value = Event(
                                        RegistrationResult(
                                           mustAskForPermission = true
                                        )
                                    )
                                }

                            }

                            is Result.Error -> {
                                _registrationResult.value = Event(
                                    RegistrationResult(
                                        //showSnackBarMessage = result.exception.message
                                        showSnackBarErrorMessage = true
                                    )
                                )

                                withContext(Dispatchers.IO) {

                                    myRepository.logStateOrErrorToOurServer(
                                        signInForm.normalizedPhoneNmb
                                            ?: "normalized phone number is null",
                                        myoptions =
                                        mapOf(
                                            Pair("process", "registration"),
                                            Pair("error", result.exception.message ?: "")
                                        )
                                    )

                                }
                            }
                        }

                    } catch (e: Exception) {
                        Log.i(MY_TAG, "registerButtonClickedError, ${e.message}")
                    }
                }
    }

    //Add number to existing account fragment reorganized
    fun addNumberToAccountButtonClicked() {
        Log.i(MY_TAG, ("addNumberToAccountButtonClicked(), ${signInForm.areSignInDataValid}"))
        when (signInForm.areSignInDataValid) {

            false -> {
                val addNumberToAccountResult = AddNumberToAccountResult().apply {
                    if (!signInForm.isPhoneNumberValid) enteredPhoneError =
                        R.string.not_valid_phone_number
                    if (!signInForm.isEmailValid) enteredEmailError = R.string.not_valid_email
                    if (!signInForm.isPasswordValid) enteredPasswordError =
                        R.string.not_valid_password

                }
                _addNumberToAccountResult.value = Event(addNumberToAccountResult)
            }

            //send  phone, email and pass to server
            true -> {
                _addNumberToAccountResult.value =
                    Event(AddNumberToAccountResult(showTermsOfUseDialog = true))
            }
        }
    }

    fun sendAddNumberToAccountToServer(){
                viewModelScope.launch {
                    try {

                        val result = withContext(Dispatchers.IO) {
                            myRepository.signUpToServer(
                                NetRequest_SignUp(
                                phoneNumber = signInForm.normalizedPhoneNmb?:throw Exception("normalized phone number in sign in form is null"),
                                email = signInForm.email?:throw Exception("email in sign in form is null"),
                                password = signInForm.password?:throw Exception("password in sign in form is null"),
                                verificationMethod = signInProcessAuxData.verificationMethod
                                )
                            )
                        }


                        when (result) {
                            is Result.Success -> {
                                val response = result.data

                                //set variable to define if registration process should use call or sms verification
                                if(response.callVerificationEnabled){
                                    signInProcessAuxData.verificationByCallEnabled=response.callVerificationEnabled
                                    signInProcessAuxData.callerId=response.verificationCallerId
                                }

                                if(signInProcessAuxData.verificationMethod==VERIFICATION_METHOD_CALL) signInProcessAuxData.callerId=response.verificationCallerId

                                if ((signInProcessAuxData.verificationByCallEnabled && checkForPermission()) || !signInProcessAuxData.verificationByCallEnabled) {

                                    val addNumberToAccountResult=AddNumberToAccountResult()
                                    when(response.success) {
                                        true -> {
                                            addNumberToAccountResult.apply {
                                                navigateToFragment=NAVIGATE_TO_AUTHORIZATION_FRAGMENT
                                                showToastMessage=response.userMessage
                                            }

                                        }
                                        false -> {
                                            addNumberToAccountResult.apply {
                                                showSnackBarMessage=response.userMessage
                                            }

                                        }
                                    }

                                    _addNumberToAccountResult.value = Event(addNumberToAccountResult)

                                }else  {
                                    _addNumberToAccountResult.value=Event(AddNumberToAccountResult(mustAskForPermission = true))
                                }


                            }

                            is Result.Error -> {
                                        _addNumberToAccountResult.value = Event(
                                            AddNumberToAccountResult(showSnackBarErrorMessage = true ))

                                        withContext(Dispatchers.IO) {
                                            myRepository.logStateOrErrorToOurServer(
                                                signInForm.normalizedPhoneNmb ?: "normalized phone number is null",
                                                mapOf(
                                                    Pair("process", "assignPhoneNumberToAccount"),
                                                    Pair("error", result.exception.message ?: "")
                                                )
                                            )

                                        }
                            }
                        }

                    } catch (e: Exception) {
                        Log.i(MY_TAG, "Add Number to account ClickedError, ${e.message}")
                    }
                }

    }


    //number Exists In DB Fragment
    fun numberExistsInDBVerifyAccountButtonClicked(memail: String?,mpassword: String?) {
        //in case when after clicking on New Account button user navigates to Authorization and then clicks back button
        //email and password fields in SignInForm are null
        //so check is necessary in case user wrote sometning in email and pass fields before clicking New Account button

        if(signInForm.email==null || signInForm.password==null){
           signInForm.email=memail
           signInForm.password=mpassword
        }

        when (signInForm.areSignInDataValid) {

            false -> {
                val numberExistsInDBResult = NumberExistsInDBResult().apply {
                    if (!signInForm.isEmailValid) enteredEmailError = R.string.not_valid_email
                    if (!signInForm.isPasswordValid) enteredPasswordError = R.string.not_valid_password
                }
                _nmbExistsInDBResult.value = Event(numberExistsInDBResult)
            }
            true->{
                signInProcessAuxData.signInValue_whenPhoneNumberAlreadyExists=true
                numberExistsInDBSignIN()
            }
        }
    }

    fun numberExistsInDBNOAccountButtonClicked(){
        resetSignInFormEmailAndPassword()
        signInProcessAuxData.signInValue_whenPhoneNumberAlreadyExists=false
        numberExistsInDBSignIN()
    }

    //number exists in DB fragment Sign In
    private fun numberExistsInDBSignIN() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    myRepository.signUpToServer(
                        NetRequest_SignUp(
                            phoneNumber = signInForm.normalizedPhoneNmb ?: throw Exception("normalized phone number is null"),
                            email = signInForm.email ?:"",
                            password = signInForm.password?:"",
                            signin = signInProcessAuxData.signInValue_whenPhoneNumberAlreadyExists.toString()
                        )
                    )
                }

                val numberExistsInDBResult=NumberExistsInDBResult()
                when (result) {
                    is Result.Success -> {
                        val response = result.data

                        when {
                            response.success == true -> {
                                numberExistsInDBResult.apply {
                                    navigateToFragment=NAVIGATE_TO_AUTHORIZATION_FRAGMENT
                                    showToastMessage=response.userMessage
                                 }
                            }
                            response.success == false -> {
                                numberExistsInDBResult.showSnackBarMessage=response.userMessage
                            }
                        }

                        _nmbExistsInDBResult.value=Event(numberExistsInDBResult)

                    }
                    is Result.Error -> {
                        numberExistsInDBResult.showSnackBarErrorMessage=true
                        _nmbExistsInDBResult.value=Event(numberExistsInDBResult)

                        withContext(Dispatchers.IO) {
                            myRepository.logStateOrErrorToOurServer(
                                signInForm.normalizedPhoneNmb ?: "",
                                mapOf(
                                    Pair("process", "numberExistsInDBSignIN"),
                                    Pair("error", result.exception.message ?: "")
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.i(MY_TAG, "numberExistsInDBSignIN, ${e.message}")
            }
        }
    }


    //Authorization fragment
    fun submitButtonClicked(smsToken: String) {

            viewModelScope.launch {
                val result = withContext(Dispatchers.IO) {
                    myRepository.authorizeThisUser(
                        signInForm.normalizedPhoneNmb ?: "",
                        smsToken,
                        signInForm.email ?: "",
                        signInForm.password ?: ""
                    )
                }


                when (result) {
                    is Result.Success ->{
                        when(result.data.success){
                            true-> {
                                processAuthorizationData(phoneNumber = signInForm.normalizedPhoneNmb?:throw Exception("normalized phone number is null"),
                                                        authData=result.data,
                                                        mRepository=myRepository)
                                _authorizationResult.value=Event(AuthorizationResult(showToastMessage = result.data.userMessage))
                            }
                            false->{
                                _authorizationResult.value=Event(AuthorizationResult(showSnackBarMessage = result.data.userMessage))
                            }
                        }
                    }
                    is Result.Error -> {
                        _authorizationResult.value=Event(AuthorizationResult(showSnackBarErrorMessage = true))

                        withContext(Dispatchers.IO) {
                            myRepository.logStateOrErrorToOurServer(
                                signInForm.normalizedPhoneNmb ?: "",
                                mapOf(
                                    Pair("process", "authorizeThisUser"),
                                    Pair("error", result.exception.message ?: "")
                                )
                            )

                        }
                    }
                }

            }
        }


    fun resendSMSButtonClicked(verificationType:String){
        //repeat last signIn call, verification type in not refreshed in SigInProcessAuxData
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    myRepository.signUpToServer(
                        NetRequest_SignUp(
                            phoneNumber = signInForm.normalizedPhoneNmb ?: throw Exception("normalized phone number is null"),
                            email = signInForm.email ?: "",
                            password = signInForm.password ?: "",
                            signin = signInProcessAuxData.signInValue_whenPhoneNumberAlreadyExists.toString(),
                            verificationMethod = verificationType
                        )
                    )
                }

                when (result) {
                    is Result.Success -> {
                        when (result.data.success) {
                            true -> {
                                _authorizationResult.value =
                                    Event(AuthorizationResult(showToastMessage = result.data.userMessage))
                            }
                            false -> {
                                _authorizationResult.value =
                                    Event(AuthorizationResult(showSnackBarMessage = result.data.userMessage))
                            }
                        }
                    }
                    is Result.Error -> {
                        _authorizationResult.value=Event(AuthorizationResult(showSnackBarErrorMessage = true))
                    }
                }

            } catch (e: Exception) {

            }
        }
    }

    //process Authorization data sent from server when user is finally created
    private suspend fun processAuthorizationData(
        phoneNumber:String,
        authData: NetResponse_Authorization,
        mRepository: IRepo
    ) {

        getApplication<MyApplication>().applicationScope.launch {

            try {
                withContext(Dispatchers.IO) {
                    // if Sip Access is not set -invoke it again
                    if (authData.sipReady == false) {
                        mRepository.resetSipAccess(
                            phone = phoneNumber,
                            authToken = authData.authToken
                        )
                    }

                    //if E1 is not sent, go fetch it
                    if (authData.e1phone.isNullOrEmpty() || authData.e1phone.isBlank()) {
                        try {
                            val result =
                                mRepository.callSetNewE1(
                                    phone = phoneNumber,
                                    token = authData.authToken
                                )

                            when (result) {
                                is Result.Success -> {
                                    Log.i(
                                        MY_TAG,
                                        "inside application scope processing auth data fetch e1 $result"
                                    )
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
            } catch (e: Exception) {
                Log.i(MY_TAG, "application scope launch ${e.message}")
            }
        }

        //after processing E1 prenumber, sipAcces and WebApiVersion - insert email, pass and token or just token in DB
            withContext(Dispatchers.IO) {
                Log.i(MY_TAG, "viewModelScope.launch updateUsersPhoneAndToken ")
                if (authData.email.isNotEmpty() && authData.email.isNotBlank()) mRepository.updateUsersPhoneTokenEmail(
                    phoneNumber,
                    authData.authToken,
                    authData.email
                )
                else mRepository.updateUsersPhoneAndToken(phoneNumber, authData.authToken)

            }


    }


    //SMS Retreiver
    fun setSMSVerificationTokenForAuthFragment(verToken: String) {
        _verificationTokenForAuthFragment.value = Event(verToken)
    }


    // this is called when SMS request is made to server
    fun startSMSRetreiverFunction(timeThisSMSRetreiverStarted: Long) {
        Log.i(
            MY_TAG,
            "startSMSRetreiverFunction this $timeThisSMSRetreiverStarted, latest $timeLatestSMSRetreiverStarted,${TIME_BETWEEN_TWO_SMS_RETREIVERS_IN_SEC * 1000}"
        )

        if ((timeThisSMSRetreiverStarted - timeLatestSMSRetreiverStarted) > TIME_BETWEEN_TWO_SMS_RETREIVERS_IN_SEC * 1000) {
            Log.i(
                MY_TAG,
                "startSMSRetreiverFunction  ${timeLatestSMSRetreiverStarted - timeThisSMSRetreiverStarted}"
            )
            _startSMSRetreiver.value = Event(true)
            timeLatestSMSRetreiverStarted = timeThisSMSRetreiverStarted
        }
    }


    override fun onCleared() {
        Log.i(MY_TAG, "ON CLEARED")
        super.onCleared()

    }

    fun isOnline(): Boolean {

        return app.adinfinitum.ello.utils.isOnline(getApplication())
    }

    fun resetSignInForm(phone:String,email:String,password:String,signin:Boolean){
        signInForm.apply {

         }
    }

    fun getEnteredPhoneNumber():String{
        return signInForm.phoneNmb?:""
    }

    fun isVerificationByCallEnabled():Boolean{
        return signInProcessAuxData.verificationMethod== VERIFICATION_METHOD_CALL
    }


    private fun checkForPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        else {
            if (getApplication<MyApplication>().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                getApplication<MyApplication>().checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED

            ) {
                return false

            } else return true
        }

    }
}
