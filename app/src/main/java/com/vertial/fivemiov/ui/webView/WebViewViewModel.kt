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
import java.lang.Exception

private const val MY_TAG="MY_WebViewActivViewMode"
class WebViewViewModel(val myRepository: RepoContacts, application: Application) : AndroidViewModel(application) {

    //user
    val user=myRepository.getUserData()

    //phonebook
    private val _phoneBook = MutableLiveData<List<PhoneBookItem>>()
    val phoneBook: LiveData<List<PhoneBookItem>>
        get() = _phoneBook

    val phoneBookExported=myRepository.exportPhoneBookWebViewNetworkSuccess

    init {
        //getPhoneBook()
    }


    fun getPhoneBook(){
       Log.i(MY_TAG,"get phone boook webview")


        viewModelScope.launch {

            var phoneBookContactsList = listOf<ContactItem>()
            val phoneBookList = mutableListOf<PhoneBookItem>()

            val deferredList = viewModelScope.async(Dispatchers.IO) {
                myRepository.getAllContacts(ContactsContract.Contacts.CONTENT_URI)
            }
            try {
                //val phoneBookList= mutableListOf<PhoneBookItem>()
                val resultListWithEmptyContact = deferredList.await()
                phoneBookContactsList = resultListWithEmptyContact
                Log.i(MY_TAG, " phone book je $phoneBookContactsList")

            } catch (e: Exception) {
                Log.i(MY_TAG, e.message ?: "no message")
            }


            Log.i(MY_TAG, "druga coroutine za telefone  , $phoneBookContactsList")
            if (!phoneBookContactsList.isNullOrEmpty()) {
                //izbaci poslednji prazan kontakt
                val resultList = removeEmptyContactItem(phoneBookContactsList)
                Log.i(MY_TAG, "druga coroutine bez empty  kontakta  , $resultList")
                // pokupi telefone za svaki kontakt
                if (resultList.isNotEmpty()) {

                    val defferedPhones = (resultList.indices).map {
                        viewModelScope.async(IO) {
                            val list =
                                myRepository.getPhoneNumbersForContact(resultList[it].lookUpKey)
                            val phoneArray = convertPhoneListToPhoneArray(list)
                            Log.i(MY_TAG, "get phone book phonearray ${phoneArray.toList()}")
                            phoneBookList.add(
                                PhoneBookItem(
                                    resultList[it].name,
                                    phoneArray
                                )
                            )
                        }
                    }
                    try {
                        val resultSuccessList = defferedPhones.map { it.await() }
                        Log.i(MY_TAG, "get phone book resul svih deferred je ${resultSuccessList}")
                        if (!phoneBookList.isNullOrEmpty()) _phoneBook.value = phoneBookList

                    } catch (t: Throwable) {
                        Log.i(MY_TAG, t.message ?: "no message")
                    }
                }

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

    /*fun loadloadDashboard(){
        viewModelScope.launch {

            val loadDashDef=MyAPI.retrofitService.loadDashboard()
            try {
                val result=loadDashDef.await()
                Log.i(MY_TAG,"result je ${result}")
            }catch (e:Throwable) {
                Log.i(MY_TAG,"exceptione ${e.message}")
            }
        }

    }*/




}