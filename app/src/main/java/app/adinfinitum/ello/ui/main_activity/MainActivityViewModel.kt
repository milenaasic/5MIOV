package app.adinfinitum.ello.ui.main_activity

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.model.PhoneBookItem
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.data.RepoLogToServer
import app.adinfinitum.ello.data.RepoUser
import app.adinfinitum.ello.model.ContactItemWithInternationalNumbers
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


private const val MY_TAG="MY_MainActivViewModel"
class MainActivityViewModel(val myRepository: RepoContacts,
                            val myRepoUser:RepoUser,
                            val myRepoLogToServer: RepoLogToServer,
                            application: Application) : AndroidViewModel(application) {



    //live data
    val userData=myRepoUser.getUserData()


    //setAccountDisclaimer
    private val _shouldShowSetAccountDisclaimer = MutableLiveData<Boolean>()
    val shouldShowSetAccountDisclaimer: LiveData<Boolean>
        get() = _shouldShowSetAccountDisclaimer


    init {
        Log.i(MY_TAG,("init"))

    }

    fun showSetAccountDisclaimer(){
        viewModelScope.launch {
            try {
                val user= myRepoUser.getUser()

                if(user.userEmail.equals(EMPTY_EMAIL) && !checkForSharedPrefDisclamerShownValue()){
                    Log.i(MY_TAG,"user is $user, disclaimer was not shown")
                    _shouldShowSetAccountDisclaimer.value=true}

            }catch (e:Exception){
                Log.i(MY_TAG,"showSetAccountDisclaimer(), ${e.message}")
            }


        }

    }

    fun setAccountDialogDiscalimerShown(){
        _shouldShowSetAccountDisclaimer.value=false
    }


    private fun checkForSharedPrefDisclamerShownValue():Boolean{

        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            DEFAULT_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        var wasShown=false

        if(sharedPreferences.contains(DISCLAIMER_WAS_SHOWN)){
            wasShown=sharedPreferences.getBoolean(DISCLAIMER_WAS_SHOWN,false)
            Log.i(MY_TAG," sharedPreferences, dislaimerWasShown: $wasShown")
        }else{
            sharedPreferences.edit().putBoolean(DISCLAIMER_WAS_SHOWN,false).apply()
        }

        return wasShown
    }


    fun logStateOrErrorToMyServer(options:Map<String,String>){
        getApplication<MyApplication>().applicationScope.launch {
                myRepoLogToServer.logStateOrErrorToServer(myoptions = options)

        }

    }



}