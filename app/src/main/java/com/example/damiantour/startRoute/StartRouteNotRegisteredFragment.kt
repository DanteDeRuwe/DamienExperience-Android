package com.example.damiantour.startRoute

import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentStartRouteNotRegisteredBinding


/**
 * @author Jonas Haenebalcke en Jordy Van Kerkvoorde
 */
class StartRouteNotRegisteredFragment : Fragment() {

    private lateinit var binding: FragmentStartRouteNotRegisteredBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentStartRouteNotRegisteredBinding.inflate(inflater, container, false)
        binding.registerButton.setOnClickListener(View.OnClickListener {
            navigateToWeb()
        })

        return binding.root
    }

    fun navigateToWeb(){
        val packageManager = activity?.packageManager!!
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, "https://damianexperience.netlify.app")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }


}