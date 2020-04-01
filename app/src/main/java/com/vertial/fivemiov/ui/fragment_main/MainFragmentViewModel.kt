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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

private val MYTAG="MY_MAINFRAGM_VIEWMODEL"

class MainFragmentViewModel(val repoContacts: RepoContacts,application: Application) :AndroidViewModel(application) {

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


    init {

    }


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

            val deferredList=async(IO) {
                repoContacts.getAllContacts(uri)
            }
            try {
                val resultList=deferredList.await()
                _contactList.value=resultList
                _numberOfSelectedContacts.value=resultList.size

            }catch (e:Exception){
                Log.i(MYTAG,e.message?:"no message")
            }
        }


    }

}