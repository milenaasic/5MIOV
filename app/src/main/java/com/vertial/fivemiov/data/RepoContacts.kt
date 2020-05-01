package com.vertial.fivemiov.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vertial.fivemiov.api.MyAPIService
import com.vertial.fivemiov.api.NetRequest_ExportPhonebook
import com.vertial.fivemiov.database.MyDatabaseDao
import com.vertial.fivemiov.model.PhoneBookItem
import com.vertial.fivemiov.model.PhoneItem
import com.vertial.fivemiov.model.ContactItem
import com.vertial.fivemiov.model.User
import com.vertial.fivemiov.utils.EMPTY_CONTACT_ITEM
import com.vertial.fivemiov.utils.EMPTY_EMAIL
import com.vertial.fivemiov.utils.EMPTY_PHONE_NUMBER
import com.vertial.fivemiov.utils.EMPTY_TOKEN
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

private const val MY_TAG="MY_ContactsRepository"
class RepoContacts (val contentResolver: ContentResolver,val myDatabaseDao: MyDatabaseDao, val myAPIService: MyAPIService) {

    //User Live Data
    fun getUserData() = myDatabaseDao.getUser()

    // Live Data
    fun getPremunber() = myDatabaseDao.getPrenumber()


    //initial export phonebook network response
    private val _initialexportPhoneBookNetworkSuccess = MutableLiveData<Boolean>()
    val initialexportPhoneBookNetworkSuccess: LiveData<Boolean>
        get() = _initialexportPhoneBookNetworkSuccess

    //initial export phonebook network response
    private val _exportPhoneBookWebViewNetworkSuccess = MutableLiveData<Boolean>()
    val exportPhoneBookWebViewNetworkSuccess: LiveData<Boolean>
        get() = _exportPhoneBookWebViewNetworkSuccess

    suspend fun logout(){
        coroutineScope {
            logoutAll(myDatabaseDao = myDatabaseDao)
         }

        /*coroutineScope {
            Log.i(MY_TAG, "e1 tabele je ${myDatabaseDao.getAllE1()}, sip ${myDatabaseDao.getAllSip()}, webapi ${myDatabaseDao.getWebApiVersion()}")

                   val deferreds = listOf(     // fetch two docs at the same time
                       async(IO) {
                        myDatabaseDao.logoutE1Table()
                        },  // async returns a result for the first doc
                       async(IO) {  myDatabaseDao.logoutSipAccount() },
                       async(IO) { myDatabaseDao.logoutWebApiVersion() }
                   )

               try {
                   val result=deferreds.awaitAll()
                   Log.i(MY_TAG, "e1 tabele je ${myDatabaseDao.getAllE1()}, sip ${myDatabaseDao.getAllSip()}, webapi ${myDatabaseDao.getWebApiVersion()}")
                   Log.i(MY_TAG, "logour tri tabele je $result")
                   Log.i(MY_TAG,"pre user logouta ${myDatabaseDao.getUserNoLiveData()}")
                   val defa=async (IO) {  myDatabaseDao.logoutUser()}
                   defa.await()
                   Log.i(MY_TAG,"posle user logouta ${myDatabaseDao.getUserNoLiveData()}")

               }catch(t:Throwable){
                Log.i(MY_TAG, "greska prilikom logaouta 3 tabele je ${t.message}")
               }


        }*/

    }





    suspend fun getUser()=withContext(Dispatchers.IO){
            myDatabaseDao.getUserNoLiveData()

    }


    fun getAllContacts(uri: Uri):List<ContactItem>{

        val CURSOR_ID = 0
        val CURSOR_LOOKUP_KEY = 1
        val CURSOR_NAME = 2
        val CURSOSR_PHOTO_THUMBNAIL_URI = 3
        val CURSOR_HAS_PHONE_NUMBER = 4

        val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )

        val SELECTION: String =
            ("((${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} NOTNULL) AND (${ContactsContract.Contacts.HAS_PHONE_NUMBER} =1) " +
                    "AND (${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY}!= '' ))")

        val cursor = contentResolver.query(
            uri,
            PROJECTION,
            SELECTION,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
        )

        if (cursor == null) return emptyList()
        else{
            val list = mutableListOf<ContactItem>()

            try {
                while (cursor.moveToNext()) {
                list.add(
                    ContactItem(
                        cursor.getLong(CURSOR_ID),
                        cursor.getString(CURSOR_LOOKUP_KEY),
                        cursor.getString(CURSOR_NAME),
                        cursor.getString(CURSOSR_PHOTO_THUMBNAIL_URI),
                        cursor.getString(CURSOR_HAS_PHONE_NUMBER)
                    )
                )
            }
            } finally {
                cursor.close();
            }

        Log.i(MY_TAG, "convert cursor u listu $list")
        //prazan kontakt na kraj da se vidi iza button-a set email and pass
        list.add(EMPTY_CONTACT_ITEM)
            return list
        }

    }


    fun getPhoneNumbersForContact(lookUpKey:String):List<PhoneItem>{

        val CURSOR_ID=0
        val CURSOR_PHONE=1
        val CURSOR_PHONE_TYPE=2
        val CURSOR_PHOTO_URI=3
        val CURSOR_PHOTO_FILE_ID=4

        val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
            ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID
        )

        val SELECTION: String = "${ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY} = ?"

        val selectionArguments=arrayOf<String>(lookUpKey)

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            PROJECTION,
            SELECTION,
            selectionArguments,
            null
        )

        if (cursor == null) return emptyList()

        val list = mutableListOf<PhoneItem>()

        try {
            while (cursor.moveToNext()) {
                list.add(
                    PhoneItem(
                        cursor.getString(CURSOR_PHONE),
                        cursor.getInt(CURSOR_PHONE_TYPE),
                        cursor.getString(CURSOR_PHOTO_URI),
                        cursor.getString(CURSOR_PHOTO_FILE_ID)
                    )

                )
            }
        } finally {
            cursor.close();
        }
        Log.i(MY_TAG,"convert cursor u listu $list")

        return list

    }

    suspend fun exportPhoneBook(token:String, phoneNumber:String,phoneBook:List<PhoneBookItem>,initialExport:Boolean=false){
        Log.i(MY_TAG, "EXPORTING PHONEBOOK")

            val deferredRes = myAPIService.exportPhoneBook(
                request = NetRequest_ExportPhonebook(
                    token,
                    phoneNumber,
                    phoneBook.toTypedArray()
                )
            )
            try {
                val result = deferredRes.await()

                if (result.authTokenMismatch == true) logoutAll(myDatabaseDao)
                else {
                    if (initialExport) _initialexportPhoneBookNetworkSuccess.value = true
                    else _exportPhoneBookWebViewNetworkSuccess.value = true
                    Log.i(MY_TAG, "EXPORTING PHONEBOOK SUCCESS")
                }

            } catch (t: Throwable) {
                    Log.i(MY_TAG, "EXPORTING PHONEBOOK FAILURE")
                    Log.i(MY_TAG, "network greska je ${t.message}")
            }


    }

    fun initialPhoneBookExportFinished(){
        _initialexportPhoneBookNetworkSuccess.value=false

    }

    fun phoneBookExportFinishedFromWebView(){
        _exportPhoneBookWebViewNetworkSuccess.value=false
    }


     fun getE1Timestamp()= myDatabaseDao.getE1Timestamp()

    fun refreshE1(phoneNumber: String,token:String){
        if(token.isNotEmpty() && phoneNumber.isNotEmpty()) {

            val myE1Job = UncancelableJob(phone=phoneNumber,resultAuthorization = null, resultSetAccountEmailAndPass = null, myDatabaseDao = myDatabaseDao, myAPI = myAPIService)

            GlobalScope.launch {
                withContext(IO) {
                    //delay(3000)
                    myE1Job.startRefreshE124HPassed(token)
                }
            }
        }else{
            Log.i(MY_TAG," token ili phone je prazan string ruta refresh E1")

        }

    }


}



