package com.vertial.fivemiov.data

import android.util.Log
import com.vertial.fivemiov.api.MyAPIService
import com.vertial.fivemiov.api.NetRequest_SendErrorToServer

class SendErrorrToServer(val myAPIService: MyAPIService,val process:String, val errorMsg:String) {

   suspend fun sendError(){
       //myAPIService.sendErrorToServer(process = process,errorMsg= errorMsg)
        val def=myAPIService.sendErrorToServer(process = process,errorMsg= errorMsg)
        try {
            val defResponse=def.await()
            Log.i("MY_Send Error To Server","${defResponse.code()},${defResponse.body()}")
        }catch (t:Throwable){
            Log.i("MY_Send Error To Server","${t.message}")

        }
    }
}