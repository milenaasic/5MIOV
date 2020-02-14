package com.vertial.sipdnidphone.ui.RegistrationAuthorization

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vertial.sipdnidphone.data.Repo
import com.vertial.sipdnidphone.ui.MainActivityViewModel

class RegAuthViewModelFactory(
    val repository: Repo,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegAuthActivityViewModel::class.java)) {
            return RegAuthActivityViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}