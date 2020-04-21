package com.vertial.fivemiov.ui.RegistrationAuthorization

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.api.NetResponse_Registration
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


    init {
        Log.i(MY_TAG,("init"))
    }

    fun resetSignUpParameters(){
        Log.i(MY_TAG,"reset user paramtri PRE su $enteredPhoneNumber, $enteredEmail, $enteredPassword, $signInParameter")
        enteredEmail=null
        enteredPassword=null
        signInParameter=null
        Log.i(MY_TAG,"reset user paramtri su $enteredPhoneNumber,$enteredEmail, $enteredPassword, $signInParameter")
    }


    //registration fragment
    fun registerButtonClicked(phoneNumber:String, smsResend:Boolean){
        enteredPhoneNumber=phoneNumber
        Log.i(MY_TAG,("registration button clicked sa smsresend $smsResend"))
        //send registrtion phone number to server and go to authorization fragment
        viewModelScope.launch {
            myRepository.sendRegistationToServer(phoneNumber,smsResend)
         }
    }

    fun resetRegistrationNetSuccess(){
        myRepository.resetRegistrationNetSuccess()
    }

    fun resetRegistrationNetErrorr(){
        myRepository.resetRegistrationNetError()
    }



    //add number to existing account fragment
    fun addNumberToAccountButtonClicked(phoneNumber:String,email:String,password:String,smsResend: Boolean=false){
        enteredPhoneNumber=phoneNumber
        enteredEmail=email
        enteredPassword=password
        Log.i(MY_TAG,("add number button clicked resend je $smsResend"))
        //send add phone, email and pass to server and go to authorization fragment
        viewModelScope.launch {
            myRepository.assignPhoneNumberToAccount(phoneNumber,email,password,smsResend)
        }
    }

    fun resetAddNumberToAccountNetSuccess() {
        myRepository.resetAssignPhoneNumberToAccountNetSuccess()
    }

    fun resetAddNumberToAccountNetError(){
        myRepository.resetAssignPhoneNumberToAccountNetError()
    }




    //number exists in DB fragment
    fun numberExistsInDBVerifyAccount(email: String,password: String,smsResend: Boolean=false){
        enteredEmail=email
        enteredPassword=password
        if(enteredPhoneNumber!=null) {
            viewModelScope.launch {
                myRepository.numberExistsInDBVerifyAccount(
                    enteredPhoneNumber?:"",
                    email,
                    password,
                    smsResend
                )
            }
        }
    }

    fun numberExistsInDb_NoAccount(smsResend: Boolean=false){
    if(enteredPhoneNumber!=null) {
        viewModelScope.launch {
            myRepository.numberExistsInDB_NOAccount(enteredPhoneNumber?:"", smsResend)
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

    fun resendSMS(phoneNumber:String){
        viewModelScope.launch {
            myRepository.resendSMS(phoneNumber)
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


}