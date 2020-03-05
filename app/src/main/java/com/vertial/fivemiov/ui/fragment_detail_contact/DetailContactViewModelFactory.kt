package com.vertial.fivemiov.ui.fragment_detail_contact

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vertial.fivemiov.data.Repo


class DetailContactViewModelFactory(
        val key:String,
        val repository: Repo,
        val application: Application
    ): ViewModelProvider.Factory{

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailContactViewModel::class.java)) {
                return DetailContactViewModel(key,repository, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}