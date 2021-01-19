package app.adinfinitum.ello.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import app.adinfinitum.ello.database.*
import app.adinfinitum.ello.utils.DEFAULT_SHARED_PREFERENCES
import app.adinfinitum.ello.utils.DISCLAIMER_WAS_SHOWN
import app.adinfinitum.ello.utils.PHONEBOOK_IS_EXPORTED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private const val MY_TAG="MY_RepoLogOut"

interface IRepoLogOut {
    suspend fun logoutAll()
}

class RepoLogOut (private val applicationContext: Context
                ) : IRepoLogOut {

    override suspend fun logoutAll(){
        val myDatabase= MyDatabase.getInstance(applicationContext)
        val myDatabaseUser=myDatabase.myDatabaseUser
        val myDatabasePrenumberAndWebApiVersion=myDatabase.myDatabasePrenumberAndWebApiVersion
        val myDatabaseRecentCalls=myDatabase.myDatabaseRecentCalls
        val myDatabaseSIPCredentials=myDatabase.myDatabaseSIPCredentials

        val sharedPreferences=applicationContext.getSharedPreferences(DEFAULT_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        sharedPreferences.edit().apply{
            putBoolean(DISCLAIMER_WAS_SHOWN,false).apply()
            putBoolean(PHONEBOOK_IS_EXPORTED,false).apply()
        }


        try {
            withContext(Dispatchers.IO) {
                myDatabasePrenumberAndWebApiVersion.logoutE1Table()
                myDatabasePrenumberAndWebApiVersion.logoutWebApiVersion()
                myDatabaseSIPCredentials.logoutSipAccount()
                myDatabaseRecentCalls.logoutRecentCalls()
                myDatabaseUser.logoutUser()
            }
        }catch (e:Exception){
            Log.i(MY_TAG, "Error logging out ${e.message}")
        }

    }


}