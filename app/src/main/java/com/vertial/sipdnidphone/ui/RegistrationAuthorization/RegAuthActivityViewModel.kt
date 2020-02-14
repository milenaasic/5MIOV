package com.vertial.sipdnidphone.ui.RegistrationAuthorization

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.vertial.sipdnidphone.data.Repo
import kotlinx.coroutines.launch

private const val MY_TAG="MY_RegAuthActivVieModel"

class RegAuthActivityViewModel(val myRepository: Repo, application: Application) : AndroidViewModel(application) {

    lateinit var enteredPhoneNumber:String

    val userData=myRepository.getUserData()

    private val _navigateToAuthFragment= MutableLiveData<Boolean>()
    val navigateToAuthFragment:LiveData<Boolean>
    get() = _navigateToAuthFragment


    val registrationNetworkError=myRepository.registrationNetworkError

    init {

        Log.i(MY_TAG,("init"))
    }

    fun registerButtonClicked(phoneNumber:String){
        enteredPhoneNumber=phoneNumber

        //send registrtion phone number to server and go to authorization fragment
        viewModelScope.launch {
            myRepository.sendRegistationToServer(phoneNumber)
         }

    }


    fun navigationToAuthFragmentFinished(){
        _navigateToAuthFragment.value=false
    }



}