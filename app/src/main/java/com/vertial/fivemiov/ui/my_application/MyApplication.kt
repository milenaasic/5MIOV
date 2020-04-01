package com.vertial.fivemiov.ui.my_application

import android.app.Application
import android.content.ContentResolver
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.database.MyDatabaseDao
import retrofit2.Retrofit
import java.security.AccessControlContext

class MyApplication: Application() {
    val myAppContainer=AppContainer(this)
}

interface Provider{
    fun provideDetailContactViewModelFactory()

}

class AppContainer(val context: Application):Provider{

    val myDBDao=MyDatabase.getInstance(context).myDatabaseDao
    val myApi=MyAPI.retrofitService

    val contactsRepoContacts:RepoContacts= RepoContacts(context.contentResolver,myDBDao,myApi)


    override fun provideDetailContactViewModelFactory() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
