package com.vertial.sipdnidphone.database


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vertial.sipdnidphone.model.Prenumber
import com.vertial.sipdnidphone.model.User
import com.vertial.sipdnidphone.utils.EMPTY_PHONE_NUMBER
import com.vertial.sipdnidphone.utils.EMPTY_TOKEN
import com.vertial.sipdnidphone.utils.EMPTY_USERNAME


private const val NAME="MY_Database"

@Database(entities = [User::class,Prenumber::class],version = 1 )
abstract class MyDatabase:RoomDatabase(){

    abstract val myDatabaseDao:MyDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getInstance(context: Context): MyDatabase {
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

                                val myvalues=ContentValues().apply {
                                    put("user_name", EMPTY_USERNAME)
                                    put("user_phone", EMPTY_PHONE_NUMBER)
                                    put("token", EMPTY_TOKEN)
                                }
                                val myValues2=ContentValues().apply {
                                    put("prenumber","+38111777111")

                                 }


                                try{
                                    db.insert("user_table",CONFLICT_IGNORE,myvalues)
                                    db.insert("prenumber_table",CONFLICT_IGNORE,myValues2)
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