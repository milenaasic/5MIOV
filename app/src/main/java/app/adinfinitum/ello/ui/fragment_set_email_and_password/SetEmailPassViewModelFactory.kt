package app.adinfinitum.ello.ui.fragment_set_email_and_password

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.data.*

class SetEmailPassViewModelFactory(
    val myrepository: Repo,
    val myRepoUser: RepoUser,
    val myRepoPrenumberAndWebApiVer: RepoPrenumberAndWebApiVer,
    val myRepoRemoteDataSource: RepoRemoteDataSource,
    val myRepoLogToServer: RepoLogToServer,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SetEmailPassFragmentViewModel::class.java)) {
            return SetEmailPassFragmentViewModel(myrepository, myRepoUser, myRepoPrenumberAndWebApiVer, myRepoRemoteDataSource, myRepoLogToServer, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}