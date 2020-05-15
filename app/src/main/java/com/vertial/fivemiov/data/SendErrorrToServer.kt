package com.vertial.fivemiov.data

import android.util.Log
import com.vertial.fivemiov.api.MyAPIService
import com.vertial.fivemiov.api.NetRequest_SendErrorToServer

class SendErrorrToServer(val myAPIService: MyAPIService,val process:String, val errorMsg:String) {

   suspend fun sendError(){
        val def=myAPIService.sendErrorToServer(process = process,errorMsg= errorMsg)
        try {
            val defResponse=def.await()
        }catch (t:Throwable){
            Log.i("Send Error To Server","${t.message}")

        }
    }
}