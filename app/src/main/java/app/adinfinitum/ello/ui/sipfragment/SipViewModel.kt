package app.adinfinitum.ello.ui.sipfragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.api.NetResponse_GetSipAccessCredentials
import app.adinfinitum.ello.data.RepoSIPE1
import app.adinfinitum.ello.ui.registrationauthorization.Event
import app.adinfinitum.ello.data.Result
import app.adinfinitum.ello.data.logoutAll
import app.adinfinitum.ello.model.User
import app.adinfinitum.ello.ui.myapplication.MyApplication
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlin.Exception

private val MYTAG="MY_SIPVIEWMODEL"

class SipViewModel(val mySipRepo: RepoSIPE1,  application: Application) : AndroidViewModel(application) {
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
                val myUser= withContext(IO){
                    mySipRepo.getUserNoLiveData()
                }
                if(myUser.userToken.isNotEmpty() && myUser.userPhone.isNotEmpty()) {
                    val result = withContext(IO) {
                        mySipRepo.getSipAccessCredentials(
                            token = myUser.userToken,
                            phone = myUser.userPhone
                        )
                    }

                    when(result){

                        is Result.Success->{
                            if (result.data.authTokenMismatch == true) {
                                    withContext(IO) {
                                        logoutAll(getApplication())
                                    }
                            } else {
                                _getSipCredentialsNetSuccess.value=Event(result.data)
                            }
                        }
                        is Result.Error->{
                            withContext(IO){
                                mySipRepo.logStateOrErrorToOurServer(myoptions = mapOf(Pair("getSipAccountCredentials() Error"," ${result.exception.message}")))
                            }
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
                withContext(IO) {
                    val myUser = mySipRepo.getUserNoLiveData()
                    mySipRepo.resetSipAccess(
                        authToken = myUser.userToken,
                        phone = myUser.userPhone
                    )
                }
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

    /*fun startRegTimeout(){
        viewModelScope.launch {
            delay(TIMEOUT_IN_MILLIS)
            _timeoutReg.value=true
        }
    }

    fun timeoutRegFinished(){
        _timeoutReg.value=false
    }*/


    fun navigateBack(){
        viewModelScope.launch {
            delay(TIMEOUT_IN_MILLIS_NAVIGATE_BACK)
            _navigateUp.value=true
        }
    }

    fun navigateBackFinished(){
        _navigateUp.value=false
    }

   /* fun logCredentialsForSipCall(sipUsername:String?,sipPassword:String?,sipDisplayname:String?,sipServer:String?,stunServer:String?){
        mySipRepo.logCredentialsForSipCall(sipUsername = sipUsername,sipPassword = sipPassword,
            sipDisplayname = sipDisplayname,sipServer = sipServer,stunServer = stunServer)

    }*/

     fun logStateOrErrorToServer(options:Map<String,String>){
        getApplication<MyApplication>().applicationScope.launch {
            withContext(IO) {
                mySipRepo.logStateOrErrorToOurServer(myoptions = options)
            }
        }

    }
}

