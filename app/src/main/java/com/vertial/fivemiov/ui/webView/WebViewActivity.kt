package com.vertial.fivemiov.ui.webView

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.ActivityWebViewBinding
import com.vertial.fivemiov.ui.main_activity.MainActivity


private const val MY_TAG="MY_WebVIewActivity"
class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var viewModel: WebViewViewModel

    companion object{
         const val HEADER_AUTH_TOKEN_KEY="wvtk"
         const val DASHBOARD_URL="https://5miov.vertial.net/dashboard"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view)

        val myDatabaseDao = MyDatabase.getInstance(this).myDatabaseDao
        val myApi = MyAPI.retrofitService

        val myRepository = RepoContacts(contentResolver, myDatabaseDao, myApi)

        viewModel = ViewModelProvider(this, WebViewViewModelFactory(myRepository, application))
            .get(WebViewViewModel::class.java)


        binding.myWebView.apply {
            webViewClient = MyWebWievClient()
            loadUrl(DASHBOARD_URL, getCustomHeaders())
            settings.javaScriptEnabled = true
            settings.domStorageEnabled=true

        }

        viewModel.user.observe(this, Observer {
            Log.i(MY_TAG, " user je $it")

        })

        viewModel.phoneBook.observe(this, Observer {
            if (it != null) {
                viewModel.exportPhoneBook(it)
            }
        })


        viewModel.phoneBookExported.observe(this, Observer {
            if (it) {
                val sharedPreferences = getSharedPreferences(
                    MainActivity.MAIN_ACTIVITY_SHARED_PREF_NAME,
                    Context.MODE_PRIVATE
                )
                if (sharedPreferences.contains(MainActivity.PHONEBOOK_IS_EXPORTED)) {
                    val isExported =
                        sharedPreferences.getBoolean(MainActivity.PHONEBOOK_IS_EXPORTED, false)
                    Log.i(
                        MY_TAG,
                        " usao u ima phoneBookIsExported promenljiva i vrednost je $isExported"
                    )
                    sharedPreferences.edit().putBoolean(MainActivity.PHONEBOOK_IS_EXPORTED, true)
                        .commit()
                    Log.i(MY_TAG, "  phoneBookIsExported promenljiva posle promene $isExported")

                }
            }

        })

       viewModel.loadloadDashboard()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.i(MY_TAG, "  on key down $keyCode, event je $event")

        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (binding.myWebView.canGoBack()) {
                        binding.myWebView.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun getCustomHeaders(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map.put(HEADER_AUTH_TOKEN_KEY, "7893c5c1781811ea9614839453911717")
        return map
    }

}


class MyWebWievClient() : WebViewClient() {

        @RequiresApi(Build.VERSION_CODES.N)
        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            Log.i(MY_TAG, "shouldInterceptRequest, url je ${request?.url}")
            Log.i(MY_TAG,"${request?.method},${request?.requestHeaders?.entries},${request?.requestHeaders?.keys}")
            Log.i(MY_TAG, "${request?.isRedirect}")

            val response = super.shouldInterceptRequest(view, request)
            Log.i(MY_TAG, "shouldInterceptRequestresponse je ${response.toString()}")

            return super.shouldInterceptRequest(view, request)
        }


        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            Log.i(MY_TAG, "shouldOverrideUrlLoading, url je ${request?.url}, super je ${super.shouldOverrideUrlLoading(view, request)}")

            return super.shouldOverrideUrlLoading(view, request)
           /* return if(request?.url!=null && request.url.toString().contains("checkout.paystack.com")) {
                Log.i(MY_TAG, "shouldOverrideUrlLoading, USAO U URL CONTAINS checkout.paystack.com}")
                Log.i(MY_TAG, "shouldOverrideUrlLoading, request url to string je ${request?.url} }")
                //paymentView.loadUrl(request.url.toString())
                return true
            } else super.shouldOverrideUrlLoading(view, request)*/

        }


}

