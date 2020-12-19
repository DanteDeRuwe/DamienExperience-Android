package com.example.damiantour.login

import android.content.Context
import com.example.damiantour.login.model.LoginFields
import com.example.damiantour.network.DamianApiService
import com.example.damiantour.network.model.LoginData
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.util.stream.Stream

internal class LoginFragmentTest {

    @Test
    fun loginFieldsTest(){
        val loginFields = LoginFields()
        loginFields.setEmail("test@test.com")
        loginFields.setPassword("P@ssword1")

        assertEquals("test@test.com", loginFields.getEmail())
        assertEquals("P@ssword1", loginFields.getPassword())
    }
}