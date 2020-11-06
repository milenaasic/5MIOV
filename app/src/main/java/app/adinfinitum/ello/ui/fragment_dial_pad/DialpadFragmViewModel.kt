package app.adinfinitum.ello.ui.fragment_dial_pad

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.R
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.model.RecentCall
import app.adinfinitum.ello.data.Result
import app.adinfinitum.ello.data.logoutAll
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.ui.registrationauthorization.Event
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


private val MYTAG="MY_DialPadVIewMOdel"
class DialpadFragmViewModel(val myRepository: RepoContacts, application: Application) : AndroidViewModel(application) {


    val myPrenumber=myRepository.getPremunber()

    //live data from database
    val userData=myRepository.getUserData()

    val recentCallList=myRepository.getAllRecentCalls()

    private val _credit=MutableLiveData<Event<String>>()
    val credit:LiveData<Event<String>>
    get() = _credit


    fun getCredit() {
        viewModelScope.launch {
            try {
                val result=withContext(IO){
                    val user=myRepository.getUser()
                    myRepository.getCredit(phone = user.userPhone, token = user.userToken)

                }

                when(result){
                    is Result.Success->{
                            if(result.data.authTokenMismatch==true) {
                                withContext(IO){
                                    logoutAll(getApplication())
                                }

                            }else{
                                if(result.data.success==true){

                                    val application=getApplication<MyApplication>()
                                    _credit.value=Event(application.resources.getString(R.string.current_credit,result.data.credit,result.data.currency))

                                   result.data.e1phone?.let {e1number->
                                        if(e1number.isNotEmpty()&&e1number.isNotBlank()) {
                                            withContext(IO){
                                                myRepository.updatePrenumber(e1number,System.currentTimeMillis())
                                            }
                                        }
                                   }

                                    result.data.appVersion?.let {appVer->
                                        if(appVer.isNotEmpty()&& appVer.isNotBlank()) {
                                            withContext(IO){
                                                myRepository.updateWebApiVersion(appVer)
                                            }
                                        }
                                    }
                                }
                            }
                    }

                    is Result.Error->_credit.value=Event(" ")

                }

            }catch (e:Exception){

            }

        }
    }



    fun insertCallIntoDB(call: RecentCall){
        GlobalScope.launch {
            withContext(Dispatchers.IO){
                myRepository.insertRecentCall(call)
            }
        }


    }


    fun logStateToMyServer(process:String,state:String){
        myRepository.logStateToServer(process = process,state = state)
    }

}