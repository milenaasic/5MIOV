  package app.adinfinitum.ello.ui.fragment_dial_pad

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.R
import app.adinfinitum.ello.data.*
import app.adinfinitum.ello.model.RecentCall
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.ui.registrationauthorization.Event
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


private val MYTAG="MY_DialPadVIewMOdel"
class DialpadFragmViewModel(val myRepository: RepoContacts,
                            val myRepoUser: IRepoUser,
                            val myRepoRecentCalls: IRepoRecentCalls,
                            val myRepoPrenumberAndWebApiVer: IRepoPrenumberAndWebApiVer,
                            val myRepoRemoteDataSource: IRepoRemoteDataSource,
                            val myRepoLogOut: IRepoLogOut,
                            val myRepoLogToServer: IRepoLogToServer,
                            application: Application) : AndroidViewModel(application) {




    //Live data
    val userData=myRepoUser.getUserData()
    val myPrenumber=myRepoPrenumberAndWebApiVer.getPremunber()
    val recentCallList=myRepoRecentCalls.getAllRecentCalls()

    private val _credit=MutableLiveData<Event<String>>()
    val credit:LiveData<Event<String>>
    get() = _credit


    fun getCredit() {
        viewModelScope.launch {
            try {
                val user=myRepoUser.getUser()
                val result= myRepoRemoteDataSource.getCredit(phone = user.userPhone, token = user.userToken)

                when(result){
                    is Result.Success->{
                            if(result.data.authTokenMismatch==true) {
                                    myRepoLogOut.logoutAll()
                            }else{
                                if(result.data.success==true){

                                    val application=getApplication<MyApplication>()
                                    _credit.value=Event(application.resources.getString(R.string.current_credit,result.data.credit,result.data.currency))

                                   result.data.e1phone?.let {e1number->
                                        if(e1number.isNotEmpty()&&e1number.isNotBlank()) {
                                                myRepoPrenumberAndWebApiVer.updatePrenumber(e1number,System.currentTimeMillis())
                                        }
                                   }

                                    result.data.appVersion?.let {appVer->
                                        if(appVer.isNotEmpty()&& appVer.isNotBlank()) {
                                                myRepoPrenumberAndWebApiVer.updateWebApiVersion(appVer)
                                        }
                                    }
                                }
                            }

                    }

                    is Result.Error->{
                            _credit.value=Event(" ")
                           myRepoLogToServer.logStateOrErrorToServer(myoptions = mapOf(
                                                    Pair("process:","DialPad fragment getCredit"),
                                                    Pair("error"," ${result.exception.message}")
                                                    )
                           )
                    }

                }

            }catch (e:Exception){

            }

        }
    }



    fun insertCallIntoDB(call: RecentCall){
        getApplication<MyApplication>().applicationScope.launch {
            myRepoRecentCalls.insertRecentCall(call)
        }
    }


     fun logStateOrErrorToMyServer(options:Map<String,String>){
        getApplication<MyApplication>().applicationScope.launch {
                try {
                    myRepoLogToServer.logStateOrErrorToServer(myoptions = options)
                } catch (e: Exception) {
                    Log.i(MYTAG, "in logStateOrErrorToMyServer applicationScope error ${e.message}")
                }
        }


    }

}