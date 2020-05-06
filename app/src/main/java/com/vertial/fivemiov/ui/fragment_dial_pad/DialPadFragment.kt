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
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentDialPadBinding
import com.vertial.fivemiov.ui.fragment_main.MainFragment
import com.vertial.fivemiov.utils.isVOIPsupported
import com.vertial.fivemiov.utils.isValidPhoneNumber
import kotlinx.android.synthetic.main.fragment_dial_pad.*


private val MYTAG="MY_DialPadFragment"
class DialPadFragment : Fragment() {

    private lateinit var binding: FragmentDialPadBinding
    private lateinit var viewModel: DialpadFragmViewModel
    private lateinit var myPrenumber:String
    private lateinit var clipboard:ClipboardManager
    private var currentCursorPosition=0

    companion object{
        val MY_PERMISSIONS_REQUEST_MAKE_CALL_and_SIP_and_AUDIO_DIALPAD=11
        val FORMATTING_COUNTRY_CODE="US"
        val NOTHING=""

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

            // prenumber poziv
            buttonPrenumberCallDialpad.setOnClickListener { if(checkForPermissions()) {
                                                                                        binding.editTextEnterNumber.isCursorVisible=false
                                                                                        makePhoneCall()
                                                                                        }
                                                            }

            // sip poziv
            buttonSipCallDialpadFrag.setOnClickListener {

                binding.editTextEnterNumber.isCursorVisible=false

                if(isVOIPsupported(requireContext())){
                        enableCallButtons(false)
                        val phoneNumber=binding.editTextEnterNumber.text.toString()
                        val normPhoneNumber=PhoneNumberUtils.normalizeNumber(phoneNumber)
                        if(checkForPermissions()) {
                            if(normPhoneNumber.isValidPhoneNumber()) findNavController().
                                navigate(DialPadFragmentDirections.actionDialPadFragmentToSipFragment(normPhoneNumber,normPhoneNumber))
                            else {
                                showSnackBar(resources.getString(R.string.not_valid_phone_number))
                                enableCallButtons(true)
                            }
                        }

                } else  showSnackBar(resources.getString(R.string.VOIP_not_supported))

             }

         }


         binding.editTextEnterNumber.apply {
             setShowSoftInputOnFocus(false)
             addTextChangedListener(PhoneNumberFormattingTextWatcher(FORMATTING_COUNTRY_CODE))
             accessibilityDelegate=object : View.AccessibilityDelegate() {
                 override fun sendAccessibilityEvent(host: View?, eventType: Int) {
                     super.sendAccessibilityEvent(host, eventType)
                     if(eventType==AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
                       val start=binding.editTextEnterNumber.selectionStart
                       val end=binding.editTextEnterNumber.selectionEnd
                       if(start==end)
                         Log.i(MYTAG, " accessibility event TYPE_VIEW_TEXT_SELECTION_CHANGED, start $start, end $end")
                         currentCursorPosition=start

                     }
                 }
             }

          }

          binding.editTextEnterNumber.setOnClickListener {
              Log.i(MYTAG, "duzina u edit tekstu je ${binding.editTextEnterNumber.length()} ")
              if(binding.editTextEnterNumber.length()!=0) binding.editTextEnterNumber.isCursorVisible=true
           }

       // if(!isVOIPsupported(requireContext())) showSnackBar(resources.getString(R.string.VOIP_not_supported))
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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

            text.insert(this.getSelectionStart(), char)
            //text.append(char)
            isCursorVisible=true
         }
    }

    private fun deleteDigit(){

        val charCount:Int = binding.editTextEnterNumber.length()
        Log.i(MYTAG, " char count je $charCount")
        Log.i(MYTAG, " pozicija je $currentCursorPosition")
        when (charCount){
            0->{}
            1->{ binding.editTextEnterNumber.text.delete(charCount - 1, charCount)
                binding.editTextEnterNumber.isCursorVisible=false}
            else->{
                if(currentCursorPosition!=0 && charCount>=currentCursorPosition)  binding.editTextEnterNumber.text.delete(currentCursorPosition - 1, currentCursorPosition)
            }

        }

       /* if (charCount == 1) {
            binding.editTextEnterNumber.text.delete(charCount - 1, charCount)
            binding.editTextEnterNumber.isCursorVisible=false
        }
        if(charCount>1) binding.editTextEnterNumber.text.delete(charCount - 1, charCount)*/
    }

    private fun enableCallButtons(b:Boolean){
        if(b) {
                binding.buttonPrenumberCallDialpad.isEnabled=true
                binding.buttonSipCallDialpadFrag.isEnabled=true
        }else {
            binding.buttonPrenumberCallDialpad.isEnabled=false
            binding.buttonSipCallDialpadFrag.isEnabled=false
        }

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
            binding.editTextEnterNumber.apply {
                text=Editable.Factory.getInstance().newEditable(pasteData)
                setSelection(pasteData.lastIndex+1)

            }

        } else showSnackBar(resources.getString(R.string.clipboard_invalid_data_type))
    }

    private fun checkForPermissions():Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        else {
            if (requireActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                requireActivity().checkSelfPermission(Manifest.permission.USE_SIP) != PackageManager.PERMISSION_GRANTED ||
                requireActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE,Manifest.permission.USE_SIP,Manifest.permission.RECORD_AUDIO),
                    MY_PERMISSIONS_REQUEST_MAKE_CALL_and_SIP_and_AUDIO_DIALPAD
                )
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
            MY_PERMISSIONS_REQUEST_MAKE_CALL_and_SIP_and_AUDIO_DIALPAD-> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()) {

                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.i(MYTAG,"grantResults 0 je ${grantResults[0]}")
                    } else {
                        showSnackBar(resources.getString(R.string.no_permission_make_phone_call))
                    }


                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        Log.i(MYTAG,"grantResults 1 je ${grantResults[1]}")
                    } else {
                        showSnackBar(resources.getString(R.string.no_SIP_permission))
                    }

                    if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                        Log.i(MYTAG,"grantResults 2 audio je ${grantResults[1]}")
                    } else {
                        showSnackBar(resources.getString(R.string.no_audio_permission))
                    }
                }

                return
            }

            else -> { }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    private fun makePhoneCall() {

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
        Snackbar.make(binding.coordLayDialpadFragment,s, Snackbar.LENGTH_INDEFINITE).setAction("OK"){}.show()
    }

}
