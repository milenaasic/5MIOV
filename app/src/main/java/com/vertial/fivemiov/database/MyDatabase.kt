package com.vertial.fivemiov.database


import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.util.Log
import android.widget.Toast
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vertial.fivemiov.model.E1Prenumber
import com.vertial.fivemiov.model.SipAccount
import com.vertial.fivemiov.model.User
import com.vertial.fivemiov.model.WebApiVersion
import com.vertial.fivemiov.utils.*


private const val NAME="MY_Database"

@Database(entities = [User::class, E1Prenumber::class,SipAccount::class,WebApiVersion::class],version = 2,exportSchema = false )
abstract class MyDatabase:RoomDatabase(){

    abstract val myDatabaseDao:MyDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getInstance(context: Context): MyDatabase {
        Log.i(NAME,"context sa kojimse pravi baza je $context")
            synchronized(this) {

                var instance = INSTANCE
                if (instance == null) {

                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MyDatabase::class.java,
                        NAME
                    ).fallbackToDestructiveMigration()
                        .addCallback (object:Callback(){
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                Log.i(NAME,"DB on create")
                                val myvalues=ContentValues().apply {
                                    put("user_phone", EMPTY_PHONE_NUMBER)
                                    put("token", EMPTY_TOKEN)
                                    put("email", EMPTY_EMAIL)
                                    put("password", EMPTY_PASSWORD)
                                }
                                val myValues2=ContentValues().apply {
                                    put("e1prenumber", EMPTY_E1_PRENUMBER)
                                    put("timestamp",System.currentTimeMillis())

                                 }

                                val myValues3=ContentValues().apply {
                                    put("sipCallerId", EMPTY_SIP_CALLER_ID)
                                    put("sipUsername", EMPTY_SIP_USERNAME)
                                    put("sipPassword", EMPTY_SIP_PASSWORD)
                                    put("sipServer", EMPTY_SIP_SERVER)
                                 }

                                 val myValues4:ContentValues=ContentValues().apply {
                                     put("webApiVersion", "0.0")

                                 }

                                try{
                                    db.insert("user_table",CONFLICT_IGNORE,myvalues)
                                    db.insert("e1_prenumber_table",CONFLICT_IGNORE,myValues2)
                                    db.insert("sip_account_table", CONFLICT_IGNORE,myValues3)
                                    db.insert("webapi_version_table", CONFLICT_IGNORE,myValues4)
                                }catch (e:Exception){
                                    Log.w("MY_database error",e)}

                            }
                        })
                        .build()
                    INSTANCE = instance
                }

                return instance

            }
        }
    }
}