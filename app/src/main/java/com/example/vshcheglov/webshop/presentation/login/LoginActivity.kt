package com.example.vshcheglov.webshop.presentation.login

import android.content.Intent
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
import com.example.vshcheglov.webshop.presentation.helpres.Event
import com.example.vshcheglov.webshop.presentation.helpres.FingerprintState
import com.example.vshcheglov.webshop.presentation.helpres.MainThreadExecutor
import com.example.vshcheglov.webshop.presentation.main.MainActivity
import com.example.vshcheglov.webshop.presentation.registration.RegisterActivity
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.android.synthetic.main.activity_login.*
import nucleus5.factory.RequiresPresenter
import nucleus5.view.NucleusAppCompatActivity
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        initViewModelObservers()

        orderButton.setOnClickListener {
            emailTextInput.error = ""
            passwordTextInput.error = ""
            viewModel.logInUser(
                loginEmail.text.toString(),
                loginPassword.text.toString(), isNetworkAvailable()
            )
        }

        buttonRegister.setOnClickListener { startRegisterActivity() }
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

    private fun initViewModelObservers() {
        viewModel.showEmailInvalid.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled { emailTextInput.error = getString(R.string.email_error) }
            })
        viewModel.showPasswordIsInvalid.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled { passwordTextInput.error = getString(R.string.password_error) }
            })
        viewModel.showNoInternet.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled { showMessage(getString(R.string.no_internet_connection_warning)) }
            })
        viewModel.startMainScreen.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled { startMainActivity() }
            })
        viewModel.showBiometricError.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled { showMessage(getString(R.string.biometric_error)) }
            })
        viewModel.showNewBiometricEnrolled.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled { showMessage(getString(R.string.biometric_enrolled_error_text)) }
            })
        viewModel.hideBiometricPrompt.observe(this,
            Observer<Event> { event ->
                event.performEventIfNotHandled { hideBiometricPromptFeature() }
            })
        viewModel.isLoading.observe(this,
            Observer<Boolean> { isLoading ->
                setShowProgress(isLoading)
            })
        viewModel.loginError.observe(this,
            Observer<Exception> { exception ->
                showLoginError(exception)
            })
        viewModel.showBiometricPrompt.observe(this,
            Observer<BiometricPrompt.CryptoObject> { cryptoObject ->
                showBiometricPrompt(cryptoObject)
            })
        viewModel.userEmail.observe(this,
            Observer<String> { email ->
                loginEmail.setText(email, TextView.BufferType.EDITABLE)
            })
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

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
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

    private fun startRegisterActivity() {
        Intent(this, RegisterActivity::class.java).apply {
            startActivity(this)
        }
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