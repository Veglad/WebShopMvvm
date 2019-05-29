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

        viewModel.stateLiveData.observe(this, Observer { state -> updateUi(state) })
        viewModel.commandLiveData.observe(this, Observer { commandEvent ->
            commandEvent.getContentIfNotHandled()?.let { command -> performCommand(command) }
        })

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

    private fun performCommand(command: RegisterCommand) {
        when (command) {
            is RegisterCommand.StartMainScreen -> startMainActivity()
            is RegisterCommand.ShowEmailInvalid -> registerEmailTextInput.error = getString(R.string.email_error)
            is RegisterCommand.ShowPasswordsNotMatch -> {
                registerConfirmPasswordTextInput.error = getString(R.string.passwords_not_match)
            }
            is RegisterCommand.ShowPasswordInvalid -> {
                registerPasswordTextInput.error = resources.getString(R.string.password_error)
            }
            is RegisterCommand.ShowConfirmPasswordInvalid -> {
                registerConfirmPasswordTextInput.error = resources.getString(R.string.password_error)
            }
            is RegisterCommand.ShowNoInternet -> showNoInternetError()
            is RegisterCommand.ShowRegisterError -> showLoginError(command.exception)
        }
    }

    private fun updateUi(state: RegisterViewState) {
        setShowProgress(state.isLoading)
    }

    private fun clearTextInputErrors() {
        registerEmailTextInput.error = ""
        registerPasswordTextInput.error = ""
        registerConfirmPasswordTextInput.error = ""
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
