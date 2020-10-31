package com.example.damiantour.login.model

import androidx.databinding.BaseObservable

/**
 * @author: Ruben Naudts
 * Bevat de inhoud van de email en passwoord invulvelden.
 */
class LoginFields : BaseObservable() {

    private var email: String? = null
    private var password: String? = null

    fun getEmail(): String? {
        return email
    }

    fun setEmail(email: String?) {
        this.email = email
    }

    fun getPassword(): String? {
        return password
    }

    fun setPassword(password: String?) {
        this.password = password
    }
}