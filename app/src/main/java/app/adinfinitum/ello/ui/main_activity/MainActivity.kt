package app.adinfinitum.ello.ui.main_activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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
import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.MyAPI
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.database.MyDatabase
import app.adinfinitum.ello.databinding.ActivityMainBinding
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.ui.registrationauthorization.RegistrationAuthorizationActivity
import app.adinfinitum.ello.ui.webView.WebViewActivity
import app.adinfinitum.ello.utils.*
import org.acra.ACRA


private const val MY_TAG="MY_MAIN_ACTIVITY"
private const val LOG_STATE_TO_SERVER_TAG="Main_Activity"
class MainActivity : AppCompatActivity() {


    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: MainActivityViewModel
    private var userHasEmailAndPass: Boolean = false

    private var mySavedInstanceState:Bundle?=null

    private lateinit var appUpdateManager: AppUpdateManager
    private val MY_UPDATE_REQUEST_CODE = 10


    companion object {
        const val CURRENT_FRAGMENT = "current_fragment"
        const val MAIN_FRAGMENT = 0
        const val DIAL_PAD_FRAGMENT = 1
        const val DETAIL_FRAGMENT = 2
        const val SET_ACCOUNT_EMAIL_PASS_FRAGMENT = 3
        const val SIP_FRAGMENT = 4
        const val ABOUT_FRAGMENT=5


    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbarMain)

        /*val myDatabaseDao = MyDatabase.getInstance(this).myDatabaseDao
        val myApi = MyAPI.retrofitService

        val myRepository = RepoContacts(contentResolver,
                                        myDatabaseDao,
                                        myApi,
                                        resources.getString(R.string.mobile_app_version_header,(application as MyApplication).mobileAppVersion)
        )*/



        viewModel = ViewModelProvider(this,
            MainActivityViewModelFactory(
                (application as MyApplication).repoContacts,
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

                R.id.aboutFragment->{
                    setAboutFragmentUI()
                }

            }

        }

        if (savedInstanceState != null) {
            mySavedInstanceState=savedInstanceState
            Log.i(MY_TAG, "savedInstanceState is not null")

            when (savedInstanceState.get(CURRENT_FRAGMENT)) {
                MAIN_FRAGMENT -> {
                    setMainFragmentUI()
                }
                DIAL_PAD_FRAGMENT -> {
                    setDialPadFragmentUI()
                }
                DETAIL_FRAGMENT -> {
                    setDetailContactFragmentUI()
                }
                SET_ACCOUNT_EMAIL_PASS_FRAGMENT -> {
                    setEmailAndAccountFragmentUI()
                }

                SIP_FRAGMENT -> {
                   navController.navigateUp()
                    //setSipFragmentUI()
                }

                ABOUT_FRAGMENT ->{
                    setAboutFragmentUI()
                }
            }

        }else setDialPadFragmentUI()



        viewModel.userData.observe(this, Observer { user ->
            //crash report custom data
           ACRA.getErrorReporter().putCustomData("MAIN_ACTIVITY_observe_user_data_phone",user.userPhone)
           ACRA.getErrorReporter().putCustomData("MAIN_ACTIVITY_observe_user_data_token",user.userToken)

            viewModel.logStateOrErrorToMyServer(
                mapOf(
                    Pair("process","Main Activity"),
                    Pair("state","MAIN_ACTIVITY_observe_user_data: phone ${user.userPhone},token ${user.userToken}, $user")
                )
            )

            if(user!=null){
                if (user.userPhone==EMPTY_PHONE_NUMBER || user.userPhone.isEmpty() || user.userToken== EMPTY_TOKEN || user.userToken.isEmpty()) {
                    startActivity(Intent(this, RegistrationAuthorizationActivity::class.java))
                    finish()
                }else{
                    userHasEmailAndPass = !user.userEmail.equals(EMPTY_EMAIL)
                }
            }
        })


        viewModel.shouldShowSetAccountDisclaimer.observe(this, Observer {

            if(it!=null) {
                if (it) {
                    showSetAccountDialog()
                    viewModel.setAccountDialogDiscalimerShown()
                }
            }
         })


        viewModel.showSetAccountDisclaimer()

        // start in app update
        startInAppUpdate()

    }

    override fun onRestart() {

       Log.i(MY_TAG, " Restart, saved state is ${mySavedInstanceState?.get(CURRENT_FRAGMENT)}")
      /* if(navController.currentDestination?.id==R.id.sipFragment) {
           navController.navigateUp()
       }*/
        super.onRestart()
    }

    override fun onResume() {
        super.onResume()
        Log.i(MY_TAG, " onResume")
       checkIfUpdateInProcess()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MY_UPDATE_REQUEST_CODE) {
            Log.i(MY_TAG, "Update flow Result code: $resultCode")

            viewModel.logStateOrErrorToMyServer(
                mapOf(
                    Pair("process","Main Activity"),
                    Pair("state","Automatic Update resultCode :$resultCode")
                )
            )

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
            title=resources.getString(R.string.fragment_contacts_title)

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
            elevation=(4 * resources.displayMetrics.density)
            title=resources.getString(R.string.app_name)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.statusBarColor= Color.TRANSPARENT
            }

        }

    }

    private fun setDetailContactFragmentUI(){
        binding.toolbarMain.apply {
            elevation = 0f
            //navigationIcon = resources.getDrawable(R.drawable.ic_back_white, null)
            navigationIcon = ResourcesCompat.getDrawable(resources,R.drawable.ic_back_white, null)

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
            //navigationIcon = resources.getDrawable(R.drawable.ic_back_black, null)
            navigationIcon = ResourcesCompat.getDrawable(resources,R.drawable.ic_back_black, null)
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
            //navigationIcon=resources.getDrawable(R.drawable.logo5m169, null)
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


    private fun setAboutFragmentUI(){
        binding.toolbarMain.apply {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    window.decorView.systemUiVisibility = 0
                    setBackgroundColor(resources.getColor(android.R.color.background_light, null))
                    window.statusBarColor = resources.getColor(R.color.colorPrimaryDark, null)
            }else{
                    setBackgroundColor(resources.getColor(android.R.color.background_light))
                    window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
            }
            }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.menu_item_myaccount->{
                                    startActivity(Intent(this, WebViewActivity::class.java))
                                    return true
            }
            R.id.menu_item_share->{
                    val share = Intent.createChooser(Intent().apply {
                        action = Intent.ACTION_SEND
                        Log.i(MY_TAG,"google play link ${resources.getString(R.string.google_play_link_to_app,application.packageName)}")
                        putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.google_play_link_to_app,application.packageName))

                        // (Optional) Here we're setting the title of the content
                        //putExtra(Intent.EXTRA_TITLE, "Make cheap international calls")
                        type="text/plain"

                        }, getString(R.string.share_title))
                        startActivity(share)
                        return true

            }
            else->return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)

        }

    }



    override fun onSaveInstanceState(outState: Bundle) {

        var currentFragment=
            MAIN_FRAGMENT
        when (navController.currentDestination?.id) {
            R.id.mainFragment->currentFragment= MAIN_FRAGMENT
            R.id.dialPadFragment->currentFragment= DIAL_PAD_FRAGMENT
            R.id.detailContact->currentFragment= DETAIL_FRAGMENT
            R.id.setEmailAndPasswordFragment->currentFragment= SET_ACCOUNT_EMAIL_PASS_FRAGMENT
            R.id.sipFragment->currentFragment= SIP_FRAGMENT
        }

        outState.putInt(CURRENT_FRAGMENT,currentFragment)
        super.onSaveInstanceState(outState)
    }


    private fun showSetAccountDialog(){

        val builder: AlertDialog.Builder? = AlertDialog.Builder(this)

        builder?.apply {
            setMessage(R.string.set_account_dialog)
                .setTitle(getString(R.string.disclaimer_title_important))
                .setNeutralButton(getString(R.string.disclaimer_IUnderstand_button), { dialog, id ->
                    setSharedPrefDisclaimerShownValueToTrue()
                    dialog.dismiss()
                })

                .setCancelable(false)

        }
        builder?.create()?.show()

    }

    private fun setSharedPrefDisclaimerShownValueToTrue() {
        val sharedPreferences =
            application.getSharedPreferences(DEFAULT_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        if (sharedPreferences.contains(DISCLAIMER_WAS_SHOWN)) {
            sharedPreferences.edit().putBoolean(DISCLAIMER_WAS_SHOWN, true).apply()
        }
    }

    private fun startInAppUpdate(){
        //IN APP UPDATE
        // Creates instance of the manager.
        viewModel.logStateOrErrorToMyServer(
            mapOf(
                Pair("process","Main Activity"),
                Pair("state","startInAppUpdate")
            )
        )

        appUpdateManager = AppUpdateManagerFactory.create(this)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Request the update
                viewModel.logStateOrErrorToMyServer(
                    mapOf(
                        Pair("process","Main Activity"),
                        Pair("state","startInAppUpdate, UpdateAvailability.UPDATE_AVAILABLE")
                    )
                )

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
        }

    }

    private fun checkIfUpdateInProcess(){
        appUpdateManager
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
            }

    }


    override fun onBackPressed() {
        if(navController.currentDestination?.id==R.id.sipFragment) {
            //do nothing
            Toast.makeText(this,"Back button currently disabled",Toast.LENGTH_SHORT).show()
        } else super.onBackPressed()
    }


}


