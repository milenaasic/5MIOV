package com.adinfinitum.hello.ui.registrationauthorization

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adinfinitum.hello.data.Repo
import com.adinfinitum.hello.data.RepoSIPE1

class RegAuthViewModelFactory(
    val repository: Repo,
    val sipE1Repo: RepoSIPE1,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegAuthActivityViewModel::class.java)) {
            return RegAuthActivityViewModel(repository, sipE1Repo,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}