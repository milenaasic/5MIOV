package com.vertial.fivemiov.ui.sipfragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoSIPE1
import com.vertial.fivemiov.data.UncancelableJobSip
import com.vertial.fivemiov.database.MyDatabaseDao
import com.vertial.fivemiov.model.User
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.lang.Exception

private val MYTAG="MY_SIPVIEWMODEL"

class SipViewModel(val mySipRepo: RepoSIPE1,  application: Application) : AndroidViewModel(application) {

    private val _timeout= MutableLiveData<Boolean>()
    val timeout:LiveData<Boolean>
        get() = _timeout

    private val _timeoutReg= MutableLiveData<Boolean>()
    val timeoutReg:LiveData<Boolean>
        get() = _timeoutReg


    private val _navigateUp= MutableLiveData<Boolean>()
    val navigateUp:LiveData<Boolean>
        get() = _navigateUp


    val getSipCredentialsNetSuccess=mySipRepo.getSipAccessCredentialsNetSuccess
    val getSipAccessCredentialsNetError=mySipRepo.getSipAccessCredentialsNetError

    //token mismatch logging out
    val loggingOut=mySipRepo.loggingOut
    fun resetLoggingOutToFalse(){
        mySipRepo.resetLoggingOutToFalse()
    }

    // new sip credentials
    fun getSipAccountCredentials(){

        viewModelScope.launch{
            val deferredUser = viewModelScope.async(IO) {

                mySipRepo.getUserNoLiveData()
            }
            try {
                val myUser = deferredUser.await()
                if(myUser.userToken.isNotEmpty() && myUser.userPhone.isNotEmpty()) mySipRepo.getSipAccessCredentials(token = myUser.userToken,phone = myUser.userPhone)

            } catch (e: Exception) {
                Log.i(MYTAG,"db error in GetSipAccountCredentials ${e.message}")
            }


            }
    }

    //reset getCredentials
    fun resetgetSipAccountCredentialsNetSuccess(){
        mySipRepo.resetGetSipAccessCredentialsNetSuccess()
    }
    fun resetgetSipAccountCredentialsNetError(){
        mySipRepo.resetGetSipAccessCredentialsNetError()
    }


    //reset sip credentials
    fun resetSipCredentials(){
       mySipRepo.resetSipAccessInBackGround()
    }


    fun startTimeout(){
        viewModelScope.launch {
            delay(500)
            _timeout.value=true
         }

    }

    fun timeoutFinished(){
        _timeout.value=false
    }

    fun startRegTimeout(){
        viewModelScope.launch {
            delay(500)
            _timeoutReg.value=true
        }
    }

    fun timeoutRegFinished(){
        _timeoutReg.value=false
    }

    fun navigateBack(){
        viewModelScope.launch {

            _navigateUp.value=true
        }
    }

    fun navigateBackFinished(){
        _navigateUp.value=false
    }
}