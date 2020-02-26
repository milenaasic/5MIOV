package com.vertial.sipdnidphone.ui.fragment_main

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

private val MYTAG="MY_MAINFRAGM_VIEWMODEL"

class MainFragmentViewModel(application: Application) :AndroidViewModel(application) {

    val contentResolver = getApplication<Application>().contentResolver

    private val _contactList = MutableLiveData<List<ContactItem>>()
    val contactList: LiveData<List<ContactItem>>
        get() = _contactList

    private val _numberOfSelectedContacts = MutableLiveData<Int>()
    val numberOfSelectedContacts: LiveData<Int>
        get() = _numberOfSelectedContacts

    init {
        //populateContactList("")
    }


     fun populateContactList(searchString:String?) {

         Log.i(MYTAG,"uri jeje ${getUri(searchString)}")
        GetAllContacts(contentResolver,getUri(searchString),searchString).execute()
    }

    private fun getUri(search: String?):Uri{
        Log.i(MYTAG,"search je $search")
        return if(search!=null)Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(search))
        else ContactsContract.Contacts.CONTENT_URI
    }


    inner class GetAllContacts(val contentResolver: ContentResolver,val uri: Uri,val search:String?):
        AsyncTask<Unit, Unit, List<ContactItem>>() {

        private val CURSOR_ID=0
        private val CURSOR_LOOKUP_KEY=1
        private val CURSOR_NAME=2
        private val CURSOSR_PHOTO_THUMBNAIL_URI=3
        private val CURSOR_HAS_PHONE_NUMBER=4

        //var baseUri = ContactsContract.Contacts.CONTENT_URI

        //var baseUri2 = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(search))



        private val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )
        private val SELECTION: String = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
        val selectionArguments=if(search==null)arrayOf<String>("%")
                                    else arrayOf<String>("%$search%")



        override fun doInBackground(vararg p0: Unit?): List<ContactItem> {
            val cursor = contentResolver.query(
                uri,
                PROJECTION,
                SELECTION,
                selectionArguments,
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
            )
            Log.i(MYTAG,"u do in background")

            return convertCursorToList(cursor)
        }

        private fun convertCursorToList(cursor: Cursor?): List<ContactItem> {

            if (cursor == null) return emptyList()

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
            Log.i(MYTAG,"convert cursor u listu $list")
            return list

        }


        override fun onPostExecute(result: List<ContactItem>) {
            _contactList.value=result
            _numberOfSelectedContacts.value=result.size
        }




    }



}