package com.vertial.fivemiov.ui.fragment_main


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentMainLinLayoutBinding
import com.vertial.fivemiov.model.ContactItem
import com.vertial.fivemiov.ui.main_activity.MainActivity
import com.vertial.fivemiov.ui.main_activity.MainActivity.Companion.MAIN_ACTIVITY_SHARED_PREF_NAME
import com.vertial.fivemiov.ui.main_activity.MainActivity.Companion.PHONEBOOK_IS_EXPORTED
import com.vertial.fivemiov.ui.main_activity.MainActivityViewModel
import com.vertial.fivemiov.utils.EMPTY_EMAIL
import com.vertial.fivemiov.utils.EMPTY_NAME
import com.vertial.fivemiov.utils.isOnline
import kotlinx.android.synthetic.main.activity_main.*

private val MYTAG="MY_MAIN_FRAGMENT"



class MainFragment : Fragment(){

    private lateinit var binding: FragmentMainLinLayoutBinding
    private lateinit var viewModel:MainFragmentViewModel
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

        val database= MyDatabase.getInstance(requireContext()).myDatabaseDao
        val apiService= MyAPI.retrofitService
        val repo=RepoContacts(requireActivity().contentResolver,database,apiService)

        /*val myApp=requireActivity().application as MyApplication
        val myAppContanier=myApp.myAppContainer*/

        viewModel = ViewModelProvider(this, MainFragmentViewModelFactory(repo,requireActivity().application))
            .get(MainFragmentViewModel::class.java)


        if(checkForPermissions()) {
               initalizeAdapter()
               if(shouldExportPhoneBook()) (requireActivity() as MainActivity).exportPhoneBook()

        }

        /*binding.setEmailAndPassButton.setOnClickListener{
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToSetEmailAndPasswordFragment())
        }*/

        return binding.root
    }


    private fun shouldExportPhoneBook(): Boolean {
       val sharedPreferences= requireActivity().getSharedPreferences(MAIN_ACTIVITY_SHARED_PREF_NAME,Context.MODE_PRIVATE)

       if(sharedPreferences.contains(PHONEBOOK_IS_EXPORTED)){
            val isExported=sharedPreferences.getBoolean(PHONEBOOK_IS_EXPORTED,false)
           Log.i(MYTAG," usao u ima phoneBookIsExported promenljiva i vrednost je $isExported")
            if(!isExported) return true
            else return false

       }else{
            sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED,false).apply()
           Log.i(MYTAG," nema phoneBookIsExported promenljive i sada je napravljena")
           return true
       }

    }

    private fun initalizeAdapter(){

        contactsAdapter=MainFragmentAdapter(ContactItemClickListener {
            if(it.name== EMPTY_NAME){
            }else{
                val action=MainFragmentDirections.actionMainFragmentToDetailContact(it.lookUpKey,it.name)
                findNavController().navigate(action)}
        },getColorForHighlightLetters().toString())


        binding.mainFregmRecViewLinLayout.setHasFixedSize(true)
        binding.mainFregmRecViewLinLayout.adapter=contactsAdapter

       // val list= createFakeContactList()
        //contactsAdapter.dataList= list

        //inicijalizacija liste
        viewModel.populateContactList("")
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*viewModel.userData.observe(viewLifecycleOwner, Observer {user->
            if(user!=null) {
                Log.i(MYTAG, " user je $user")
                if (user.userEmail == EMPTY_EMAIL) binding.setEmailAndPassButton.visibility =
                    View.VISIBLE
                else binding.setEmailAndPassButton.visibility = View.GONE
            }
        })*/

        viewModel.contactList.observe(viewLifecycleOwner, Observer {list->

            if(list!=null) contactsAdapter.dataList=list

         })

        viewModel.numberOfSelectedContacts.observe(viewLifecycleOwner, Observer {
            if(it!=null) binding.nbOfContactsTextView.text=String.format(resources.getString(R.string.nb_of_contacts_found,it))

         })

        viewModel.currentSearchString.observe(viewLifecycleOwner, Observer {
            if(it!=null) contactsAdapter.stringToColor=it

        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.main_fragment_menu,menu)
        val mitem=menu.findItem(R.id.menu_item_search)
        val itemMyAccount= menu.findItem(R.id.menu_item_myaccount)
        //val itemDialPad=menu.findItem(R.id.dialPadFragment)
        val itemAboutFragment=menu.findItem(R.id.aboutFragment)
        searchViewActionBar=menu.findItem(R.id.menu_item_search).actionView as SearchView
        searchViewActionBar.setQueryHint(getString(R.string.search_hint))


        mitem.setOnActionExpandListener(object:MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                Log.i(MYTAG,"on search expand listener")
                itemMyAccount.isVisible=false
               // itemDialPad.isVisible=false
                itemAboutFragment.isVisible=false
                searchViewActionBar.isIconified=false
                return true
            }
            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                Log.i(MYTAG,"on search collapse listener")
                searchViewActionBar.isIconified=true
                itemMyAccount.isVisible=true
               // itemDialPad.isVisible=true
                itemAboutFragment.isVisible=true
                searchViewActionBar.clearFocus()
                return true
            }
        })


        searchViewActionBar.setOnQueryTextListener(object:SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(p0: String?): Boolean {
                Log.i(MYTAG,"on qerry text submit $p0")
                if(!p0.isNullOrBlank()) viewModel.populateContactList(p0?:"")
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                Log.i(MYTAG,"on qerry text change $p0")
                //viewModel.cancelOngoingJob()
                viewModel.populateContactList(p0)
                return true
            }

        })

        searchViewActionBar.setOnCloseListener{
            itemMyAccount.isVisible=true
            //itemDialPad.isVisible=true
            Log.i(MYTAG,"on search collapse listener")
            true
        }

        val mSearchCloseButton = resources.getIdentifier("android:id/search_close_btn", null, null)
        searchViewActionBar.findViewById<ImageView>(mSearchCloseButton).setOnClickListener{
            searchViewActionBar.setQuery("", false)

        }

    }


    override fun onStart() {
        super.onStart()
        getE1Prenumber()
        Log.i(MYTAG, "ON START")
        //proba rasinog querry-ja
        //viewModel.getContactsWithInternationalNumbers()

    }


    private fun getE1Prenumber() {
        if(isOnline(requireActivity().application)) viewModel.getE1PrenumberIf24hPassed()
    }



    private fun getColorForHighlightLetters():String{
        var color=0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            color= resources.getColor(R.color.colorAccent)
        }else{
            color= resources.getColor(R.color.colorAccent,null)

        }
        Log.i(MYTAG, "boja je ${color.toUInt().toString(16)}")
        return "#${color.toUInt().toString(16)}"

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
                   initalizeAdapter()
                    if(shouldExportPhoneBook()) (requireActivity() as MainActivity).exportPhoneBook()
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
        Snackbar.make(binding.root,s,Snackbar.LENGTH_LONG).show()
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

