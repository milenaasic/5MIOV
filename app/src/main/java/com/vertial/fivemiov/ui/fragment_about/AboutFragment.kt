package com.vertial.fivemiov.ui.fragment_about

import android.content.pm.PackageInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.vertial.fivemiov.R
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentAboutBinding
import com.vertial.fivemiov.ui.fragment_dial_pad.DialpadFragmViewModel
import com.vertial.fivemiov.ui.fragment_dial_pad.DialpadFragmentViewModelFactory

private const val MYTAG="MY_AboutFragment"
class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding
    private lateinit var viewModel: AboutFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_about,container,false)

        val database= MyDatabase.getInstance(requireContext()).myDatabaseDao

        viewModel = ViewModelProvider(this, AboutFragmentViewModelFactory(database,requireActivity().application))
            .get(AboutFragmentViewModel::class.java)

        binding.mobileAppVerTextView.text=String.format(resources.getString(R.string.mobile_app_version,getMobAppVersion()))

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.webApiVersion.observe(viewLifecycleOwner, Observer {
            if(it!=null) {
                Log.i(MYTAG," webapi iz baze je $it")
                binding.webApiVerTextView.text=String.format(resources.getString(R.string.web_api_version,it))

            }

         })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.dialPadFragment).isVisible=false
        menu.findItem(R.id.menu_item_myaccount).isVisible=false
        menu.findItem(R.id.aboutFragment).isVisible=false

    }

    private fun getMobAppVersion():String{

        var myversionName=""
        var versionCode=-1L

            try {
                val packageInfo:PackageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0);
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
