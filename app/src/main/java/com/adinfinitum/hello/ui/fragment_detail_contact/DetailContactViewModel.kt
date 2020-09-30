package com.adinfinitum.hello.ui.fragment_detail_contact

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adinfinitum.hello.data.RepoContacts
import com.adinfinitum.hello.model.PhoneItem
import com.adinfinitum.hello.model.RecentCall
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.lang.Exception

private val MYTAG="MY_DetailContViewModel"
class DetailContactViewModel(val contactLookUp:String,val myRepository: RepoContacts, application: Application) : AndroidViewModel(application) {


    private val _phoneList = MutableLiveData<List<PhoneItem>>()
    val phoneList: LiveData<List<PhoneItem>>
        get() = _phoneList

    val prefixNumber=myRepository.getPremunber()

    init {

        getContactPhoneNumbers()


    }

    fun getContactPhoneNumbers() {


        viewModelScope.launch {

                try {
                    val defList = viewModelScope.async(IO) {
                        myRepository.getInternationalPhoneNumbersForContact(contactLookUp)
                    }
                    val list=defList.await()
                    _phoneList.value=list

                }catch (e: Exception) {
                    Log.i(MYTAG, e.message ?: "no message")
                }

        }
    }

    fun insertCallIntoDB(call: RecentCall){
        GlobalScope.launch {
            withContext(Dispatchers.IO){
                myRepository.insertRecentCall(call)
            }
        }


    }

    fun logStateToMyServer(process:String,state:String){
        myRepository.logStateToServer(process = process,state = state)
    }



}