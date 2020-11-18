package com.example.damiantour.sos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.damiantour.R
import com.google.android.material.bottomnavigation.BottomNavigationView


/***
 * @author DanteDeRuwe
 */
class SosFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_sos, container, false)
        val bottomNavigationView : BottomNavigationView = root.findViewById(R.id.nav_bar)
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)

        val callOrganizationBtn : Button = root.findViewById(R.id.call_organisation_btn)
        val call112Btn : Button = root.findViewById(R.id.call_112_btn)
        val open112AppImgBtn : ImageButton = root.findViewById(R.id.open_112_app_imgbtn)
        val open112AppBtn : Button = root.findViewById(R.id.open_112_app_btn)

        callOrganizationBtn.setOnClickListener { openDialer(getString(R.string.organisation_phonenumber))}
        call112Btn.setOnClickListener { openDialer(getString(R.string.sos_phonenumber))}
        open112AppBtn.setOnClickListener{openSosApp()}
        open112AppImgBtn.setOnClickListener{openSosApp()}

        return root
    }

    private fun openSosApp() {
        val appPackageName: String = getString(R.string._112_app_name)
        try{
            val pm = requireContext().packageManager
            val intent: Intent? = pm.getLaunchIntentForPackage(appPackageName)
            intent?.addCategory(Intent.CATEGORY_LAUNCHER)
            requireContext().startActivity(intent)

        }catch(exception: Exception){
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    private fun openDialer(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        startActivity(intent)
    }
}