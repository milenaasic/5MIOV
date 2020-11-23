package app.adinfinitum.ello.ui.fragment_about

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.database.MyDatabaseDao
import app.adinfinitum.ello.ui.myapplication.MyApplication
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlin.Exception


private const val MYTAG="MY_AboutFragmViewModel"

class AboutFragmentViewModel (val repoContacts: RepoContacts, application: Application) : AndroidViewModel(application) {


    private val _webApiVersion= MutableLiveData<String>()
    val webApiVersion: LiveData<String>
        get() = _webApiVersion

    init {
        getWebApiVersion()
        //startCorTest()
    }
    //todo delete this test function
    /*private fun startCorTest() {

            val j=viewModelScope.launch {
                try {
                        Log.i(MYTAG, " viewModelScope.launch ")
                        //delay(5000)
                        //Log.i(MYTAG, " viewModelScope.launch after 5 sec")

                    GlobalScope.launch {
                            async (Dispatchers.IO){
                                Log.i(MYTAG, " GlobalScope.launch inside viewModelScope")
                                delay(5000)
                                Log.i(MYTAG, " GlobalScope.launch inside viewModelScope after 5 sec")
                            }
                    }

                } catch (e: Exception) {
                    Log.i(MYTAG, " viewModelScope error withContext ${e.message}")
                }
            }


                GlobalScope.launch {
                    try {
                        withContext(Dispatchers.IO){
                            Log.i(MYTAG, " GlobalScope.launch ")
                            delay(5000)
                            Log.i(MYTAG, " GlobalScope.launch. after 5 sec")
                        }
                    }catch (e:Exception){
                        Log.i(MYTAG, " GlobalScope.withContext ${e.message}")
                    }

                }


            try {
                getApplication<MyApplication>().applicationScope.launch {
                    Log.i(MYTAG, " applicationScope ")
                    delay(5000)
                    Log.i(MYTAG, " applicationScope after 5 sec")
                }
            }catch (e:Exception){
                Log.i(MYTAG, " GlobalScope.launch error ${e.message}")
            }


    }*/


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

    override fun onCleared() {
        super.onCleared()
        Log.i(MYTAG, " onCleared()")
    }
}