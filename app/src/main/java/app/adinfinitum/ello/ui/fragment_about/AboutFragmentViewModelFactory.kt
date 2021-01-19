package app.adinfinitum.ello.ui.fragment_about

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.data.RepoPrenumberAndWebApiVer
import app.adinfinitum.ello.database.MyDatabaseDao

class AboutFragmentViewModelFactory(
    val repoPrenumberAndWebApiVer: RepoPrenumberAndWebApiVer,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AboutFragmentViewModel::class.java)) {
            return AboutFragmentViewModel(repoPrenumberAndWebApiVer, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}