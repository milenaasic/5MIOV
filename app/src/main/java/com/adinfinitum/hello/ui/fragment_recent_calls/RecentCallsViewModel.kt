package com.adinfinitum.hello.ui.fragment_recent_calls

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.adinfinitum.hello.data.RepoContacts

private val MYTAG="MY_RecentCallsViewModel"
class RecentCallsViewModel(val myRepository: RepoContacts, application: Application) : AndroidViewModel(application) {

    val recentCalls=myRepository.getAllRecentCalls()


}
