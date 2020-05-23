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

            var phoneBookContactsList= listOf<ContactItem>()
            val phoneBookList= mutableListOf<PhoneBookItem>()

            val deferredList=viewModelScope.async(IO){
             myRepository.getAllContacts(ContactsContract.Contacts.CONTENT_URI)
            }
            try {

                val resultListWithEmptyContact=deferredList.await()
                Log.i(MY_TAG,"get phone book lista $resultListWithEmptyContact")
                phoneBookContactsList=resultListWithEmptyContact


            }catch (e:Exception){
                Log.i(MY_TAG,e.message?:"no message")
            }

              if (!phoneBookContactsList.isNullOrEmpty()) {
                Log.i(MY_TAG," lista nije prazna ni nije null ali je $phoneBookContactsList")
                //izbaci poslednji prazan kontakt
                  val resultList= phoneBookContactsList
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
                          Log.i(
                              MY_TAG,
                              "get phone book resul svih deferred je ${resultSuccessList}"
                          )
                          if (!phoneBookList.isNullOrEmpty()) _phoneBook.value = phoneBookList

                      } catch (t: Throwable) {
                          Log.i(MY_TAG, t.message ?: "no message")
                      }
                  }

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

    fun getContactsWithInternationalNumbers(uri: Uri) {

        val contactListWithInternationalNumbers =
            mutableListOf<ContactItemWithInternationalNumbers>()

        viewModelScope.launch {
            var allContacts = listOf<ContactItem>()
            val defAllContacts = async(IO) {
                myRepository.getAllContacts(uri)
            }
            try {
                allContacts = defAllContacts.await()
            } catch (t: Throwable) {
                Log.i(MY_TAG, " all Contacts error ${t.message}")
            }

            if (allContacts.isNotEmpty()) {

                val defferedPhones = (allContacts.indices).map {
                    viewModelScope.async(IO) {
                        val internationalPhonesList =
                            myRepository.getInternationalPhoneNumbersForContact(allContacts[it].lookUpKey)

                        /*contactListWithInternationalNumbers.add(
                            ContactItemWithInternationalNumbers(
                                lookUpKey = allContacts[it].lookUpKey,
                                name = allContacts[it].name,
                                photoThumbUri = allContacts[it].photoThumbUri,
                                internationalNumbers = internationalPhonesList
                            )
                        )*/
                    }
                }

                try {
                    val resultSuccessList = defferedPhones.map { it.await() }
                    Log.i(MY_TAG, "lista svih kontakata sa internacionalnim brojevima ${resultSuccessList}")
                    Log.i(MY_TAG,"lista internacionalnih brojeva je $contactListWithInternationalNumbers")
                    if (!contactListWithInternationalNumbers.isNullOrEmpty()) {

                        val listOfContactsWithPhones = removeContactsWithNoPhones(contactListWithInternationalNumbers)
                        Log.i(MY_TAG,"posle izbacivanja praznih brojeva je $listOfContactsWithPhones")
                        //sortiranje liste
                        Collections.sort(listOfContactsWithPhones, Comparator { t, t2 -> t.name.toLowerCase().compareTo(t2.name.toLowerCase()) })
                        _fullContactListWithInternationalNumbers.value= listOfContactsWithPhones

                    }

                } catch (t: Throwable) {
                    Log.i(MY_TAG, t.message ?: "no message")
                }

            }else {
                _fullContactListWithInternationalNumbers.value= emptyList()

            }

        }
    }

    private fun removeContactsWithNoPhones(list:List<ContactItemWithInternationalNumbers>):List<ContactItemWithInternationalNumbers>{
        var resultList= mutableListOf<ContactItemWithInternationalNumbers>()

        for(item in list){
            if(item.internationalNumbers.isNotEmpty())  resultList.add(item)
        }
        Log.i(MY_TAG,"removeContactsWithNoPhones ulazna lista je $list, posle izbacivanja result je $resultList")

        return resultList
    }


}