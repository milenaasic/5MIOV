package app.adinfinitum.ello.ui.sipfragment


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.data.RepoSIPE1

class SipViewModelFactory(
    val mySipRepo:RepoSIPE1,
    val application: Application
): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SipViewModel::class.java)) {
            return SipViewModel(mySipRepo, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}