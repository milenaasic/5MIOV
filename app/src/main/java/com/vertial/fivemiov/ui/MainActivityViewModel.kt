package com.vertial.fivemiov.ui

import android.app.Application
import android.database.Cursor
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.PhoneBookItem
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.ui.fragment_detail_contact.PhoneItem
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.lang.Exception


private const val MY_TAG="MY_MainActivViewModel"
class MainActivityViewModel(val myRepository: RepoContacts, application: Application) : AndroidViewModel(application) {



    //live data from database
    val userData=myRepository.getUserData()

    //phonebook
    private val _phoneBook = MutableLiveData<List<PhoneBookItem>>()
    val phoneBook: LiveData<List<PhoneBookItem>>
        get() = _phoneBook

    val phoneBookExported=myRepository.exportPhoneBookNetworkSuccess

    init {
        Log.i(MY_TAG,("init"))

    }

    fun getPhoneBook(){
        Log.i(MY_TAG,"get phone boook")

        viewModelScope.launch {
            val deferredList=viewModelScope.async(IO){
             myRepository.getAllContacts(ContactsContract.Contacts.CONTENT_URI)
            }
            try {
                val phoneBookList= mutableListOf<PhoneBookItem>()
                val resultList=deferredList.await()
                Log.i(MY_TAG,"get phone book lista $resultList")
                val defferedPhones=(resultList.indices).map {
                    viewModelScope.async(IO) {
                        val list=myRepository.getPhoneNumbersForContact(resultList[it].lookUpKey)
                        val phoneArray=convertPhoneListToPhoneArray(list)
                        Log.i(MY_TAG,"get phone book phonearray ${phoneArray.toList()}")
                        phoneBookList.add(PhoneBookItem(resultList[it].name,phoneArray))
                    }
                }

                val resultP=defferedPhones.map { it.await() }
                Log.i(MY_TAG,"get phone book resul svih deferred je ${resultP}")
                Log.i(MY_TAG,"get phone book phone book je  ${phoneBookList}")
                _phoneBook.value=phoneBookList



            }catch (e:Exception){
                Log.i(MY_TAG,e.message?:"no message")
            }
          }
          //Log.i(MY_TAG,"phone book je $phoneBookList")
          //return phoneBookList

    }

    private fun convertPhoneListToPhoneArray(phoneList: List<PhoneItem>): Array<String> {
        val resultList= mutableListOf<String>()
        for(item in phoneList){
            resultList.add(PhoneNumberUtils.normalizeNumber(item.phoneNumber))
        }
        return resultList.toTypedArray()

    }

    fun exportPhoneBook(phoneBook:List<PhoneBookItem>){
        viewModelScope.launch {
            myRepository.exportPhoneBook(phoneBook)
        }

    }

}