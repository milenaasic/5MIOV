package com.vertial.sipdnidphone.ui.emty_logo_fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.vertial.sipdnidphone.R

/**
 * A simple [Fragment] subclass.
 */
class EmptyLogoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_empty_logo, container, false)
    }


}
