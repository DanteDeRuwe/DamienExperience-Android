package com.example.damiantour.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentLoginBinding
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * @author Ruben
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
        binding.loginViewModel = viewModel

        binding.loginButton.setOnClickListener{
            //TODO feedback week 5: hier zou je dan de viewModel moeten laten weten dat er op een button geklikt is. Dus het eevent doorgeven aan de viewmodel
            //Doe tis zodra we login opnemen
            viewModel.login(binding.emailInput.text.toString(), binding.passwordInput.text.toString())
            Toast.makeText(context, "Login gebeurd", Toast.LENGTH_SHORT).show()
            view?.findNavController()?.navigate(R.id.action_loginFragment_to_mapFragment)
        }

        return binding.root
    }



}