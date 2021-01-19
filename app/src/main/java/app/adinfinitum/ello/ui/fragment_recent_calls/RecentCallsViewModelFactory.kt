package app.adinfinitum.ello.ui.fragment_recent_calls

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.data.RepoRecentCalls

class RecentCallsViewModelFactory (
    val repository: RepoRecentCalls,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecentCallsViewModel::class.java)) {
            return RecentCallsViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}