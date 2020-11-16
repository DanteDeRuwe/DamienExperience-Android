package com.example.damiantour.stopRoute

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.damiantour.R

/**
 * @author: Ruben Naudts & Jordy Van Kerkvoorde <3
 */
class StoppedRouteFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stopped_route, container, false)
    }


}