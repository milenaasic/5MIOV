package app.adinfinitum.ello.ui.webView

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.data.*

class WebViewViewModelFactory(
    val repository: RepoContacts,
    val myRepoUser: RepoUser,
    val myRepoProvideContacts: RepoProvideContacts,
    val myRepoRemoteDataSource: RepoRemoteDataSource,
    val myRepoLogOut: RepoLogOut,
    val myRepoLogToServer: RepoLogToServer,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WebViewViewModel::class.java)) {
            return WebViewViewModel(repository, myRepoUser, myRepoProvideContacts, myRepoRemoteDataSource, myRepoLogOut, myRepoLogToServer, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}