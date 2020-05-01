package com.vertial.fivemiov.data

import android.util.Log
import com.vertial.fivemiov.database.MyDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


private const val MY_TAG="MY_REPO_HELPERS"
suspend fun logoutAll( myDatabaseDao: MyDatabaseDao){

    coroutineScope {
        Log.i(MY_TAG, "e1 tabele je ${myDatabaseDao.getAllE1()}, sip ${myDatabaseDao.getAllSip()}, webapi ${myDatabaseDao.getWebApiVersion()}")

        val deferreds = listOf(     // fetch two docs at the same time
            async(Dispatchers.IO) {
                myDatabaseDao.logoutE1Table()
            },  // async returns a result for the first doc
            async(Dispatchers.IO) {  myDatabaseDao.logoutSipAccount() },
            async(Dispatchers.IO) { myDatabaseDao.logoutWebApiVersion() }
        )

        try {
            val result=deferreds.awaitAll()
            Log.i(MY_TAG, "e1 tabele je ${myDatabaseDao.getAllE1()}, sip ${myDatabaseDao.getAllSip()}, webapi ${myDatabaseDao.getWebApiVersion()}")
            Log.i(MY_TAG, "logour tri tabele je $result")
            Log.i(MY_TAG,"pre user logouta ${myDatabaseDao.getUserNoLiveData()}")
            val defa=async (Dispatchers.IO) {  myDatabaseDao.logoutUser()}
            defa.await()
            Log.i(MY_TAG,"posle user logouta ${myDatabaseDao.getUserNoLiveData()}")

        }catch(t:Throwable){
            Log.i(MY_TAG, "greska prilikom logaouta 3 tabele je ${t.message}")
        }


    }

}