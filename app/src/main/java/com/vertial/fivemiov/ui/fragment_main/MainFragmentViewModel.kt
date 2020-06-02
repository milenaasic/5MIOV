package com.vertial.fivemiov.ui.fragment_main

import android.app.Application
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.model.ContactItem
import com.vertial.fivemiov.model.ContactItemWithInternationalNumbers
import com.vertial.fivemiov.model.RawContactWithoutNumber
import com.vertial.fivemiov.utils.did24HoursPass
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.Comparator

private val MYTAG="MY_MAINFRAGM_VIEWMODEL"

class MainFragmentViewModel(val repoContacts: RepoContacts,application: Application) :AndroidViewModel(application) {


    //live data from database
    val userData=repoContacts.getUserData()
    var fullContactListWithInternationalNumbers:List<ContactItem>?=null

    private val _contactList = MutableLiveData<List<ContactItem>>()
    val contactList: LiveData<List<ContactItem>>
        get() = _contactList


    private val _numberOfSelectedContacts = MutableLiveData<Int>()
    val numberOfSelectedContacts: LiveData<Int>
        get() = _numberOfSelectedContacts

    //logging out zbog token mismatch
    val loggingOut=repoContacts.loggingOut
    fun resetLoggingOutToFalse(){
        repoContacts.resetLoggingOutToFalse()
    }



    fun populateContactList() {
        viewModelScope.launch {
           getAllRawContacts()
        }
    }


    fun querryContactList(query:String?){
        val fullList=fullContactListWithInternationalNumbers

        if(!fullList.isNullOrEmpty()){
            if(query!=null) {
                val lowerCaseQuery: String = query.toLowerCase()

                val filteredContactsList: MutableList<ContactItem> = ArrayList()
                for (item in fullList) {
                    if (item.name.toLowerCase().contains(lowerCaseQuery)) {
                        filteredContactsList.add(item)
                    }
                }
                _contactList.value=filteredContactsList
                _numberOfSelectedContacts.value=filteredContactsList.size
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

   private fun  getAllRawContacts(){
        viewModelScope.launch {

            val defResultLIst= async(IO) {
                repoContacts.getAllRawContactWithInternPhoneNumber()
            }
            try {
                val resultList=defResultLIst.await()
                fullContactListWithInternationalNumbers=resultList
                _contactList.value=resultList
                _numberOfSelectedContacts.value=resultList.size

            }catch (t:Throwable){
                Log.i(MYTAG, " getAllRawContacts error ${t.message}")

            }
        }
    }

    /*private fun  getPhoneBook(){
        viewModelScope.launch {

            val defResultLIst= async(IO) {
                repoContacts.getRawContactsPhonebook()
            }
            try {
                val resultList=defResultLIst.await()

            }catch (t:Throwable){
                Log.i(MYTAG, " getAllRawContacts error ${t.message}")

            }
        }
    }*/

}