package com.example.damiantour.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentLoginBinding
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * @author Ruben
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    //private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login,
            container,
            false
        )

        //viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        //binding.LoginViewModel = viewModel

        binding.setLifecycleOwner(this)

        binding.loginButton.setOnClickListener{
            login()
        }

        return binding.root
    }

    private fun login(){
        val email = email_input.text.toString()
        val pw = password_input.text.toString()
        if(email=="ruben.naudts@student.hogent.be"&&pw=="testpass"){
            Toast.makeText(context, "Login gebeurd", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(context, "Login ging mis", Toast.LENGTH_SHORT).show()
        }
    }


}