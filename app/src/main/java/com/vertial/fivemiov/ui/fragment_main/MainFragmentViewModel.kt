package com.vertial.fivemiov.ui.fragment_main

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.model.ContactItem
import com.vertial.fivemiov.utils.did24HoursPass
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.lang.Exception

private val MYTAG="MY_MAINFRAGM_VIEWMODEL"

class MainFragmentViewModel(val repoContacts: RepoContacts,application: Application) :AndroidViewModel(application) {

    //private lateinit var jobInProgress:Deferred<List<ContactItem>>

    //live data from database
    val userData=repoContacts.getUserData()

    private val _contactList = MutableLiveData<List<ContactItem>>()
    val contactList: LiveData<List<ContactItem>>
        get() = _contactList

    private val _numberOfSelectedContacts = MutableLiveData<Int>()
    val numberOfSelectedContacts: LiveData<Int>
        get() = _numberOfSelectedContacts

    private val _currentSearchString = MutableLiveData<String>()
    val currentSearchString: LiveData<String>
        get() = _currentSearchString



    /*fun cancelOngoingJob(){
        try {
           jobInProgress.cancel()
            Log.i(MYTAG,"canceled jobs")
        }catch (e:Throwable){
            Log.i(MYTAG,"canceling jobs ${e.message}")
        }

    }*/

    fun populateContactList(searchString:String?) {

        viewModelScope.launch {
            Log.i(MYTAG, " search string je $searchString")
            when {
                searchString.isNullOrEmpty() -> getContacts(ContactsContract.Contacts.CONTENT_URI)
                else -> getContacts(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(searchString)))
            }
            _currentSearchString.value = searchString

        }
    }

    private fun getContacts(uri: Uri){

        viewModelScope.launch {

             val defContactList=async (IO) {
                repoContacts.getAllContacts(uri)
            }
            try {
                val resultList=defContactList.await()
                _contactList.value=resultList
                _numberOfSelectedContacts.value=resultList.size
                Log.i(MYTAG,"result je setovan")
            }catch (e:Exception){
                Log.i(MYTAG,e.message?:"no message")
            }
        }


    }

    fun getE1PrenumberIf24hPassed(){
    //uzmi timestamp iz baze i proveri da li je proslo 24h
        var myToken=""
        var myPhoneNumber=""
        viewModelScope.launch {
            val defUser = async(IO) {
                repoContacts.getUser()
            }
            try {
                val user = defUser.await()
                myToken = user.userToken
                myPhoneNumber=user.userPhone
            } catch (t: Throwable) {
                Log.i(MYTAG, "nije pokupio usera iz baze ${t.message} ")
            }
        }

        if(myToken.isNotEmpty() && myPhoneNumber.isNotEmpty()) {
            viewModelScope.launch {
                val defTimestamp = async(IO) {
                    repoContacts.getE1Timestamp()
                }
                try {
                    val timestamp = defTimestamp.await()
                    Log.i(
                        MYTAG,
                        "timestamp je $timestamp, a system time je ${System.currentTimeMillis()} , token je $myToken"
                    )
                    if (did24HoursPass(System.currentTimeMillis(), timestamp)) {
                        Log.i(MYTAG, "usao u did 24 hours passed ")
                        repoContacts.refreshE1(phoneNumber= myPhoneNumber,token=myToken)
                    }

                } catch (e: Throwable) {
                    Log.i(MYTAG, "24 hours passed ${e.message} ")
                }

            }
        }


    }

}