package app.adinfinitum.ello.ui.fragment_dial_pad

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.data.*

class DialpadFragmentViewModelFactory(
    val myRepository: RepoContacts,
    val myRepoUser: RepoUser,
    val myRepoRecentCalls: RepoRecentCalls,
    val myRepoPrenumberAndWebApiVer: RepoPrenumberAndWebApiVer,
    val myRepoRemoteDataSource: RepoRemoteDataSource,
    val myRepoLogOut: RepoLogOut,
    val myRepoLogToServer: RepoLogToServer,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DialpadFragmViewModel::class.java)) {
            return DialpadFragmViewModel(myRepository, myRepoUser, myRepoRecentCalls, myRepoPrenumberAndWebApiVer,
                                        myRepoRemoteDataSource, myRepoLogOut, myRepoLogToServer, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}