package com.example.vshcheglov.webshop.presentation.registration

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.text.InputType
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.extensions.isNetworkAvailable
import com.example.vshcheglov.webshop.presentation.helpres.Event
import com.example.vshcheglov.webshop.presentation.main.MainActivity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.android.synthetic.main.activity_register.*
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        viewModel = ViewModelProviders.of(this).get(RegisterViewModel::class.java)

        buttonRegisterUser.setOnClickListener {
            clearTextInputErrors()
            viewModel.registerUser(
                registerEmail.text.toString(), registerPassword.text.toString(),
                registerConfirmPassword.text.toString(), isNetworkAvailable()
            )
        }

        initViewModelObservers()

        setSupportActionBar(registerActionBar)
        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowTitleEnabled(false)
        }

        registerShowPasswordButton.setOnTouchListener { _, event ->
            showPassword(event, registerPassword)
            true
        }

        showConfirmPasswordButton.setOnTouchListener { _, event ->
            showPassword(event, registerConfirmPassword)
            true
        }
    }

    private fun clearTextInputErrors() {
        registerEmailTextInput.error = ""
        registerPasswordTextInput.error = ""
        registerConfirmPasswordTextInput.error = ""
    }

    private fun initViewModelObservers() {
        viewModel.liveDataShowEmailInvalid.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled { registerEmailTextInput.error = getString(R.string.email_error) }
            })
        viewModel.liveDataShowPasswordsNotMatch.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled {
                    registerConfirmPasswordTextInput.error = getString(R.string.passwords_not_match)
                }
            })
        viewModel.liveDataShowPasswordIsInvalid.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled {
                    registerPasswordTextInput.error = resources.getString(R.string.password_error)
                }
            })
        viewModel.showConfirmPasswordInvalid.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled {
                    registerConfirmPasswordTextInput.error = resources.getString(R.string.password_error)
                }
            })
        viewModel.liveDataShowNoInternet.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled { showNoInternetError() }
            })
        viewModel.liveDataStartMainScreen.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled { startMainActivity() }
            })
        viewModel.liveDataIsLoading.observe(this,
            Observer<Boolean> { isLoading ->
                setShowProgress(isLoading)
            })
        viewModel.liveDataRegistrationError.observe(this,
            Observer<Exception> { exception ->
                showLoginError(exception)
            })
    }

    private fun showPassword(event: MotionEvent, editText: EditText) { //TODO: Change icon when pressed
        when (event.action) {
            MotionEvent.ACTION_DOWN -> editText.inputType = InputType.TYPE_CLASS_TEXT
            MotionEvent.ACTION_UP -> editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun showLoginError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthUserCollisionException -> {
                resources.getString(R.string.email_address_collision)
            }
            else -> {
                resources.getString(R.string.unknown_error)
            }
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun showNoInternetError() {
        Snackbar.make(
            registerLinearLayout,
            resources.getString(R.string.no_internet_connection_warning),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun setShowProgress(isLoading: Boolean) {
        if (isLoading) {
            buttonRegisterUser.startAnimation()
        } else {
            buttonRegisterUser.revertAnimation()
        }
    }

    override fun onDestroy() {
        buttonRegisterUser.dispose()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }

        return false
    }
}
