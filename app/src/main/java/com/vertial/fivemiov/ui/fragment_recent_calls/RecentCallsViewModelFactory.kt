package com.vertial.fivemiov.ui.fragment_recent_calls

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vertial.fivemiov.data.RepoContacts

class RecentCallsViewModelFactory (
    val repository: RepoContacts,
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