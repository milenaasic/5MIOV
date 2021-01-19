package app.adinfinitum.ello.data

import app.adinfinitum.ello.database.MyDatabaseUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class RepoUser( val myDatabaseUser: MyDatabaseUser,
                val dispatcher: CoroutineDispatcher =Dispatchers.IO
                ){

    //val userLiveData=myDatabaseUser.getUser()
    //User Live Data
    fun getUserData()=myDatabaseUser.getUser()

    suspend fun getUser()= withContext(dispatcher){
        myDatabaseUser.getUserNoLiveData()
    }

    suspend fun updateUsersPhoneTokenEmail(phone: String, token: String, email: String){
        val res=withContext(dispatcher){
            myDatabaseUser.updateUsersPhoneTokenEmail(phoneNb = phone,token = token,email = email)
        }
    }

    suspend fun updateUsersPhoneAndToken (phone:String, token:String) {
        withContext(dispatcher){
            myDatabaseUser.updateUsersPhoneAndToken(phoneNb = phone,token =  token)
        }
    }

    suspend fun updateUserEmail (email: String){
        withContext(dispatcher){
            myDatabaseUser.updateUserEmail(email = email)
        }
    }

}