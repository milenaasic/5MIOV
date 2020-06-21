package com.vertial.fivemiov.ui.webView

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.*
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.data.produceJWtToken
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.ActivityWebViewBinding
import com.vertial.fivemiov.model.PhoneBookItem
import com.vertial.fivemiov.ui.initializeSharedPrefToFalse
import com.vertial.fivemiov.ui.myapplication.MyApplication
import com.vertial.fivemiov.utils.DEFAULT_SHARED_PREFERENCES
import com.vertial.fivemiov.utils.PHONEBOOK_IS_EXPORTED
import kotlinx.android.synthetic.main.fragment_detail_contact.*
import okhttp3.Cookie
import okhttp3.internal.parseCookie
import org.acra.ACRA


private const val MY_TAG="MY_WEBVIEW_ACTIVITY"
class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var viewModel: WebViewViewModel

    companion object{
         const val HEADER_AUTH_TOKEN_KEY="X-Wvtk"
         const val DASHBOARD_URL= BASE_URL+"dashboard"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view)

        val myDatabaseDao = MyDatabase.getInstance(this).myDatabaseDao
        val myApi = MyAPI.retrofitService
        val mobileAppVersion = (application as MyApplication).mobileAppVersion
        val myRepository = RepoContacts(
            contentResolver,
            myDatabaseDao,
            myApi,
            resources.getString(R.string.mobile_app_version_header, mobileAppVersion)
        )



        viewModel = ViewModelProvider(this, WebViewViewModelFactory(myRepository, application))
            .get(WebViewViewModel::class.java)



        binding.myWebView.apply {
            webViewClient = MyWebWievClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

        }

        val cookieManager = CookieManager.getInstance()

        Log.i(MY_TAG, "pre setovanja has cookies ${cookieManager.hasCookies()}")
        Log.i(MY_TAG, "pre setovanja get cookies ${cookieManager.getCookie(DASHBOARD_URL)}")

        cookieManager.setCookie(DASHBOARD_URL, "webView=webView")
        Log.i(MY_TAG, "posle setovanja has cookies ${cookieManager.hasCookies()}")
        Log.i(MY_TAG, "posle setovanja get cookies ${cookieManager.getCookie(DASHBOARD_URL)}")

        viewModel.user.observe(this, Observer {

            ACRA.getErrorReporter()
                .putCustomData("WEBVIEW_ACTIVITY_observe_user_data_phone", it.userPhone)
            ACRA.getErrorReporter()
                .putCustomData("WEBVIEW_ACTIVITY_observe_user_data_token", it.userToken)

            if (it != null) binding.myWebView.loadUrl(
                DASHBOARD_URL,
                getCustomHeaders(token = it.userToken, phone = it.userPhone)
            )

        })

        viewModel.startGetingPhoneBook.observe(this, Observer {
            if (it != null) {
                if (it == true) {
                    viewModel.getPhoneBook()
                    viewModel.resetStartGetingPhoneBook()

                }

            }

        })

        viewModel.phoneBook.observe(this, Observer {
            if (it != null) {
                val mylist = it.filter { item: PhoneBookItem? -> item != null }
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

                    sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED, true)
                        .apply()

                }
            }
        })

        viewModel.loggingOut.observe(this, Observer {
            if (it != null) {
                if (it) {
                    initializeSharedPrefToFalse(application)
                    viewModel.resetLoggingOutToFalse()
                }

            }
        })


    }



    fun getCustomHeaders(token:String, phone:String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val mobileAppVer=resources.getString(R.string.mobile_app_version_header,(application as MyApplication).mobileAppVersion)
        map.put(HEADER_AUTH_TOKEN_KEY, token)
        map.put(HEADER_PHONE_KEY,phone)
        map.put(HEADER_MOBILE_APP_VERSION,mobileAppVer)
        map.put(HEADER_SIGNATURE, produceJWtToken(
                                                Pair(HEADER_AUTH_TOKEN_KEY,token),
                                                Pair(HEADER_PHONE_KEY,phone)
                                )
        )
        return map
    }




    inner class MyWebWievClient() : WebViewClient() {


        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)


        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            //progress bar gone
            binding.webViewLinLayout.visibility= View.GONE

        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Log.i(MY_TAG," WebView onReceivedError $error, $detail_contact_rec_view, $request")
            super.onReceivedError(view, request, error)
        }


    }
}

