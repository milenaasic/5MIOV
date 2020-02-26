package com.vertial.sipdnidphone.ui.fragment_detail_contact

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.os.AsyncTask
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vertial.sipdnidphone.data.Repo

private val MYTAG="MY_DetailContViewModel"
class DetailContactViewModel(val contactLookUp:String,myRepository: Repo, application: Application) : AndroidViewModel(application) {

    val contentResolver = getApplication<Application>().contentResolver

    private val _phoneList = MutableLiveData<List<PhoneItem>>()
    val phoneList: LiveData<List<PhoneItem>>
        get() = _phoneList



    init {
        getContactPhoneNumbers()


    }

    private fun getContactPhoneNumbers() {
        GetContactsNumbers(contentResolver,lookUpKey = contactLookUp).execute()
        Log.i(MYTAG,"usao u get contacts phone ")

    }


    inner class GetContactsNumbers(val contentResolver: ContentResolver, val lookUpKey:String):
        AsyncTask<Unit, Unit, List<PhoneItem>>() {


        private val CURSOR_ID=0
        private val CURSOR_PHONE=1
        private val CURSOR_PHONE_TYPE=2
        private val CURSOR_PHOTO_URI=3
        private val CURSOR_PHOTO_FILE_ID=4



        private val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
            ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID
        )

        private val SELECTION: String = "${ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY} = ?"

        val selectionArguments=arrayOf<String>(lookUpKey)

        override fun doInBackground(vararg p0: Unit?): List<PhoneItem> {
            Log.i(MYTAG,"u do in background phone mime type ${ContactsContract.CommonDataKinds.Phone.CONTENT_URI}")

            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION,
                SELECTION,
                selectionArguments,
                null
            )


            return convertCursorToList(cursor)
        }

        private fun convertCursorToList(cursor: Cursor?): List<PhoneItem> {

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
            Log.i(MYTAG,"convert cursor u listu $list")
            return list

        }


        override fun onPostExecute(result: List<PhoneItem>) {
            _phoneList.value=result

        }


    }



}