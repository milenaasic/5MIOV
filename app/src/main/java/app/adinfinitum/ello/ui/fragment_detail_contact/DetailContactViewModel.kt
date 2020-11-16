package app.adinfinitum.ello.ui.fragment_detail_contact

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.model.PhoneItem
import app.adinfinitum.ello.model.RecentCall
import app.adinfinitum.ello.ui.myapplication.MyApplication
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
                    val list = withContext(IO) {
                        myRepository.getPhoneNumbersForContact(contactLookUp)
                    }
                    _phoneList.value=list

                }catch (e: Exception) {
                    Log.i(MYTAG, e.message ?: "no message")
                }
        }
    }

    fun insertCallIntoDB(call: RecentCall){
        getApplication<MyApplication>().applicationScope.launch {
            withContext(Dispatchers.IO){
                myRepository.insertRecentCall(call)
            }
        }

    }

    fun logStateOrErrorToMyServer(options:Map<String,String>){

        getApplication<MyApplication>().applicationScope.launch {
            withContext(Dispatchers.IO){
                myRepository.logStateOrErrorToOurServer(myoptions = options)
            }
        }

    }



}