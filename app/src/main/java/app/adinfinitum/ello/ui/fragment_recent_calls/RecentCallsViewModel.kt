package app.adinfinitum.ello.ui.fragment_recent_calls

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.data.RepoRecentCalls

private val MYTAG="MY_RecentCallsViewModel"
class RecentCallsViewModel(val myRepoRecentCalls: RepoRecentCalls, application: Application) : AndroidViewModel(application) {

    val recentCalls=myRepoRecentCalls.getAllRecentCalls()


}
