package com.example.damiantour.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentLoginBinding

/**
 * @author Ruben Naudts
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        if (savedInstanceState == null) {
            viewModel.init()
        }

        binding.loginViewModel = viewModel

        viewModel.getButtonClick()!!.observe(viewLifecycleOwner,
            { loginModel ->
                Toast.makeText(
                    context,
                    "Email " + loginModel.getEmail()
                        .toString() + ", Password " + loginModel.getPassword(),
                    Toast.LENGTH_SHORT
                ).show()
                view?.findNavController()?.navigate(R.id.action_loginFragment_to_mapFragment)
            })

        return binding.root
    }



}