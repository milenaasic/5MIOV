package com.vertial.fivemiov.data

import android.util.Log
import com.vertial.fivemiov.api.MyAPIService
import com.vertial.fivemiov.api.NetRequest_ResetSipAccess
import com.vertial.fivemiov.api.NetRequest_SetE1Prenumber
import com.vertial.fivemiov.database.MyDatabaseDao


private const val MYTAG="MY_Sip_and_e1_repo"

class RepoSIPE1 (val myDatabaseDao: MyDatabaseDao, val myAPI: MyAPIService){


    suspend fun resetSipAccess(token:String){

        val defResponse=myAPI.resetSipAccess(request = NetRequest_ResetSipAccess(authToken = token))
        try {
            val response=defResponse.await()
        }catch (e:Throwable){
            Log.i(MYTAG," ruta resetSipAccess, greska ${e.message}")
        }

    }

    suspend fun setNewE1(token: String){

        val defResponse=myAPI.setNewE1(request = NetRequest_SetE1Prenumber(authToken = token))
        try {
            val response=defResponse.await()
        }catch (e:Throwable){
            Log.i(MYTAG," ruta setNewE1, greska ${e.message}")

        }

    }



}