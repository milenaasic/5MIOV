package com.vertial.fivemiov.ui.fragment_detail_contact

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.model.PhoneItem
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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

    private fun getContactPhoneNumbers() {
        viewModelScope.launch {
            val deferredList=async(IO) {
                myRepository.getPhoneNumbersForContact(contactLookUp)
            }
           try {
               val resultPhoneList=deferredList.await()
               _phoneList.value=resultPhoneList
           }catch (e:Exception){
                Log.i(MYTAG,e.message?:"no message")
           }
        }
    }



}