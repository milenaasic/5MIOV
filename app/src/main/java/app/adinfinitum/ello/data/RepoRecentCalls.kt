package app.adinfinitum.ello.data

import app.adinfinitum.ello.database.MyDatabaseRecentCalls
import app.adinfinitum.ello.model.RecentCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepoRecentCalls(  val myDatabaseRecentCalls: MyDatabaseRecentCalls,
                        val dispatcher: CoroutineDispatcher = Dispatchers.IO)
    {
        //LiveData
        fun getAllRecentCalls()=myDatabaseRecentCalls.getAllRecentCalls()

        suspend fun insertRecentCall(call: RecentCall) {
            withContext(dispatcher) {
                myDatabaseRecentCalls.insertRecentCall(call)
            }
        }


    }





