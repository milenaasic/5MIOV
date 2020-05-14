package com.vertial.fivemiov.ui.fragment_dial_pad

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoContacts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val MYTAG="MY_DialPadVIewMOdel"
class DialpadFragmViewModel(val myRepository: RepoContacts, application: Application) : AndroidViewModel(application) {


    val myPrenumber=myRepository.getPremunber()

    //live data from database
    val userData=myRepository.getUserData()


    val getCreditNetSuccess=myRepository.getCredit_NetSuccess
    val getCreditNetworkError=myRepository.getCredit_NetError


    fun getCredit() {
        Log.i(MYTAG, "get Credit ")
        //pokupi phone i token iz baze
        var myToken = ""
        var myPhoneNumber = ""
        viewModelScope.launch {
            val defUser = async(Dispatchers.IO) {
                myRepository.getUser()
            }
            try {
                val user = defUser.await()
                myToken = user.userToken
                myPhoneNumber = user.userPhone
            } catch (t: Throwable) {
                Log.i(MYTAG, "nije pokupio usera iz baze ${t.message} ")
            }

            Log.i(MYTAG, "my TOken u get credit je $myToken ")
            if (myToken.isNotEmpty() && myPhoneNumber.isNotEmpty()) {
                Log.i(MYTAG, "my repo get Credit ")
                viewModelScope.launch {
                        myRepository.getCredit(phone = myPhoneNumber, token = myToken)

                }


            }

        }
    }

    fun resetGetCreditNetSuccess(){
        myRepository.resetGetCreditNetSuccess()
    }

    fun resetGetCreditNetErrorr(){
        myRepository.resetGetCreditNetError()
    }

}