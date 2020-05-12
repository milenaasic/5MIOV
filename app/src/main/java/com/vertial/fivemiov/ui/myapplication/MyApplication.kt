package com.vertial.fivemiov.ui.myapplication

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log


class MyApplication : Application() {
private val MYTAG="MY_ApplicationContext"

    val mobileAppVersion:String by lazy {
        getMobAppVersion()
    }

    val myenigma:String by lazy {
        getEnigma()
    }

    private fun getEnigma():String {
        TODO("Not yet implemented")
        /*val i:String= "${111.toChar()}+${110.toChar()}+${97.toChar()}"

        val f = String.(111,110,97);
        f = String.fromCharCode(107,111,114) + f;*/
        return ""
    }



    private fun getMobAppVersion():String{

        var myversionName=""
        var versionCode=-1L

        try {
            val packageInfo: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0);
            myversionName = packageInfo.versionName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode=packageInfo.longVersionCode
            }else{
                versionCode= packageInfo.versionCode.toLong()

            }
        } catch ( e:Throwable) {
            Log.i(MYTAG,"package manager $e")
            e.printStackTrace();
        }

        return myversionName
    }

}