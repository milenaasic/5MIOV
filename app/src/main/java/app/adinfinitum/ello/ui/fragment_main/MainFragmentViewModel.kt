package app.adinfinitum.ello.ui.fragment_main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.model.ContactItem
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

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
                val lowerCaseQuery: String = query.toLowerCase(Locale.getDefault())

                val filteredContactsList: MutableList<ContactItem> = ArrayList()
                for (item in fullList) {
                    if (item.name.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                        filteredContactsList.add(item)
                    }
                }
                _contactList.value=filteredContactsList
                _numberOfSelectedContacts.value=filteredContactsList.size
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

     fun logStateToServer(process:String, state:String){
        repoContacts.logStateToServer(process = process,state = state)
    }


}