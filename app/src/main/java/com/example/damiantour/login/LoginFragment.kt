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
import com.example.damiantour.network.DamianApiService
import com.example.damiantour.network.LoginData
import kotlinx.coroutines.launch
import java.lang.Exception


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
                lifecycleScope.launch {
                    println("pre request")
                    val loginData = LoginData(
                        loginModel.getEmail().toString(),
                        loginModel.getPassword().toString()
                    )
                    sendLoginRequest(loginData)
                }
            })

        return binding.root
    }

    /**
     * @author: Ruben Naudts
     * Verstuurt de login request naar de API. Toont een melding wanneer de login faalt
     */
    private suspend fun sendLoginRequest(loginData: LoginData){
        try {
            //TODO: save token for later use.
            val token =  apiService.login(loginData)
            Log.i("LoginFragment", token)
            view?.findNavController()?.navigate(R.id.action_loginFragment_to_mapFragment)
        } catch (e : Exception){
            binding.loginErrorfield.text = getString(R.string.login_error)
            binding.loginErrorfield.visibility = View.VISIBLE
            //Toast.makeText(context, "Deze login bestaat niet", Toast.LENGTH_SHORT).show()
        }
    }

}