package app.adinfinitum.ello.ui.fragment_set_email_and_password

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.api.NetResponse_Registration
import app.adinfinitum.ello.api.NetResponse_SetAccountEmailAndPass
import app.adinfinitum.ello.data.Repo
import app.adinfinitum.ello.data.Result
import app.adinfinitum.ello.utils.EMPTY_PHONE_NUMBER
import app.adinfinitum.ello.utils.EMPTY_TOKEN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.Exception

private val MYTAG="MY_SetEmailPass_VIEWMOD"

class SetEmailPassFragmentViewModel(val myrepository: Repo, application: Application) : AndroidViewModel(application) {


    private val _setAccountEmailAndPassSuccess= MutableLiveData<NetResponse_SetAccountEmailAndPass?>()
    val setAccountEmailAndPassSuccess: LiveData<NetResponse_SetAccountEmailAndPass?>
        get() = _setAccountEmailAndPassSuccess

    private val _setAccountEmailAndPassError= MutableLiveData<String?>()
    val setAccountEmailAndPassError: LiveData<String?>
        get() = _setAccountEmailAndPassError

    fun setAccountAndEmailForUser(email:String,password:String){

            viewModelScope.launch {
                try {
                    val myUser = withContext(IO) {
                        myrepository.getUser()
                    }

                    Log.i(MYTAG, " user from DB: ${myUser.userPhone},$myUser.authtoken,$email,$password")
                    if( myUser.userPhone.isNotEmpty() && myUser.userPhone!= EMPTY_PHONE_NUMBER
                        && myUser.userToken.isNotEmpty() && myUser.userToken!= EMPTY_TOKEN) {

                        try {

                            val result = withContext(Dispatchers.IO) {
                                myrepository.setAccountEmailAndPasswordForUser(
                                    phoneNumber = myUser.userPhone,
                                    token = myUser.userToken,
                                    email = email,
                                    password = password
                                )

                            }

                            when(result){
                                is Result.Success->{
                                   try {
                                       withContext(Dispatchers.IO) {
                                           if (result.data.email.isNotEmpty() && result.data.email.isNotBlank()) myrepository.updateUserEmail(
                                               result.data.email
                                           )
                                           if (!result.data.appVersion.isNullOrEmpty() || !result.data.appVersion.isNullOrBlank()) myrepository.updateWebApiVersion(
                                               result.data.appVersion
                                           )
                                       }
                                   }catch (e:Exception){
                                        Log.e(MYTAG,"insertion into DB failed")
                                   }

                                    _setAccountEmailAndPassSuccess.value=result.data
                                }
                                is Result.Error->_setAccountEmailAndPassError.value=result.exception.message
                            }

                        }catch (e:Exception){
                            Log.i(MYTAG,"set email and account for user ${e.message}")
                        }

                    }

                } catch (e: Exception) {
                    Log.i(MYTAG,"DB error ${e.message}")
                }



            }

    }

    fun resetSetEmailAndPassNetSuccess(){
        _setAccountEmailAndPassSuccess.value=null
    }

    fun resetSetEmailAndPasstNetErrorr(){
       _setAccountEmailAndPassError.value=null
    }



}
