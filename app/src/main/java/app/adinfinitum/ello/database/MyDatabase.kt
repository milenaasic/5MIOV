package app.adinfinitum.ello.database


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.adinfinitum.ello.model.*
import app.adinfinitum.ello.utils.*


private const val NAME="MY_DATABASE"

@Database(entities = [User::class, E1Prenumber::class,SipAccount::class,WebApiVersion::class,UsersSipCallerIDs::class,RecentCall::class,E1andCallVerificationEnabledCountries::class],
                        version = 1,exportSchema = true)
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
                                Log.i(NAME,"DB On create")

                                db.execSQL("CREATE TRIGGER delete_2_callbacks BEFORE INSERT ON recent_calls_table WHEN (select count(*) from recent_calls_table)>2100" +
                                                " BEGIN  DELETE FROM recent_calls_table WHERE id IN  (SELECT id FROM recent_calls_table ORDER BY id limit (select count(*) -101 from recent_calls_table)); END ")

                               val myvalues=ContentValues().apply {
                                    put("user_phone", EMPTY_PHONE_NUMBER)
                                    put("token", EMPTY_TOKEN)
                                    put("email", EMPTY_EMAIL)
                                    put("password", EMPTY_PASSWORD)
                                }
                                val myValues2=ContentValues().apply {
                                    put("e1prenumber", EMPTY_E1_PRENUMBER)
                                    put("timestamp",0L)

                                 }

                                val myValues3=ContentValues().apply {
                                    put("mainSipCallerId", EMPTY_MAIN_SIP_CALLER_ID)
                                    put("sipUsername", EMPTY_SIP_USERNAME)
                                    put("sipPassword", EMPTY_SIP_PASSWORD)
                                    put("sipServer", EMPTY_SIP_SERVER)
                                 }

                                 val myValues4:ContentValues=ContentValues().apply {
                                     put("webApiVersion", "0.0")

                                 }

                                 val myValues5:ContentValues=ContentValues().apply {
                                     put("e1_enabled_countries", "")
                                     put("call_verification_enabled_countries", "")
                                  }

                                try{
                                    db.insert("user_table",CONFLICT_IGNORE,myvalues)
                                    db.insert("e1_prenumber_table",CONFLICT_IGNORE,myValues2)
                                    db.insert("sip_account_table", CONFLICT_IGNORE,myValues3)
                                    db.insert("webapi_version_table", CONFLICT_IGNORE,myValues4)
                                    db.insert("e1_and_call_verif_enabled_countries_table", CONFLICT_IGNORE,myValues5)
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