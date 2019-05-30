package com.example.vshcheglov.webshop.presentation.login

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.extensions.canUseFingerprint
import com.example.vshcheglov.webshop.extensions.getFingerprintSensorState
import com.example.vshcheglov.webshop.extensions.isNetworkAvailable
import com.example.vshcheglov.webshop.presentation.helpers.Router
import com.example.vshcheglov.webshop.presentation.helpres.FingerprintState
import com.example.vshcheglov.webshop.presentation.helpres.MainThreadExecutor
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by lazy {
        ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel.stateLiveData.observe(this, Observer { state -> updateUi(state) })
        viewModel.commandLiveData.observe(this, Observer { commandEvent ->
            commandEvent.getContentIfNotHandled()?.let { command -> performCommand(command) }
        })

        orderButton.setOnClickListener {
            emailTextInput.error = ""
            passwordTextInput.error = ""
            viewModel.logInUser(
                loginEmail.text.toString(),
                loginPassword.text.toString(), isNetworkAvailable()
            )
        }

        buttonRegister.setOnClickListener { Router.showRegister(this) }
        useFingerprintButton.setOnClickListener { prepareBiometricPrompt() }
        showPasswordButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> loginPassword.inputType = InputType.TYPE_CLASS_TEXT
                MotionEvent.ACTION_UP -> loginPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            true
        }
    }

    private fun performCommand(command: LoginCommand) {
        when (command) {
            is LoginCommand.ShowEmailInvalid -> emailTextInput.error = getString(R.string.email_error)
            is LoginCommand.ShowPasswordInvalid -> passwordTextInput.error = getString(R.string.password_error)
            is LoginCommand.ShowNoInternet -> showMessage(getString(R.string.no_internet_connection_warning))
            is LoginCommand.StartMainScreen -> Router.showMain(this)
            is LoginCommand.ShowBiometricError -> showMessage(getString(R.string.biometric_error))
            is LoginCommand.ShowNewBiometricEnrolled -> showMessage(getString(R.string.biometric_enrolled_error_text))
            is LoginCommand.HideBiometricPrompt -> hideBiometricPromptFeature()
            is LoginCommand.ShowLoginError -> showLoginError(command.exception)
            is LoginCommand.ShowBiometricPrompt -> showBiometricPrompt(command.cryptoObject)
        }
    }

    private fun updateUi(state: LoginViewState) {
        setShowProgress(state.isLoading)
        loginEmail.setText(state.userEmail, TextView.BufferType.EDITABLE)
    }

    private fun prepareBiometricPrompt() {
        if (canUseFingerprint() && getFingerprintSensorState() == FingerprintState.READY) {
            viewModel.useBiometricPrompt()
        } else {
            useFingerprintButton.visibility = View.GONE
        }
    }

    private fun hideBiometricPromptFeature() {
        useFingerprintButton.visibility = View.GONE
    }

    private fun showBiometricPrompt(cryptoObject: BiometricPrompt.CryptoObject) {
        val biometricPrompt = BiometricPrompt(this, MainThreadExecutor(),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.authenticateUser(result.cryptoObject?.cipher, isNetworkAvailable())
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_login_title))
            .setDescription(getString(R.string.biometric_login_description))
            .setNegativeButtonText(getString(R.string.cancel))
            .build()

        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    private fun showLoginError(exception: Exception) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> {
                resources.getString(R.string.incorrect_email_for_user)
            }
            is FirebaseAuthInvalidCredentialsException -> {
                resources.getString(R.string.incorrect_password_for_user)
            }
            else -> {
                resources.getString(R.string.unknown_error)
            }
        }
        showMessage(errorMessage)
    }

    private fun showMessage(message: String) {
        Snackbar.make(loginConstraintLayout, message, Snackbar.LENGTH_LONG).show()
    }

    private fun setShowProgress(isLoading: Boolean) {
        if (isLoading) {
            orderButton.startAnimation()
        } else {
            orderButton.revertAnimation()
        }
    }

    override fun onDestroy() {
        orderButton.dispose()
        super.onDestroy()
    }
}