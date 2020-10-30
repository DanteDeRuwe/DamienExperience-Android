package com.example.damiantour.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentLoginBinding
import com.example.damiantour.login.model.LoginFields
import com.example.damiantour.network.DamianApiService
import com.example.damiantour.network.LoginData
import kotlinx.coroutines.launch

/**
 * @author Ruben Naudts
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private lateinit var viewModel: LoginViewModel

    private val apiService : DamianApiService = DamianApiService.create()

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

                lifecycleScope.launch{
                    println("pre request")
                    val loginData = LoginData(loginModel.getEmail().toString(), loginModel.getPassword().toString())
                    val result = sendLoginRequest(loginData)
                    println(result.toString())
                    //Log.i("LoginFragment", "token: "+ result.toString())
                }

                view?.findNavController()?.navigate(R.id.action_loginFragment_to_mapFragment)
            })

        return binding.root
    }

    suspend fun sendLoginRequest(loginData: LoginData){
        return apiService.login(loginData)
        //Log.i("LoginFragment","na sendloginRequest "+ loginData.email)
    }

}