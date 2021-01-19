package app.adinfinitum.ello.ui.main_activity

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.data.RepoLogToServer
import app.adinfinitum.ello.data.RepoUser


class MainActivityViewModelFactory(
    val myRepository: RepoContacts,
    val myRepoUser: RepoUser,
    val myRepoLogToServer: RepoLogToServer,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(
                myRepository, myRepoUser, myRepoLogToServer, application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}