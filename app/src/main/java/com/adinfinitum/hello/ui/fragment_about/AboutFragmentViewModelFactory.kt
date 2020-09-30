package com.adinfinitum.hello.ui.fragment_about

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adinfinitum.hello.database.MyDatabaseDao

class AboutFragmentViewModelFactory(
    val myDatabase: MyDatabaseDao,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AboutFragmentViewModel::class.java)) {
            return AboutFragmentViewModel(myDatabase, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}