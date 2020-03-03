package com.vertial.fivemiov.ui.webview_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import com.vertial.fivemiov.R
import androidx.webkit.WebViewClientCompat

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val webView=findViewById<WebView>(R.id.myWebView)

        webView.webViewClient=object:WebViewClientCompat(){
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return true
            }
        }


        webView.loadUrl("https://www.doubango.org/sipml5/call.htm?svn=252")
    }
}
