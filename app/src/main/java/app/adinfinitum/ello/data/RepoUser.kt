package app.adinfinitum.ello.data

import androidx.lifecycle.LiveData
import app.adinfinitum.ello.database.MyDatabaseUser
import app.adinfinitum.ello.model.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


interface IRepoUser {

    fun getUserData(): LiveData<User>
    suspend fun getUser(): User
    suspend fun updateUsersPhoneTokenEmail(phone: String, token: String, email: String)
    suspend fun updateUsersPhoneAndToken (phone: String, token: String)
    suspend fun updateUserEmail (email: String)
}

class RepoUser(val myDatabaseUser: MyDatabaseUser,
               val dispatcher: CoroutineDispatcher =Dispatchers.IO
                ) : IRepoUser {

    //User Live Data
    override fun getUserData()=myDatabaseUser.getUser()

    override suspend fun getUser()= withContext(dispatcher){
        myDatabaseUser.getUserNoLiveData()
    }

    override suspend fun updateUsersPhoneTokenEmail(phone: String, token: String, email: String){
        val res=withContext(dispatcher){
            myDatabaseUser.updateUsersPhoneTokenEmail(phoneNb = phone,token = token,email = email)
        }
    }

    override suspend fun updateUsersPhoneAndToken (phone:String, token:String) {
        withContext(dispatcher){
            myDatabaseUser.updateUsersPhoneAndToken(phoneNb = phone,token =  token)
        }
    }

    override suspend fun updateUserEmail (email: String){
        withContext(dispatcher){
            myDatabaseUser.updateUserEmail(email = email)
        }
    }

}