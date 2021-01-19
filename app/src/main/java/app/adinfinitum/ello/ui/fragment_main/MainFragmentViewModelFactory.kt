package app.adinfinitum.ello.ui.fragment_main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.data.*

class MainFragmentViewModelFactory(
    val myrepoContacts: RepoContacts,
    val myRepoUser: RepoUser,
    val myRepoProvideContacts: RepoProvideContacts,
    val myRepoRemoteDataSource: RepoRemoteDataSource,
    val myRepoLogOut: RepoLogOut,
    val myRepoLogToServer: RepoLogToServer,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainFragmentViewModel::class.java)) {
            return MainFragmentViewModel(myrepoContacts,myRepoUser,myRepoProvideContacts,myRepoRemoteDataSource,myRepoLogOut,myRepoLogToServer,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}