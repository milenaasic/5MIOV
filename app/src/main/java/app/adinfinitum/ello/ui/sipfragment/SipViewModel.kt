package app.adinfinitum.ello.ui.sipfragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.api.NetResponse_GetSipAccessCredentials
import app.adinfinitum.ello.data.*
import app.adinfinitum.ello.ui.registrationauthorization.Event
import app.adinfinitum.ello.model.User
import app.adinfinitum.ello.ui.myapplication.MyApplication
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlin.Exception

private val MYTAG="MY_SIPVIEWMODEL"

class SipViewModel( val mySipRepo: RepoSIPE1,
                    val myRepoUser: IRepoUser,
                    val myRepoRemoteDataSource: IRepoRemoteDataSource,
                    val myRepoLogToServer: IRepoLogToServer,
                    val myRepoLogOut: IRepoLogOut,
                    application: Application) : AndroidViewModel(application) {

    val TIMEOUT_IN_MILLIS=500L
    val TIMEOUT_IN_MILLIS_NAVIGATE_BACK=2000L

    private val _timeout= MutableLiveData<Boolean>()
    val timeout:LiveData<Boolean>
        get() = _timeout

    /*private val _timeoutReg= MutableLiveData<Boolean>()
    val timeoutReg:LiveData<Boolean>
        get() = _timeoutReg*/

    private val _navigateUp= MutableLiveData<Boolean>()
    val navigateUp:LiveData<Boolean>
        get() = _navigateUp

    private val _getSipCredentialsNetSuccess= MutableLiveData<Event<NetResponse_GetSipAccessCredentials>>()
    val getSipCredentialsNetSuccess:LiveData<Event<NetResponse_GetSipAccessCredentials>>
        get() = _getSipCredentialsNetSuccess

    private val _getSipAccessCredentialsNetError= MutableLiveData<Event<String>>()
    val getSipAccessCredentialsNetError:LiveData<Event<String>>
        get() = _getSipAccessCredentialsNetError


    // new sip credentials
    fun getSipAccountCredentials(){

        viewModelScope.launch{
            try {
                val myUser= myRepoUser.getUser()

                if(myUser.userToken.isNotEmpty() && myUser.userPhone.isNotEmpty()) {
                    val result =
                        myRepoRemoteDataSource.getSipAccessCredentials(
                            token = myUser.userToken,
                            phone = myUser.userPhone
                        )

                    when(result){

                        is Result.Success->{
                            if (result.data.authTokenMismatch == true) myRepoLogOut.logoutAll()
                             else {
                                _getSipCredentialsNetSuccess.value=Event(result.data)
                            }
                        }
                        is Result.Error->{

                            myRepoLogToServer.logStateOrErrorToServer(myoptions = mapOf(Pair("getSipAccountCredentials() Error"," ${result.exception.message}")))
                            _getSipAccessCredentialsNetError.value=Event(result.exception.message?:"")
                        }
                    }
                }

            }catch (e: Exception) {
                Log.i(MYTAG,"error in GetSipAccountCredentials ${e.message}")
            }

         }
    }

    //reset sip credentials
    fun resetSipCredentials(){

        getApplication<MyApplication>().applicationScope.launch {
            Log.i(MYTAG, "resetSipCredentials(),applicationScope.launch $this")
            try {
                    val myUser = myRepoUser.getUser()
                    myRepoRemoteDataSource.resetSipAccess(
                        authToken = myUser.userToken,
                        phone = myUser.userPhone
                    )
            }catch (t:Throwable){
                Log.i(
                    MYTAG,
                    "resetSipCredentials(), applicationScope message ${t.message}"
                )
            }
        }
    }


    fun startTimeout(){
        viewModelScope.launch {
            delay(TIMEOUT_IN_MILLIS)
            _timeout.value=true
         }
    }

    fun timeoutFinished(){
        _timeout.value=false
    }


    fun navigateBack(){
        viewModelScope.launch {
            delay(TIMEOUT_IN_MILLIS_NAVIGATE_BACK)
            _navigateUp.value=true
        }
    }

    fun navigateBackFinished(){
        _navigateUp.value=false
    }

     fun logStateOrErrorToServer(options:Map<String,String>){
        getApplication<MyApplication>().applicationScope.launch {
                myRepoLogToServer.logStateOrErrorToServer(myoptions = options)
        }
     }
}

