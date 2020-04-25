package com.vertial.fivemiov.ui.fragment_dial_pad


import android.Manifest
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentDialPadBinding
import com.vertial.fivemiov.ui.fragment_main.MainFragment
import com.vertial.fivemiov.utils.isValidPhoneNumber


private val MYTAG="MY_DialPadFragment"
class DialPadFragment : Fragment() {

    private lateinit var binding: FragmentDialPadBinding
    private lateinit var viewModel: DialpadFragmViewModel
    private lateinit var myPrenumber:String
    private lateinit var clipboard:ClipboardManager

    companion object{
        val MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL=11
        val FORMATTING_COUNTRY_CODE="US"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_dial_pad,container,false)

        val database=MyDatabase.getInstance(requireContext()).myDatabaseDao
        val apiService=MyAPI.retrofitService
        val repo=RepoContacts(requireActivity().contentResolver,database,apiService)

        /*val myApp=requireActivity().application as MyApplication
        val myAppContanier=myApp.myAppContainer*/


        viewModel = ViewModelProvider(this, DialpadFragmentViewModelFactory(repo,requireActivity().application))
            .get(DialpadFragmViewModel::class.java)

        binding.apply {

            button1.setOnClickListener { appendDigit((it as Button).text) }
            button2.setOnClickListener { appendDigit((it as Button).text) }
            button3.setOnClickListener { appendDigit((it as Button).text) }
            button4.setOnClickListener { appendDigit((it as Button).text) }
            button5.setOnClickListener { appendDigit((it as Button).text) }
            button6.setOnClickListener { appendDigit((it as Button).text) }
            button7.setOnClickListener { appendDigit((it as Button).text) }
            button8.setOnClickListener { appendDigit((it as Button).text) }
            button9.setOnClickListener { appendDigit((it as Button).text) }
            button10.setOnClickListener { appendDigit((it as Button).text) }
            button11.setOnClickListener { appendDigit((it as Button).text) }
            //brisanje
            button12.setOnClickListener { deleteDigit() }
            //poziv
            button13.setOnClickListener { if(checkForPermissions()) makePhoneCall()}

         }


         binding.editTextEnterNumber.apply {
             setShowSoftInputOnFocus(false)
             addTextChangedListener(PhoneNumberFormattingTextWatcher(FORMATTING_COUNTRY_CODE))

          }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.myPrenumber.observe(viewLifecycleOwner, Observer {
            if(it!=null) myPrenumber=it
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        clipboard= requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun onStart() {
        super.onStart()
        //requireActivity().actionBar?.setBackgroundDrawable(resources.getDrawable(android.R.color.transparent,null))
    }

    private fun appendDigit(char:CharSequence){
        binding.editTextEnterNumber.apply {
            text.append(char)
            isCursorVisible=true
         }
    }

    private fun deleteDigit(){
        val charCount:Int = binding.editTextEnterNumber.length()
        if (charCount == 1) {
            binding.editTextEnterNumber.text.delete(charCount - 1, charCount)
            binding.editTextEnterNumber.isCursorVisible=false
        }
        if(charCount>1) binding.editTextEnterNumber.text.delete(charCount - 1, charCount)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dial_pad_menu,menu)
        menu.findItem(R.id.dialPadFragment).isVisible=false
        menu.findItem(R.id.menu_item_myaccount).isVisible=false
        menu.findItem(R.id.menu_item_logout).isVisible=false
        menu.findItem(R.id.aboutFragment).isVisible=false

        //da li je Paste item visible
        menu.findItem(R.id.menu_item_paste).isEnabled=
                // This disables the paste menu item, since the clipboard has data but it is not plain text
            when {
            !clipboard.hasPrimaryClip() -> false
            !(clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN))!! -> false
            else -> true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId){
            R.id.menu_item_paste->{
                pastePhoneNumber()
                return true
            }
        else->return super.onOptionsItemSelected(item)
        }
    }

    private fun pastePhoneNumber() {
        val item = clipboard.primaryClip?.getItemAt(0)
        val pasteData:CharSequence? = item?.text
        if(pasteData!=null &&(pasteData.toString().isValidPhoneNumber())){
            binding.editTextEnterNumber.text=Editable.Factory.getInstance().newEditable(pasteData)
        } else showSnackBar(resources.getString(R.string.clipboard_invalid_data_type))
    }

    private fun checkForPermissions():Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        else {
            if (requireActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL)
                    return false
            } else return true
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            MainFragment.MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    makePhoneCall()
                } else {
                    showSnackBar(resources.getString(R.string.no_permission_make_phone_call))
                }
                return
            }

            else -> { }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    private fun makePhoneCall() {
        //TODO NAPRAVI FUNKCIJU ZA NORMALIZACIJU , DODAVANJE #
        //TODO PROVERI DA LI IMA RESOLVE ACTIVITY ZA PHONE CALL
        val phone = PhoneNumberUtils.normalizeNumber(binding.editTextEnterNumber.text.toString())
        Log.i(MYTAG, "normalizovan broj je $phone")
        if (phone.isValidPhoneNumber()) {
            val intentToCall = Intent(Intent.ACTION_CALL).apply {
                setData(Uri.parse(resources.getString(R.string.prenumber_call,myPrenumber,phone)))
                Log.i(MYTAG, "uri je tel:$myPrenumber,$phone ")
            }

            if (intentToCall.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intentToCall)
            } else showSnackBar(resources.getString(R.string.unable_to_make_call))

        } else showSnackBar(resources.getString(R.string.not_valid_phone_number))

    }



    private fun showSnackBar(s:String) {
        Snackbar.make(binding.root,s, Snackbar.LENGTH_LONG).show()
    }

}
