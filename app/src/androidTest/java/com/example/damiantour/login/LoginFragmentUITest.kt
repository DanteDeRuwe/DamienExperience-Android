package com.example.damiantour.login

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.damiantour.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.damiantour.R
import org.junit.Assert

@RunWith(AndroidJUnit4::class)
internal class LoginFragmentUITest{
    private var email = "test@test.be"
    private var password = "testpass"

    @Test fun testEventFragment() {
        val scenario = launchFragmentInContainer<LoginFragment>()
        onView(withId(R.id.email_input)).perform(typeText(email), closeSoftKeyboard())
        onView(withId(R.id.password_input)).perform(typeText(password), closeSoftKeyboard())

        scenario.onFragment { fragment ->
           val fields =  fragment.getLoginFields()
            Assert.assertEquals(email,fields.getEmail())
            Assert.assertEquals(password,fields.getPassword())
        }

    }

}