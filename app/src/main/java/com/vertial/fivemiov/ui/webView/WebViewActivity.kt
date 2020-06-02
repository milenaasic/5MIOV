package com.vertial.fivemiov.ui.webView

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.BASE_URL
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.data.produceJWtToken
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.ActivityWebViewBinding
import com.vertial.fivemiov.model.PhoneBookItem
import com.vertial.fivemiov.model.User
import com.vertial.fivemiov.ui.initializeSharedPrefToFalse
import com.vertial.fivemiov.ui.main_activity.MainActivity
import com.vertial.fivemiov.utils.DEFAULT_SHARED_PREFERENCES
import com.vertial.fivemiov.utils.PHONEBOOK_IS_EXPORTED
import kotlinx.android.synthetic.main.fragment_detail_contact.*


private const val MY_TAG="MY_WebVIewActivity"
class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var viewModel: WebViewViewModel

    companion object{
         const val HEADER_AUTH_TOKEN_KEY="X-Wvtk"
         const val HEADER_AUTH_PHONE_KEY="X-Phone-Number"
         const val HEADER_SIGNATURE="Sign"
         const val DASHBOARD_URL= BASE_URL+"dashboard"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view)

        val myDatabaseDao = MyDatabase.getInstance(this).myDatabaseDao
        val myApi = MyAPI.retrofitService
        val myRepository = RepoContacts(contentResolver, myDatabaseDao, myApi)

       /* val myApp=application as MyApplication
        val myAppContanier=myApp.myAppContainer*/

        viewModel = ViewModelProvider(this, WebViewViewModelFactory(myRepository, application))
            .get(WebViewViewModel::class.java)


        binding.myWebView.apply {
            webViewClient = MyWebWievClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled=true

        }

        viewModel.user.observe(this, Observer {
            Log.i(MY_TAG, " user je $it")
            if(it!=null) binding.myWebView.loadUrl(DASHBOARD_URL, getCustomHeaders(token=it.userToken,phone=it.userPhone))

        })

        viewModel.startGetingPhoneBook.observe(this, Observer {
            if(it!=null){
                if(it==true){
                    viewModel.getPhoneBook()
                    viewModel.resetStartGetingPhoneBook()

                }

            }

         })

        viewModel.phoneBook.observe(this, Observer {
            if (it != null) {
                Log.i(MY_TAG,"phone book je $it")
               // val testLIst= mutableListOf<PhoneBookItem?>()
                val mylist=it.filter { item:PhoneBookItem?->item!=null }
                viewModel.exportPhoneBook(mylist)
            }
        })


        viewModel.phoneBookExported.observe(this, Observer {
            if (it) {

                viewModel.phoneBookExportFinished()

                val sharedPreferences = application.getSharedPreferences(
                    DEFAULT_SHARED_PREFERENCES,
                    Context.MODE_PRIVATE
                )
                if (sharedPreferences.contains(PHONEBOOK_IS_EXPORTED)) {
                    val isExported =
                        sharedPreferences.getBoolean(PHONEBOOK_IS_EXPORTED, false)
                    Log.i(
                        MY_TAG,
                        " usao u ima phoneBookIsExported promenljiva i vrednost je $isExported"
                    )
                    sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED, true)
                        .apply()
                    Log.i(MY_TAG, "  phoneBookIsExported promenljiva posle promene $isExported")
                }
            }
        })

        viewModel.loggingOut.observe(this, Observer {
            if(it!=null){
                if(it) {
                    initializeSharedPrefToFalse(application)
                    viewModel.resetLoggingOutToFalse()
                }

            }
        })


    }



    fun getCustomHeaders(token:String, phone:String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map.put(HEADER_AUTH_TOKEN_KEY, token)
        map.put(HEADER_AUTH_PHONE_KEY,phone)
        map.put(HEADER_SIGNATURE, produceJWtToken(
                                                Pair(HEADER_AUTH_TOKEN_KEY,token),
                                                Pair(HEADER_AUTH_PHONE_KEY,phone)
                                )
        )
        return map
    }




    inner class MyWebWievClient() : WebViewClient() {

        /*override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            Log.i(
                MY_TAG, "shouldOverrideUrlLoading, url je ${request?.url}, super je ${super.shouldOverrideUrlLoading(view, request)}"
            )
            return super.shouldOverrideUrlLoading(view, request)

        }*/


        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.i(MY_TAG, "web view client onPage started")

        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            //progress bar gone
            Log.i(MY_TAG, "web view client onPage finished")
            binding.webViewLinLayout.visibility= View.GONE

        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Log.i(MY_TAG," webview greska $error, $detail_contact_rec_view, $request")
            super.onReceivedError(view, request, error)
        }


    }
}

