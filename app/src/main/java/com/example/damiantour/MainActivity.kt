package com.example.damiantour
import android.content.Intent
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.damiantour.databinding.ActivityMainBinding
import com.example.damiantour.network.NetworkConnection
import com.google.zxing.integration.android.IntentIntegrator


class MainActivity : AppCompatActivity() {
    //databinding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //set the view of the activity
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val networkConnection = NetworkConnection(applicationContext)
        networkConnection.observe(this, Observer { isConnected ->
            if(isConnected){
                print("----------------------------------------------------------------------------")
                print("CONNECTED")
                print("----------------------------------------------------------------------------")
            }else{
                print("----------------------------------------------------------------------------")
                print("NOT CONNECTED")
                print("----------------------------------------------------------------------------")
            }
        })
    }

    override fun onBackPressed() {
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

    /**
     * @author Jonas Haenebalcke & Lucas Van der Haegen
     */
    fun scanQr() {
        val integrator = IntentIntegrator(this)
        integrator.setBeepEnabled(false)
//        integrator.setOrientationLocked(false)
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
//        integrator.setPrompt("Scan a barcode");
        integrator.initiateScan();
    }

    /**
     * @author Jonas Haenebalcke
     * source https://www.youtube.com/watch?v=NqFVqLqbw_g&ab_channel=CodeAndroid
     */

    //Result of scanning qr code
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
//                    Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                    openLink(result.contents)
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun openLink(url: String) {
        if (URLUtil.isValidUrl(url)) {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            this.startActivity(intent)
        } else {
            Toast.makeText(this, "This is not a valid link!", Toast.LENGTH_LONG).show()
        }
    }
}