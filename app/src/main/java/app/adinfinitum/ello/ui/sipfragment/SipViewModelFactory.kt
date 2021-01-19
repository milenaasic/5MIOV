package app.adinfinitum.ello.ui.sipfragment


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.data.RepoLogToServer
import app.adinfinitum.ello.data.RepoRemoteDataSource
import app.adinfinitum.ello.data.RepoSIPE1
import app.adinfinitum.ello.data.RepoUser

class SipViewModelFactory(
    val mySipRepo: RepoSIPE1,
    val myRepoUser: RepoUser,
    val myRepoRemoteDataSource: RepoRemoteDataSource,
    val myRepoLogToServer: RepoLogToServer,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SipViewModel::class.java)) {
            return SipViewModel(mySipRepo, myRepoUser, myRepoRemoteDataSource, myRepoLogToServer, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}