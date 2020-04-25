package com.vertial.fivemiov.ui.fragment_about

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil

import com.vertial.fivemiov.R
import com.vertial.fivemiov.databinding.FragmentAboutBinding


class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_about,container,false)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.dialPadFragment).isVisible=false
        menu.findItem(R.id.menu_item_myaccount).isVisible=false
        menu.findItem(R.id.menu_item_logout).isVisible=false
        menu.findItem(R.id.aboutFragment).isVisible=false


    }



}
