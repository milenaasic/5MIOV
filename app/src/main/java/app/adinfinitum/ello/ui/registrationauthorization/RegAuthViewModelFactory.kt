package app.adinfinitum.ello.ui.registrationauthorization

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.data.*

class RegAuthViewModelFactory(
    val repository: Repo,
    val repoUser:RepoUser,
    val repoPrenumberAndWebApiVer: RepoPrenumberAndWebApiVer,
    val repoRemoteDataSource: RepoRemoteDataSource,
    val repoLogToServer: RepoLogToServer,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegAuthActivityViewModel::class.java)) {
            return RegAuthActivityViewModel(repository,repoUser,repoPrenumberAndWebApiVer,repoRemoteDataSource,repoLogToServer, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}