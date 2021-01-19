package app.adinfinitum.ello.ui.fragment_detail_contact

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.data.RepoLogToServer
import app.adinfinitum.ello.data.RepoProvideContacts
import app.adinfinitum.ello.data.RepoRecentCalls


class DetailContactViewModelFactory(
    val key:String,
    val repository: RepoContacts,
    val myRepoProvideContacts: RepoProvideContacts,
    val myRepoRecentCalls: RepoRecentCalls,
    val myRepoLogToServer: RepoLogToServer,
    val application: Application
    ): ViewModelProvider.Factory{

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailContactViewModel::class.java)) {
                return DetailContactViewModel(key,repository,myRepoProvideContacts,myRepoRecentCalls, myRepoLogToServer, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}