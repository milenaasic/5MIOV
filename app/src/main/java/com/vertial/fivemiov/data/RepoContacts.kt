package com.vertial.fivemiov.data

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.api.MyAPIService
import com.vertial.fivemiov.api.NetRequest_ExportPhonebook
import com.vertial.fivemiov.api.NetResponse_ExportPhonebook
import com.vertial.fivemiov.database.MyDatabaseDao
import com.vertial.fivemiov.ui.fragment_detail_contact.PhoneItem
import com.vertial.fivemiov.ui.fragment_main.ContactItem
import com.vertial.fivemiov.utils.EMPTY_EMAIL
import com.vertial.fivemiov.utils.EMPTY_PHONE_NUMBER
import com.vertial.fivemiov.utils.EMPTY_TOKEN
import kotlinx.coroutines.*

private const val MY_TAG="MY_ContactsRepository"
class RepoContacts (val contentResolver: ContentResolver,val myDatabaseDao: MyDatabaseDao, val myAPIService: MyAPIService) {

    //User Live Data
    fun getUserData() = myDatabaseDao.getUser()

    // Live Data
    fun getPremunber() = myDatabaseDao.getPrenumber()


    //export phonebook network response
    private val _exportPhoneBookNetworkSuccess = MutableLiveData<Boolean>()
    val exportPhoneBookNetworkSuccess: LiveData<Boolean>
        get() = _exportPhoneBookNetworkSuccess


    suspend fun logout(){
        withContext(Dispatchers.IO){
            myDatabaseDao.logout(EMPTY_PHONE_NUMBER, EMPTY_TOKEN, EMPTY_EMAIL)
        }

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

    suspend fun exportPhoneBook(phoneBook:List<PhoneBookItem>){
        val deferredRes=myAPIService.exportPhoneBook(request = NetRequest_ExportPhonebook(phoneBook.toTypedArray()))
        try {
            val result=deferredRes.await()
            _exportPhoneBookNetworkSuccess.value=true

        }catch (e:Exception){
            Log.i(MY_TAG,"network greska je ${e.message}")
        }

    }


}



