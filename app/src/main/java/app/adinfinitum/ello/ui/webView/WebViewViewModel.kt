package app.adinfinitum.ello.ui.webView

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.data.*
import app.adinfinitum.ello.model.PhoneBookItem
import app.adinfinitum.ello.model.User
import app.adinfinitum.ello.ui.registrationauthorization.Event
import app.adinfinitum.ello.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import app.adinfinitum.ello.ui.myapplication.MyApplication

private const val MY_TAG="MY_WebViewActivViewMode"
class WebViewViewModel(val myRepository: RepoContacts,
                        val myRepoUser: IRepoUser,
                        val myRepoProvideContacts: IRepoProvideContacts,
                        val myRepoRemoteDataSource: IRepoRemoteDataSource,
                        val myRepoLogOut: IRepoLogOut,
                        val myRepoLogToServer: IRepoLogToServer,
                        application: Application) : AndroidViewModel(application) {

    //LiveData
    val user=myRepoUser.getUserData()

    init {
        startGetingPhoneBook()
    }

    fun startGetingPhoneBook(){
        Log.i(MY_TAG,"startGetingPhoneBook()")
       if(checkForPermission()) getPhoneBook()
    }

    fun checkForPermission():Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        else return checkSelfPermission(getApplication(),Manifest.permission.READ_CONTACTS) == PermissionChecker.PERMISSION_GRANTED

    }


    fun getPhoneBook(){
       Log.i(MY_TAG,"get phone boook from webview")

        viewModelScope.launch {

            try {

                val resultList= myRepoProvideContacts.getRawContactsPhonebook()
                val user=myRepoUser.getUser()
                if (!resultList.isNullOrEmpty()) exportPhoneBook(user,resultList)

            } catch (t: Throwable) {
                    Log.i(MY_TAG, t.message ?: "no message")
            }

        }

    }


    private suspend fun exportPhoneBook(myUser: User, phoneBook:List<PhoneBookItem>) {

        if (myUser.userPhone != EMPTY_PHONE_NUMBER && myUser.userPhone.isNotEmpty()
            && myUser.userToken != EMPTY_TOKEN && myUser.userToken.isNotEmpty()
        ) {
            try {
                val result =
                    myRepoRemoteDataSource.exportPhoneBook(myUser.userToken, myUser.userPhone, phoneBook)

                when(result){
                    is Result.Success->{

                        if(result.data.authTokenMismatch==true) myRepoLogOut.logoutAll()
                        else {
                            val sharedPreferences = getApplication<MyApplication>().getSharedPreferences(
                                DEFAULT_SHARED_PREFERENCES,
                                Context.MODE_PRIVATE
                            )
                            if (sharedPreferences.contains(PHONEBOOK_IS_EXPORTED)) {
                                sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED, true)
                                    .apply()

                            }else{
                                sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED,true).apply()
                                Log.i(MY_TAG," Shared pref value Phonebook_is_exported is created")
                            }
                        }
                    }
                    is Result.Error->{
                        myRepoLogToServer.logStateOrErrorToServer(myoptions =
                                mapOf(Pair("process ","WebView -export phonebool"),
                                        Pair("error message"," ${result.exception.message}")))

                    }
                }

            } catch (e: Exception) {
                Log.i(MY_TAG, "export phonebook Failed ${e.message}")
            }

        }
    }

}

