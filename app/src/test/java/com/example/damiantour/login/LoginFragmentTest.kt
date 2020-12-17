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


@RunWith(Parameterized::class)
internal class LoginFragmentTest {
    @Rule
    @JvmField
    var mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var apiService: DamianApiService

    val loginFragment: LoginFragment = LoginFragment()
    private lateinit var context: Context


    @Test
    fun loginFieldsTest(){
        val loginFields = LoginFields()
        loginFields.setEmail("test@test.com")
        loginFields.setPassword("P@ssword1")

        assertEquals("test@test.com", loginFields.getEmail())
        assertEquals("P@ssword1", loginFields.getPassword())
    }


    @ParameterizedTest
    @MethodSource("provideLoginData")
    suspend fun sendLoginRequestTest(loginData: LoginData, expectedAssertion: Boolean){
        var loginData = loginData
        `when`(apiService.login(loginData)).then {
            provideApiLogin(loginData)
        }
        loginFragment.sendLoginRequest(loginData)
        val sharedPreferences = context.getSharedPreferences("damian-tours", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)
        assertEquals(expectedAssertion, token != "")
    }

    fun provideApiLogin(loginData: LoginData): String{
        var userData: HashMap<LoginData, String> = hashMapOf(
                LoginData("test@test.com", "testpass") to "poqzkdpqzodk",
                LoginData("jonas@test.com", "testpass") to "euhdfiszsef",
                LoginData("dante@test.com", "testpass") to "foisuhyfife",
        )

        var token = userData.getOrDefault(loginData, "")

        return token
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun provideLoginData(): Collection<Array<Any>>
        = listOf(
                arrayOf(LoginData("test@test.com", "testpass"), true),
                arrayOf(LoginData("test@test.com", "testtass"), false)
        )
    }
}