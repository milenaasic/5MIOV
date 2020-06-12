package com.vertial.fivemiov.ui.webView

import android.app.Application
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.model.PhoneBookItem
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.model.ContactItem
import com.vertial.fivemiov.model.PhoneItem
import com.vertial.fivemiov.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

private const val MY_TAG="MY_WebViewActivViewMode"
class WebViewViewModel(val myRepository: RepoContacts, application: Application) : AndroidViewModel(application) {

    //user
    val user=myRepository.getUserData()

    //phonebook
    private val _startGetingPhoneBook = MutableLiveData<Boolean>()
    val startGetingPhoneBook: LiveData<Boolean>
        get() = _startGetingPhoneBook

    private val _phoneBook = MutableLiveData<List<PhoneBookItem>>()
    val phoneBook: LiveData<List<PhoneBookItem>>
        get() = _phoneBook

    val phoneBookExported=myRepository.exportPhoneBookWebViewNetworkSuccess

    //logging out zbog token mismatch
    val loggingOut=myRepository.loggingOut
    fun resetLoggingOutToFalse(){
        myRepository.resetLoggingOutToFalse()
    }

    init {
        startGetingPhoneBook()
    }

    fun startGetingPhoneBook(){
        _startGetingPhoneBook.value=true
    }

    fun resetStartGetingPhoneBook(){
        _startGetingPhoneBook.value=false
    }


    fun getPhoneBook(){
       Log.i(MY_TAG,"get phone boook from webview")

        viewModelScope.launch {

            val deferredList = viewModelScope.async(IO) {
                myRepository.getRawContactsPhonebook()
            }
            try {

                    val resultList = deferredList.await()
                    if (!resultList.isNullOrEmpty()) _phoneBook.value = resultList

            } catch (t: Throwable) {
                    Log.i(MY_TAG, t.message ?: "no message")
            }


        }

    }



    fun exportPhoneBook(phoneBook:List<PhoneBookItem>){

        val myUser=user.value

        if(myUser!=null
            && myUser.userPhone!= EMPTY_PHONE_NUMBER && myUser.userPhone.isNotEmpty()
            && myUser.userToken!= EMPTY_TOKEN && myUser.userToken.isNotEmpty()
            ) {
                viewModelScope.launch {
                        myRepository.exportPhoneBook(myUser.userToken,myUser.userPhone,phoneBook)
                }
             }

    }

    fun phoneBookExportFinished(){
        myRepository.phoneBookExportFinishedFromWebView()
    }





}