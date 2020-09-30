package com.adinfinitum.hello.ui.webView

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adinfinitum.hello.data.RepoContacts

class WebViewViewModelFactory(
    val repository: RepoContacts,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WebViewViewModel::class.java)) {
            return WebViewViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}