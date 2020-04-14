package com.vertial.fivemiov.ui.sipfragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.database.MyDatabaseDao
import com.vertial.fivemiov.model.User
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

private val MYTAG="MY_SIPVIewMOdel"

class SipViewModel(val mydatabaseDao: MyDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _timeout= MutableLiveData<Boolean>()
    val timeout:LiveData<Boolean>
        get() = _timeout

    private val _timeoutReg= MutableLiveData<Boolean>()
    val timeoutReg:LiveData<Boolean>
        get() = _timeoutReg

    /*private val _timeoutCallEnded= MutableLiveData<Boolean>()
    val timeoutCallEnded:LiveData<Boolean>
        get() = _timeoutCallEnded*/

    private val _navigateUp= MutableLiveData<Boolean>()
    val navigateUp:LiveData<Boolean>
        get() = _navigateUp

    fun getSipAccountInfo():User?{
        var myUser:User?=null
        viewModelScope.launch {
            val userDeferred = async(IO) {
                    mydatabaseDao.getUser()

            }

            try {
                val user=userDeferred.await()
                myUser=user.value
            }catch (e: Throwable) {
                Log.i(MYTAG, e.message ?: "no message")


            }

        }
        return myUser

    }

    fun startTimeout(){
        viewModelScope.launch {
            delay(1000)
            _timeout.value=true
         }

    }

    fun timeoutFinished(){
        _timeout.value=false
    }

    fun startRegTimeout(){
        viewModelScope.launch {
            delay(5000)
            _timeoutReg.value=true
        }
    }

    fun timeoutRegFinished(){
        _timeoutReg.value=false
    }

    /*fun callEndedTimeout(){
        viewModelScope.launch {
            delay(1000)
            _timeoutReg.value=true
        }
    }

    fun callEndedTimeoutFinished(){
        _timeoutCallEnded.value=false
    }*/

    fun navigateBack(){
        viewModelScope.launch {
            delay(1000)
            _navigateUp.value=true
        }
    }

    fun navigateBackFinished(){
        _navigateUp.value=false
    }
}