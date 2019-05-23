package com.example.vshcheglov.webshop


import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.example.vshcheglov.webshop.matchers.hasTextInputLayoutErrorText
import com.example.vshcheglov.webshop.presentation.login.LoginActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule

class LoginScreenUITest {

    val INCORRECT_EMAIL = "some error"
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Rule
    @JvmField
    // third parameter is set to false which means the activity is not started automatically
    var activityRule: ActivityTestRule<LoginActivity> = ActivityTestRule(LoginActivity::class.java, false, false)

    @Test
    fun test_IfPrintedCorrectErrorMessage_AfterTypingIncorrectPassword() {
        activityRule.launchActivity(Intent())

        Espresso.onView(withId(R.id.loginEmail))
            .perform(clearText())
            .perform(typeText(INCORRECT_EMAIL))

        Espresso.closeSoftKeyboard()

        Espresso.onView(withId(R.id.orderButton))
            .perform(click())

        Espresso.onView(withId(R.id.emailTextInput))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.email_error))))
    }
}
