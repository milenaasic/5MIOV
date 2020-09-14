package com.vertial.fivemiov.ui.registrationauthorization

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoSIPE1
import kotlinx.coroutines.launch

private const val MY_TAG="MY_RegAuthActivVieModel"

class RegAuthActivityViewModel(val myRepository: Repo, val mySIPE1Repo:RepoSIPE1,application: Application) : AndroidViewModel(application) {

    var enteredPhoneNumber: String?=null
    var enteredEmail:String?=null
    var enteredPassword:String?=null
    var signInParameter:Boolean?=null


    val userData=myRepository.getUserData()


    val registrationNetworkError=myRepository.registrationNetworkError
    val registrationNetSuccess=myRepository.registrationSuccess

    val addNumberToAccuntNetworkError=myRepository.addNumberToAccountNetworkError
    val addNumberToAccuntNetworkSuccess=myRepository.addNumberToAccountNetworkSuccess


    val nmbExistsInDBUserHasAccountSuccess=myRepository.nmbExistsInDBUserHasAccountSuccess
    val nmbExistsInDBUserHasAccountError=myRepository.nmbExistsInDBUserHasAccountError
    val nmbExistsInDB_NoAccountSuccess=myRepository.nmbExistsInDB_NoAccountSuccess
    val nmbExistsInDB_NoAccountError=myRepository.nmbExistsInDB_NoAccountError



    val authorizationNetworkError=myRepository.authorizationNetworkError
    val authorizationNetworkSuccess=myRepository.authorizationSuccess

    val smsResendNetworkError=myRepository.smsResendNetworkError
    val smsResendNetworkSuccess=myRepository.smsResendSuccess

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
            myRepository.sendRegistationToServer(phone = phoneNumber,smsResend = smsResend,verificationMethod = verificationMethod)
         }
    }

    fun resetRegistrationNetSuccess(){
        myRepository.resetRegistrationNetSuccess()
    }

    fun resetRegistrationNetErrorr(){
        myRepository.resetRegistrationNetError()
    }



    //add number to existing account fragment
    fun addNumberToAccountButtonClicked(phoneNumber:String,email:String,password:String,smsResend: Boolean=false,verificationMethod: String){
        enteredPhoneNumber=phoneNumber
        enteredEmail=email
        enteredPassword=password

        //send  phone, email and pass to server and go to authorization fragment
        viewModelScope.launch {
            myRepository.assignPhoneNumberToAccount(
                phone = phoneNumber,
                email = email,
                password = password,
                smsResend = smsResend,
                verificationMethod = verificationMethod)
        }
    }

    fun resetAddNumberToAccountNetSuccess() {
        myRepository.resetAssignPhoneNumberToAccountNetSuccess()
    }

    fun resetAddNumberToAccountNetError(){
        myRepository.resetAssignPhoneNumberToAccountNetError()
    }




    //number exists in DB fragment
    fun numberExistsInDBVerifyAccount(email: String,password: String,smsResend: Boolean=false,verificationMethod: String){
        enteredEmail=email
        enteredPassword=password
        if(enteredPhoneNumber!=null) {
            viewModelScope.launch {
                myRepository.numberExistsInDBVerifyAccount(
                    enteredPhoneNumber?:"",
                    email,
                    password,
                    smsResend,
                    verificationMethod = verificationMethod
                )
            }
        }
    }

    fun numberExistsInDb_NoAccount(smsResend: Boolean=false,verificationMethod: String){
    if(enteredPhoneNumber!=null) {
        viewModelScope.launch {
            myRepository.numberExistsInDB_NOAccount(enteredPhoneNumber?:"", smsResend,verificationMethod = verificationMethod)
        }
    }
    }

    fun resetNmbExistsInDB_VerifyAccount_NetSuccess(){
        myRepository.resetNmbExistsInDB_VerifyAccount_NetSuccess()
    }

    fun resetNmbExistsInDB_VerifyAccount_NetError(){
        myRepository.resetNmbExistsInDB_VerifyAccount_NetError()
    }

    fun resetNmbExistsInDB_NOAccount_NetSuccess(){
        myRepository.resetNmbExistsInDB_NOAccount_NetSuccess()
    }

    fun resetNmbExistsInDB_NOAccount_NetError(){
        myRepository.resetNmbExistsInDB_NOAccount_NetError()
    }





    //authorization fragment
    fun submitButtonClicked(smsToken:String){
        if(enteredPhoneNumber!=null) {
            viewModelScope.launch {
                myRepository.authorizeThisUser(
                    enteredPhoneNumber?:"",
                    smsToken,
                    enteredEmail ?: "",
                    enteredPassword ?: ""
                )
            }
        }

    }



    fun resetAuthorization_NetSuccess(){
        myRepository.resetAuthorization_NetSuccess()
    }

    fun resetAuthorization_NetError(){
        myRepository.resetAuthorization_NetError()
    }

    fun resetSMSResend_NetSuccess(){
        myRepository.resetSMSResend_NetSuccess()
    }

    fun resetSMSResend_NetError(){
        myRepository.resetSMSResend_NetError()
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