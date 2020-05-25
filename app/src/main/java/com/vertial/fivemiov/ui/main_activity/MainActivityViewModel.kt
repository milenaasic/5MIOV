package com.vertial.fivemiov.ui.main_activity

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.model.PhoneBookItem
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.model.ContactItem
import com.vertial.fivemiov.model.ContactItemWithInternationalNumbers
import com.vertial.fivemiov.model.PhoneItem
import com.vertial.fivemiov.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.lang.Exception
import java.util.*
import kotlin.Comparator


private const val MY_TAG="MY_MainActivViewModel"
class MainActivityViewModel(val myRepository: RepoContacts, application: Application) : AndroidViewModel(application) {



    //live data from database
    val userData=myRepository.getUserData()


    //setAccountDisclaimer
    private val _shouldShowSetAccountDisclaimer = MutableLiveData<Boolean>()
    val shouldShowSetAccountDisclaimer: LiveData<Boolean>
        get() = _shouldShowSetAccountDisclaimer

    //setAccountDisclaimer
    private val _fullContactListWithInternationalNumbers = MutableLiveData<List<ContactItemWithInternationalNumbers>>()
    val  fullContactListWithInternationalNumbers: LiveData<List<ContactItemWithInternationalNumbers>>
        get() = _fullContactListWithInternationalNumbers


    //phonebook
    private val _phoneBook = MutableLiveData<List<PhoneBookItem>>()
    val phoneBook: LiveData<List<PhoneBookItem>>
        get() = _phoneBook

    val phoneBookExported=myRepository.initialexportPhoneBookNetworkSuccess

    init {
        Log.i(MY_TAG,("init"))

    }


    fun showSetAccountDisclaimer(){
        viewModelScope.launch {

            val user=myRepository.getUser()
            Log.i(MY_TAG,"user za show acccount dialog je $user")
            if(user.userEmail.equals(EMPTY_EMAIL) && !checkForSharedPrefDisclamerShownValue()){
                Log.i(MY_TAG,"user ima empty email i nije pokazan disclaimer")
                _shouldShowSetAccountDisclaimer.value=true}
        }

    }

    fun setAccountDialogDiscalimerShown(){
        _shouldShowSetAccountDisclaimer.value=false
    }


    fun getPhoneBook(){
        Log.i(MY_TAG,"get phone boook")

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

        val myUser=userData.value
        if(myUser!=null
                && myUser.userPhone!= EMPTY_PHONE_NUMBER && myUser.userPhone.isNotEmpty()
                && myUser.userToken!= EMPTY_TOKEN && myUser.userToken.isNotEmpty())
            {
            viewModelScope.launch {
                myRepository.exportPhoneBook(myUser.userToken,myUser.userPhone,phoneBook,initialExport = true)
            }
        }

    }

    fun phoneBookExportFinished(){
        myRepository.initialPhoneBookExportFinished()

    }

    private fun checkForSharedPrefDisclamerShownValue():Boolean{

        val sharedPreferences = getApplication<Application>().getSharedPreferences(MainActivity.MAIN_ACTIVITY_SHARED_PREF_NAME, Context.MODE_PRIVATE)
        var wasShown=false

        if(sharedPreferences.contains(MainActivity.DISCLAIMER_WAS_SHOWN)){
            wasShown=sharedPreferences.getBoolean(MainActivity.DISCLAIMER_WAS_SHOWN,false)
            Log.i(MY_TAG," usao u ima disclaimer promenljiva i vrednost je $wasShown")
        }else{
            sharedPreferences.edit().putBoolean(MainActivity.DISCLAIMER_WAS_SHOWN,false).apply()
        }

        return wasShown
    }



    /*private fun removeContactsWithNoPhones(list:List<ContactItemWithInternationalNumbers>):List<ContactItemWithInternationalNumbers>{
        var resultList= mutableListOf<ContactItemWithInternationalNumbers>()

        for(item in list){
            if(item.internationalNumbers.isNotEmpty())  resultList.add(item)
        }
        Log.i(MY_TAG,"removeContactsWithNoPhones ulazna lista je $list, posle izbacivanja result je $resultList")

        return resultList
    }*/


}