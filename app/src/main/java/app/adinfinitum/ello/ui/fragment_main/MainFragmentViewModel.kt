package app.adinfinitum.ello.ui.fragment_main

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.data.Result
import app.adinfinitum.ello.data.logoutAll
import app.adinfinitum.ello.model.ContactItem
import app.adinfinitum.ello.model.PhoneBookItem
import app.adinfinitum.ello.model.User
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.utils.DEFAULT_SHARED_PREFERENCES
import app.adinfinitum.ello.utils.EMPTY_PHONE_NUMBER
import app.adinfinitum.ello.utils.EMPTY_TOKEN
import app.adinfinitum.ello.utils.PHONEBOOK_IS_EXPORTED
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.lang.Exception
import java.util.*

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

    suspend private fun getContacts(uri: Uri){

            try {
                val resultList= withContext(Dispatchers.IO) {
                    repoContacts.getAllContacts(uri)
                }

                _contactList.value=resultList
                _numberOfSelectedContacts.value=resultList.size

            }catch (e: Exception){
                Log.i(MYTAG,e.message?:"no message")
            }

    }


    fun getPhoneBook(){
        Log.i(MYTAG,"get phone boook from Main fragment")

        viewModelScope.launch {

            try {
                withContext(IO) {
                    val resultList= repoContacts.getRawContactsPhonebook()
                    val user=repoContacts.getUser()
                    if (!resultList.isNullOrEmpty()) exportPhoneBook(user,resultList)
                }

            } catch (t: Throwable) {
                Log.i(MYTAG, t.message ?: "no message")
            }
        }
    }


    private suspend fun exportPhoneBook(myUser: User, phoneBook:List<PhoneBookItem>) {

        if (myUser.userPhone != EMPTY_PHONE_NUMBER && myUser.userPhone.isNotEmpty()
            && myUser.userToken != EMPTY_TOKEN && myUser.userToken.isNotEmpty()
        ) {
            try {
                val result =
                    repoContacts.exportPhoneBook(myUser.userToken, myUser.userPhone, phoneBook)

                when(result){
                    is Result.Success->{
                        if(result.data.authTokenMismatch==true) logoutAll(getApplication())
                        else {
                            val sharedPreferences = getApplication<MyApplication>().getSharedPreferences(
                                DEFAULT_SHARED_PREFERENCES,
                                Context.MODE_PRIVATE
                            )
                            if (sharedPreferences.contains(PHONEBOOK_IS_EXPORTED)) {
                                sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED, true)
                                    .apply()

                            }
                        }
                    }
                    is Result.Error->{}
                }

            } catch (e: Exception) {
                Log.i(MYTAG, "export phonebook Failed from main fragment")
            }

        }
    }



     fun logStateToServer(process:String, state:String){
        repoContacts.logStateToServer(process = process,state = state)
    }


}