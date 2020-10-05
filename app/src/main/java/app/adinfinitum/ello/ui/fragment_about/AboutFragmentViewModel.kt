package app.adinfinitum.ello.ui.fragment_about

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.database.MyDatabaseDao
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception



private const val MYTAG="MY_AboutFragmViewModel"

class AboutFragmentViewModel (val myDatabaseDao: MyDatabaseDao, application: Application) : AndroidViewModel(application) {


    private val _webApiVersion= MutableLiveData<String>()
    val webApiVersion: LiveData<String>
        get() = _webApiVersion

    init {
        getWebApiVersion()
    }

     private fun getWebApiVersion() {

         viewModelScope.launch {
             val webapiDef = viewModelScope.async (IO){
                 myDatabaseDao.getWebApiVersion()
             }
             try {
                val webapi = webapiDef.await()
                _webApiVersion.value=webapi

             } catch (e: Exception) {
                 Log.i(MYTAG, " DB error ${e.message}")
             }
         }

     }


}