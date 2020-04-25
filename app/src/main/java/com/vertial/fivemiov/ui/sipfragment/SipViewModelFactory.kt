package com.vertial.fivemiov.ui.sipfragment


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.data.RepoSIPE1
import com.vertial.fivemiov.database.MyDatabaseDao

class SipViewModelFactory(
    val mySipRepo:RepoSIPE1,
    val myRepo:Repo,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SipViewModel::class.java)) {
            return SipViewModel(mySipRepo,myRepo, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}