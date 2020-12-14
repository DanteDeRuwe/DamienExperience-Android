package com.example.damiantour
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.damiantour.databinding.ActivityMainBinding
import com.example.damiantour.network.NetworkConnection


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
}