package com.example.damiantour.login

import android.util.Log
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.damiantour.login.model.LoginFields

/**
 * @author: Ruben Naudts
 */
class LoginViewModel : ViewModel(){

    private var login : LoginFields? = null
    private val buttonClick = MutableLiveData<LoginFields>()

    fun init() {
        login = LoginFields()
    }

    fun getLogin(): LoginFields? {
        return login
    }

    fun login() {
        buttonClick.setValue(login)

    }

    fun getButtonClick(): MutableLiveData<LoginFields>? {
        return buttonClick
    }

    fun forgotPassword(){

    }

}