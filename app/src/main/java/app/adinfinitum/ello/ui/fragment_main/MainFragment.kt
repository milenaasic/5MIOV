package app.adinfinitum.ello.ui.fragment_main


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.MyAPI
import app.adinfinitum.ello.data.*
import app.adinfinitum.ello.database.MyDatabase
import app.adinfinitum.ello.databinding.FragmentMainLinLayoutBinding
import app.adinfinitum.ello.model.ContactItem
import app.adinfinitum.ello.model.ContactItemWithInternationalNumbers
import app.adinfinitum.ello.ui.main_activity.MainActivity
import app.adinfinitum.ello.ui.main_activity.MainActivityViewModel
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.utils.*

private val MYTAG="MY_MAIN_FRAGMENT"



class MainFragment : Fragment(){

    private lateinit var binding: FragmentMainLinLayoutBinding
    private lateinit var viewModel:MainFragmentViewModel
    private lateinit var activityViewModel:MainActivityViewModel
    private lateinit var contactsAdapter:MainFragmentAdapter
    private lateinit var searchViewActionBar: SearchView



    companion object{
        val MY_PERMISSIONS_REQUEST_READ_CONTACTS=10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_main_lin_layout,container,false)

        val app=requireActivity().application as MyApplication
        viewModel = ViewModelProvider(this, MainFragmentViewModelFactory(
                                                        app.repoContacts,
                                                        app.repoUser,
                                                        app.repoProvideContacts,
                                                        app.repoRemoteDataSource,
                                                        app.repoLogOut,
                                                        app.repoLogToServer,
                                                        app
                                                    )
                    )
                    .get(MainFragmentViewModel::class.java)

        activityViewModel = requireActivity().run {
            ViewModelProvider(this)[MainActivityViewModel::class.java]
        }

        initalizeAdapter()


        return binding.root
    }


    private fun shouldExportPhoneBook(): Boolean {
       val sharedPreferences= requireActivity().application.getSharedPreferences(
           DEFAULT_SHARED_PREFERENCES,Context.MODE_PRIVATE)

       if(sharedPreferences.contains(PHONEBOOK_IS_EXPORTED)){
            val isExported=sharedPreferences.getBoolean(PHONEBOOK_IS_EXPORTED,false)
           Log.i(MYTAG," Shared pref value Phonebook_is_exported: $isExported")
            if(!isExported) return true
            else return false

       }else{
            sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED,false).apply()
           Log.i(MYTAG," Shared pref value Phonebook_is_exported is created")
           return true
       }

    }

    private fun initalizeAdapter(){

        contactsAdapter=MainFragmentAdapter(ContactItemClickListener {
                           val action=MainFragmentDirections.actionMainFragmentToDetailContact(it.lookUpKey,it.name)
                             findNavController().navigate(action)
                            },getColorForHighlightLetters().toString())


        binding.mainFregmRecViewLinLayout.setHasFixedSize(true)
        binding.mainFregmRecViewLinLayout.adapter=contactsAdapter

        if(checkForPermissions()) viewModel.populateContactList("")

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.contactList.observe(viewLifecycleOwner, Observer {list->
            if(list!=null) contactsAdapter.dataList=list
         })

        viewModel.numberOfSelectedContacts.observe(viewLifecycleOwner, Observer {
            if(it!=null) binding.nbOfContactsTextView.text=String.format(resources.getString(R.string.nb_of_contacts_found,it))
         })

        viewModel.currentSearchString.observe(viewLifecycleOwner, Observer {
            if(it!=null)contactsAdapter.stringToColor=it
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.main_fragment_menu,menu)
        val mitem=menu.findItem(R.id.menu_item_search)
        val itemMyAccount= menu.findItem(R.id.menu_item_myaccount)
        val itemShare=menu.findItem(R.id.menu_item_share).apply {
            isVisible=false
        }
        val itemAboutFragment=menu.findItem(R.id.aboutFragment).apply {
            isVisible=false
         }
        searchViewActionBar=menu.findItem(R.id.menu_item_search).actionView as SearchView
        searchViewActionBar.setQueryHint(getString(R.string.search_hint))


        mitem.setOnActionExpandListener(object:MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {

                itemMyAccount.isVisible=false

                searchViewActionBar.isIconified=false
                return true
            }
            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {

                searchViewActionBar.isIconified=true
                itemMyAccount.isVisible=true

                searchViewActionBar.clearFocus()
                return true
            }
        })


        searchViewActionBar.setOnQueryTextListener(object:SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(p0: String?): Boolean {
                Log.i(MYTAG,"on qerry text submit $p0")
                if(!p0.isNullOrBlank()) viewModel.populateContactList(p0)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                Log.i(MYTAG,"on qerry text change $p0")
                //contactsAdapter.stringToColor=p0
                //viewModel.querryContactList(p0)
                viewModel.populateContactList(p0?:"")
                return true
            }

        })

        searchViewActionBar.setOnCloseListener{
            itemMyAccount.isVisible=true

            true
        }

        val mSearchCloseButton = resources.getIdentifier("android:id/search_close_btn", null, null)
        searchViewActionBar.findViewById<ImageView>(mSearchCloseButton).setOnClickListener{
            searchViewActionBar.setQuery("", false)

        }

    }


    override fun onStart() {
        super.onStart()
        if(checkForPermissions()){
                    if(shouldExportPhoneBook()) viewModel.getPhoneBook()
        }


    }




    @SuppressLint("ResourceType")
    private fun getColorForHighlightLetters():String{
       var colorStr="#000000"

        try {
             colorStr=getResources().getString(R.color.colorAccent)
        }
        catch (t:Throwable){
            Log.i(MYTAG, "can not get color as string, ${t.message}")

            viewModel.logStateOrErrorToMyServer(
                mapOf(
                    Pair("process","Main_Fragment (Contacts List)"),
                    Pair("state error","getColorForHighlightLetters() exception ${t.message} ")
                )
            )

        }
        return colorStr

    }

    private fun checkForPermissions():Boolean{

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        else {
            if (requireActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                /* if (.shouldShowRequestPermissionRationale(
                         thisActivity,
                         Manifest.permission.READ_CONTACTS
                     )
                 ) {
                     // Show an explanation to the user *asynchronously* -- don't block
                     // this thread waiting for the user's response! After the user
                     // sees the explanation, try again to request the permission.
                 } else {
                     // No explanation needed, we can request the permission.*/
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),MY_PERMISSIONS_REQUEST_READ_CONTACTS)
                return false
            }else return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    viewModel.populateContactList("")
                    //phonebook is exported only once from this fragment
                    if(shouldExportPhoneBook()) viewModel.getPhoneBook()
                } else {
                    showSnackBar(resources.getString(R.string.no_permission_read_contacts))
                }
                return
            }

            else -> {
                // Ignore all other requests.
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showSnackBar(s:String) {
        Snackbar.make(binding.root,s,Snackbar.LENGTH_INDEFINITE).setAction("OK") {}.show()
    }

    private fun hidekeyboard(){
        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.mainRootFrameLayout.windowToken, 0)
        Log.i(MYTAG, "hidekeyboard()")
    }

    override fun onStop() {
        super.onStop()
        hidekeyboard()
    }


}

