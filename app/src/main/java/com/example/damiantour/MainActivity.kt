package com.example.damiantour
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.damiantour.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    //databinding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //set the view of the activity
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
}