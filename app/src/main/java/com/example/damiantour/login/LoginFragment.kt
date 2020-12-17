package com.example.damiantour.login

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.damiantour.R
import com.example.damiantour.databinding.FragmentLoginBinding
import com.example.damiantour.network.Connection
import com.example.damiantour.network.DamianApiService
import com.example.damiantour.network.model.LoginData
import kotlinx.coroutines.launch


/**
 * @author Ruben Naudts
 */
class LoginFragment : Fragment() {
    private lateinit var preferences: SharedPreferences

    private lateinit var binding: FragmentLoginBinding

    private lateinit var viewModel: LoginViewModel

    private val apiService : DamianApiService = DamianApiService.create()

    /**
     * @author Jonas Haenebalcke en Jordy Van Kerkvoorde
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireActivity().getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
        if(preferences.getString("TOKEN", null) != null){
            lifecycleScope.launch {
                navigateToStartRoute()
            }
        }
    }

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

        navigateOnNoConnection()

        viewModel.getButtonClick()!!.observe(viewLifecycleOwner,
            { loginModel ->
                lifecycleScope.launch {
                    val loginData = LoginData(
                        loginModel.getEmail().toString(),
                        loginModel.getPassword().toString()
                    )
                    sendLoginRequest(loginData)
                }
            })
        return binding.root
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        //if(preferences.getString("TOKEN", null) != null){
            lifecycleScope.launch {
                navigateToStartRoute()
            }
        //}
        super.onActivityCreated(savedInstanceState)
    }

    /**
     * @author: Ruben Naudts
     * @param loginData: a data class containing email and password fields
     * Sends login request to the API
     * On succes: saves the token in the SharedPreferences and navigates
     * On faillure: shows message
     */
     suspend fun sendLoginRequest(loginData: LoginData){
        try {
            //Execute API Login request
            val token =  "Bearer " + apiService.login(loginData)

            //Save token for later use.
            //val preferences : SharedPreferences = requireActivity().getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
            preferences.edit().putString("TOKEN",token).apply()

            //Code to request JWT token
            /*
            val preferences : SharedPreferences = requireActivity().getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
            val JWTtoken : String = preferences.getString("TOKEN", null).toString()
            */

            getUserData(token)
            //Navigate to map
            navigateToStartRoute()
        } catch (e: Exception){
            binding.loginErrorfield.text = getString(R.string.login_error)
            binding.loginErrorfield.visibility = View.VISIBLE
        }
    }

    /**
     * @author Jonas Haenebalcke en Jordy Van Kerkvoorde
     */
    private suspend fun navigateToStartRoute(){
        val token = preferences.getString("TOKEN", null)
        if(token!=null){
            try{
                val hasRegistration = apiService.isRegistered(token)
                var action = LoginFragmentDirections.actionLoginFragmentToStartRouteNotRegistered()
                if(hasRegistration){
                    action = LoginFragmentDirections.actionLoginFragmentToStartRouteSuccess()
                }
                view?.findNavController()?.navigate(action)
            }catch(e : Exception){
                println(e)
            }
        }
    }

    /**
     * @author Jonas Haenebalcke en Jordy Van Kerkvoorde
     */
    private fun navigateOnNoConnection(){
        val hasConnection = Connection.isOnline(requireContext())
        if(!hasConnection){
            findNavController().navigate(R.id.action_loginFragment_to_noConnection)
        }
    }

    private suspend fun getUserData(token: String){
        val profiledata = apiService.getProfile(token)
        val fullname = profiledata.firstName + " " + profiledata.lastName
        preferences.edit().putString("fullName",fullname).apply()
    }

}