package app.adinfinitum.ello.data

import androidx.lifecycle.LiveData
import app.adinfinitum.ello.database.MyDatabaseRecentCalls
import app.adinfinitum.ello.model.RecentCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface IRepoRecentCalls {

    fun getAllRecentCalls(): LiveData<List<RecentCall>>
    suspend fun insertRecentCall(call: RecentCall)
}

class RepoRecentCalls(val myDatabaseRecentCalls: MyDatabaseRecentCalls,
                      val dispatcher: CoroutineDispatcher = Dispatchers.IO) : IRepoRecentCalls {
        //LiveData
        override fun getAllRecentCalls()=myDatabaseRecentCalls.getAllRecentCalls()

        override suspend fun insertRecentCall(call: RecentCall) {
            withContext(dispatcher) {
                myDatabaseRecentCalls.insertRecentCall(call)
            }
        }


    }





