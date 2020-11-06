package app.adinfinitum.ello.ui.fragment_about

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.MyAPI
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.database.MyDatabase
import app.adinfinitum.ello.databinding.FragmentAboutBinding
import app.adinfinitum.ello.ui.myapplication.MyApplication

private const val MYTAG="MY_ABOUT_FRAGMENT"
class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding
    private lateinit var viewModel: AboutFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Log.i(MYTAG, "ONLIFE ONCREATE")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(MYTAG, "ONLIFE ONCREATEVIEW")
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_about,container,false)


        val database= MyDatabase.getInstance(requireContext()).myDatabaseDao
        val apiService= MyAPI.retrofitService

        val repo= RepoContacts(requireActivity().contentResolver,
            database,
            apiService,
            resources.getString(R.string.mobile_app_version_header,(requireActivity().application as MyApplication).mobileAppVersion)
        )

        viewModel = ViewModelProvider(this, AboutFragmentViewModelFactory(repo,requireActivity().application))
            .get(AboutFragmentViewModel::class.java)

        binding.mobileAppVerTextView.text=String.format(resources.getString(R.string.mobile_app_version,getMobAppVersionFromApplication()))

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.webApiVersion.observe(viewLifecycleOwner, Observer {
            if(it!=null) {
                binding.webApiVerTextView.text=String.format(resources.getString(R.string.web_api_version,it))

            }

         })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
       menu.findItem(R.id.menu_item_share).isVisible=false
        menu.findItem(R.id.menu_item_myaccount).isVisible=false
        menu.findItem(R.id.aboutFragment).isVisible=false

    }


    private fun getMobAppVersionFromApplication():String {

        val myApp=requireActivity().application as MyApplication
        return myApp.mobileAppVersion

    }


    override fun onResume() {
        super.onResume()
        Log.i(MYTAG, "ONLIFE RESUME")
    }


    override fun onPause() {
        super.onPause()
        Log.i(MYTAG, "ONLIFE PAUSE")

    }

    override fun onStart() {
        super.onStart()
        Log.i(MYTAG, "ONLIFE START")
    }

    override fun onStop() {
        super.onStop()
        Log.i(MYTAG, "ONLIFE STOP")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(MYTAG, "ONLIFE ONDESTROYVIEW")
    }

}
