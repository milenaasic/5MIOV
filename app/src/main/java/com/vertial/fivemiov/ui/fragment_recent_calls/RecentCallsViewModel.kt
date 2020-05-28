package com.vertial.fivemiov.ui.fragment_recent_calls

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.model.RecentCall
import kotlinx.coroutines.launch

private val MYTAG="MY_RecentCallsViewModel"
class RecentCallsViewModel(val myRepository: RepoContacts, application: Application) : AndroidViewModel(application) {

    val recentCalls=myRepository.getAllRecentCalls()


}
