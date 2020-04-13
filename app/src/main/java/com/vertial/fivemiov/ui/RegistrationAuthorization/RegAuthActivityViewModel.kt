package com.vertial.fivemiov.ui.RegistrationAuthorization

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoSIPE1
import kotlinx.coroutines.launch

private const val MY_TAG="MY_RegAuthActivVieModel"

class RegAuthActivityViewModel(val myRepository: Repo, val mySIPE1Repo:RepoSIPE1,application: Application) : AndroidViewModel(application) {

    lateinit var enteredPhoneNumber:String
    var enteredEmail:String?=null
    var enteredPassword:String?=null
    var signIn:Boolean?=null

    val userData=myRepository.getUserData()


    val registrationNetworkError=myRepository.registrationNetworkError
    val registrationNetSuccessIsNmbAssigned=myRepository.registrationSuccessIsNmbAssigned

    val addNumberToAccuntNetworkError=myRepository.addNumberToAccountNetworkError
    val addNumberToAccuntNetworkSuccess=myRepository.addNumberToAccountNetworkSuccess


    val nmbExistsInDBUserHasAccountSuccess=myRepository.nmbExistsInDBUserHasAccountSuccess
    val nmbExistsInDBUserHasAccountError=myRepository.nmbExistsInDBUserHasAccountError
    val nmbExistsInDB_NoAccountSuccess=myRepository.nmbExistsInDB_NoAccountSuccess
    val nmbExistsInDB_NoAccountError=myRepository.nmbExistsInDB_NoAccountError



    val authorizationNetworkError=myRepository.authorizationNetworkError
    val authorizationNetworkSuccess=myRepository.authorizationSuccess
    val smsResendNetworkError=myRepository.smsResendNetworkError
    val smsResendNetworkSuccess=myRepository.smsResendNetworkError


    init {
        Log.i(MY_TAG,("init"))
    }

    //registration fragment
    fun registerButtonClicked(phoneNumber:String){
        enteredPhoneNumber=phoneNumber
        Log.i(MY_TAG,("registration button clicked"))
        //send registrtion phone number to server and go to authorization fragment
        viewModelScope.launch {
            myRepository.sendRegistationToServer(phoneNumber)
         }

    }

    //add number to existing account fragment
    fun addNumberToAccountButtonClicked(phoneNumber:String,email:String,password:String){
        enteredPhoneNumber=phoneNumber
        enteredEmail=email
        enteredPassword=password
        Log.i(MY_TAG,("add number button clicked"))
        //send add phone, email and pass to server and go to authorization fragment
        viewModelScope.launch {
            myRepository.assignPhoneNumberToAccount(phoneNumber,email,password)
        }
    }


    //number exists in DB fragment
    fun numberExistsInDBVerifyAccount(email: String,password: String){
        enteredEmail=email
        enteredPassword=password
        viewModelScope.launch {
            myRepository.numberExistsInDBVerifyAccount(enteredPhoneNumber,email,password)
        }
    }

    fun numberExistsInDb_NoAccount(){
        viewModelScope.launch {
            myRepository.numberExistsInDB_NOAccount(enteredPhoneNumber)
        }
    }





    //authorization fragment
    fun submitButtonClicked(smsToken:String){

        viewModelScope.launch {
            myRepository.authorizeThisUser(enteredPhoneNumber,smsToken,enteredEmail?:"",enteredPassword?:"")
        }

    }

    fun resendSMS(phoneNumber:String){
        viewModelScope.launch {
            myRepository.resendSMS(phoneNumber)
        }

    }
}