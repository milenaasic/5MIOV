package com.vertial.fivemiov.ui.main_activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.ActivityMainBinding
import com.vertial.fivemiov.ui.RegistrationAuthorization.RegistrationAuthorizationActivity
import com.vertial.fivemiov.ui.webView.WebViewActivity
import com.vertial.fivemiov.utils.EMPTY_EMAIL
import com.vertial.fivemiov.utils.EMPTY_PHONE_NUMBER




private const val MY_TAG="MY_MainActivity"
class MainActivity : AppCompatActivity() {


    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: MainActivityViewModel
    private var userHasEmailAndPass: Boolean = false

    //private lateinit var appUpdateManager: AppUpdateManager
    private val MY_UPDATE_REQUEST_CODE = 10

    companion object {
        const val CURRENT_FRAGMENT = "current_fragment"
        const val MAIN_FRAGMENT = 0
        const val DIAL_PAD_FRAGMENT = 1
        const val DETAIL_FRAGMENT = 2
        const val SET_ACCOUNT_EMAIL_PASS_FRAGMENT0 = 3
        const val SIP_FRAGMENT = 4

        const val MAIN_ACTIVITY_SHARED_PREF_NAME = "MainActivitySharedPref"
        const val PHONEBOOK_IS_EXPORTED = "phone_book_is_exported"

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbarMain)

        val myDatabaseDao = MyDatabase.getInstance(this).myDatabaseDao
        val myApi = MyAPI.retrofitService

        val myRepository = RepoContacts(contentResolver, myDatabaseDao, myApi)

        viewModel = ViewModelProvider(this,
            MainActivityViewModelFactory(
                myRepository,
                application
            )
        )
            .get(MainActivityViewModel::class.java)


        navController = findNavController(R.id.navfragment_main)
        NavigationUI.setupWithNavController(binding.toolbarMain, navController)



        navController.addOnDestinationChangedListener { controller, destination, arguments ->

            when (destination.id) {

                R.id.mainFragment -> {
                    setMainFragmentUI()
                }

                R.id.dialPadFragment -> {
                    setDialPadFragmentUI()
                }

                R.id.detailContact -> {
                    setDetailContactFragmentUI()
                }

                R.id.setEmailAndPasswordFragment -> {
                    setEmailAndAccountFragmentUI()
                }

                R.id.sipFragment->{
                    setSipFragmentUI()
                }

            }

        }

        if (savedInstanceState != null) {
            Log.i(MY_TAG, "usao u onSaveInstance nije null")

            when (savedInstanceState.get(CURRENT_FRAGMENT)) {
                MAIN_FRAGMENT -> {
                }
                DIAL_PAD_FRAGMENT -> {
                    setDialPadFragmentUI()
                }
                DETAIL_FRAGMENT -> {
                    setDetailContactFragmentUI()
                }
                SET_ACCOUNT_EMAIL_PASS_FRAGMENT0 -> {
                    setEmailAndAccountFragmentUI()
                }

                SIP_FRAGMENT -> {
                    setSipFragmentUI()
                }
            }

        }

        //showSetAccountDialog()

        viewModel.userData.observe(this, Observer { user ->

            if (user.userPhone.equals(EMPTY_PHONE_NUMBER)) {
                startActivity(Intent(this, RegistrationAuthorizationActivity::class.java))
                finish()
            }else{
                userHasEmailAndPass = !user.userEmail.equals(EMPTY_EMAIL)

            }

        })

        viewModel.phoneBook.observe(this, Observer {
            if (it != null) {
                viewModel.exportPhoneBook(it)
            }
            Log.i(MY_TAG, "phonebook lista je main activity $it")
        })

        viewModel.phoneBookExported.observe(this, Observer {
            if (it) {
                val sharedPreferences =
                    getSharedPreferences(MAIN_ACTIVITY_SHARED_PREF_NAME, Context.MODE_PRIVATE)
                if (sharedPreferences.contains(PHONEBOOK_IS_EXPORTED)) {
                    val isExported = sharedPreferences.getBoolean(PHONEBOOK_IS_EXPORTED, false)
                    Log.i(
                        MY_TAG,
                        " usao u ima phoneBookIsExported promenljiva i vrednost je $isExported"
                    )
                    sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED, true).apply()
                    Log.i(MY_TAG, "  phoneBookIsExported promenljiva posle promene $isExported")

                }
            }

        })

        //IN APP UPDATE
        // Creates instance of the manager.
        /*appUpdateManager = AppUpdateManagerFactory.create(this)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Request the update
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    MY_UPDATE_REQUEST_CODE
                )
            }
        }*/


    }




    override fun onResume() {
        super.onResume()

        /*appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        IMMEDIATE,
                        this,
                        MY_UPDATE_REQUEST_CODE
                    )
                }
            }*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MY_UPDATE_REQUEST_CODE) {
            Log.i(MY_TAG, "Update flow Result code: $resultCode")
            if (resultCode != RESULT_OK) {
                Log.i(MY_TAG, "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }

    }



    private fun setMainFragmentUI() {
        binding.toolbarMain.apply {
            elevation=(4 * resources.displayMetrics.density)
            title=resources.getString(R.string.app_name)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility= 0
                setBackgroundColor(resources.getColor(android.R.color.background_light,null))
                window.statusBarColor= resources.getColor(R.color.colorPrimaryDark,null)
            }else{
                setBackgroundColor(resources.getColor(android.R.color.background_light))
                window.statusBarColor= resources.getColor(R.color.colorPrimaryDark)
            }

         }
    }

    private fun setDialPadFragmentUI() {
        binding.toolbarMain.apply {
            navigationIcon = resources.getDrawable(R.drawable.ic_back_black, null)
            elevation = 2f
            title=resources.getString(R.string.empty_string)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor= Color.TRANSPARENT
        }
    }

    private fun setDetailContactFragmentUI(){
        binding.toolbarMain.apply {
            elevation = 0f
            navigationIcon = resources.getDrawable(R.drawable.ic_back_white, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setBackgroundColor(resources.getColor(R.color.colorPrimaryDark, null))
                window.decorView.systemUiVisibility = 0
                window.statusBarColor = resources.getColor(R.color.colorPrimaryDark, null)
            } else {
                setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
                window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
            }
            title = resources.getString(R.string.empty_string)

        }

    }

    private fun setEmailAndAccountFragmentUI() {
        binding.toolbarMain.apply {
            navigationIcon = resources.getDrawable(R.drawable.ic_back_black, null)
            elevation = 0f
            title=resources.getString(R.string.empty_string)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
             window.statusBarColor= Color.TRANSPARENT
        }
    }

    private fun setSipFragmentUI() {
        binding.toolbarMain.apply {
            navigationIcon = null
            elevation = 0f
            title = resources.getString(R.string.empty_string)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setBackgroundColor(resources.getColor(android.R.color.background_light, null))
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.statusBarColor = Color.TRANSPARENT
            } else {
                setBackgroundColor(resources.getColor(android.R.color.background_light))
                window.statusBarColor = Color.TRANSPARENT
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_item_logout-> {
                                    handleLogOut()
                                    return true
                                    }
            R.id.menu_item_myaccount->{
                                    startActivity(Intent(this, WebViewActivity::class.java))
                                    return true
            }
            else->return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)

        }

    }

    private fun handleLogOut() {
       if(userHasEmailAndPass) logout()
        else showAlertDialog()

    }

    private fun logout() {
        Log.i(MY_TAG,"   log out funkcija")
        viewModel.logout()
    }

    private fun showAlertDialog(){
        val alertDialog=AlertDialog.Builder(this)
            .setMessage(getString(R.string.log_out_warning))
            .setPositiveButton("LOG OUT ANYWAY",object:DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    logout()
                }
            })
            .setNeutralButton("CANCEL",object :DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    p0?.dismiss()}
                })

        alertDialog.show()

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var currentFragment=
            MAIN_FRAGMENT
        when (navController.currentDestination?.id) {
            R.id.mainFragment->{}
            R.id.dialPadFragment->currentFragment=
                DIAL_PAD_FRAGMENT
            R.id.detailContact->currentFragment=
                DETAIL_FRAGMENT
        }

        outState.putInt(CURRENT_FRAGMENT,currentFragment)
    }


    fun exportPhoneBook(){
        viewModel.getPhoneBook()
    }

    private fun showSetAccountDialog(){

        val builder: AlertDialog.Builder? = AlertDialog.Builder(this)

        builder?.apply {
            setMessage(R.string.set_account_dialog)
                .setTitle("Important")
                .setNeutralButton("I Understand", { dialog, id ->
                    dialog.dismiss()
                })

                .setCancelable(false)

        }
        builder?.create()?.show()

    }



}
