package com.allow.food4needy.common

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class PlaceHolderFragment : Fragment() {

    companion object {
        const val ARG = "arg"
        fun newInstance(text: String): PlaceHolderFragment {
            val bundle = Bundle()
            bundle.putString(ARG, text)
            val fragment = PlaceHolderFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return TextView(inflater.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view as TextView).text = arguments?.getString(ARG)
    }
}