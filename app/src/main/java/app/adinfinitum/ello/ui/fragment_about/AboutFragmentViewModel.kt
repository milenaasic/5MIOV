package app.adinfinitum.ello.ui.fragment_about

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.database.MyDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception



private const val MYTAG="MY_AboutFragmViewModel"

class AboutFragmentViewModel (val repoContacts: RepoContacts, application: Application) : AndroidViewModel(application) {


    private val _webApiVersion= MutableLiveData<String>()
    val webApiVersion: LiveData<String>
        get() = _webApiVersion

    init {
        getWebApiVersion()
    }


     private fun getWebApiVersion(){
            viewModelScope.launch {
                try {
                        val webApiVer=withContext(Dispatchers.IO){
                                                        repoContacts.getWebAppVersion()
                                                }
                        _webApiVersion.value=webApiVer

                } catch (e:Exception){
                    Log.i(MYTAG, " DB error ${e.message}")
                }

             }
     }


}