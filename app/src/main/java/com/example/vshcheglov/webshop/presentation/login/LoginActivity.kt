package com.example.vshcheglov.webshop.presentation.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import com.google.android.material.snackbar.Snackbar
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.biometric.BiometricPrompt
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.extensions.canUseFingerprint
import com.example.vshcheglov.webshop.extensions.getFingerprintSensorState
import com.example.vshcheglov.webshop.extensions.isNetworkAvailable
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

@RequiresPresenter(LoginPresenter::class)
class LoginActivity : NucleusAppCompatActivity<LoginPresenter>(), LoginPresenter.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        orderButton.setOnClickListener {
            emailTextInput.error = ""
            passwordTextInput.error = ""
            presenter.logInUser(
                loginEmail.text.toString(),
                loginPassword.text.toString(), isNetworkAvailable()
            )
        }

        buttonRegister.setOnClickListener {
            presenter.registerUser()
        }
        useFingerprintButton.setOnClickListener {
            prepareBiometricPrompt()
        }
        showPasswordButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> loginPassword.inputType = InputType.TYPE_CLASS_TEXT
                MotionEvent.ACTION_UP -> loginPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            true
        }
    }

    private fun prepareBiometricPrompt() {
        if (canUseFingerprint() && getFingerprintSensorState() == FingerprintState.READY) {
            presenter.useBiometricPrompt()
        } else {
            useFingerprintButton.visibility = View.GONE
        }
    }

    override fun hideBiometricPromptFeature() {
        useFingerprintButton.visibility = View.GONE
    }

    override fun showUserEmail(email: String) {
        loginEmail.setText(email, TextView.BufferType.EDITABLE)
    }

    override fun showBiometricPrompt(cryptoObject: BiometricPrompt.CryptoObject) {
        val biometricPrompt = BiometricPrompt(this, MainThreadExecutor(),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    presenter.authenticateUser(result.cryptoObject?.cipher, isNetworkAvailable())
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_login_title))
            .setDescription(getString(R.string.biometric_login_description))
            .setNegativeButtonText(getString(R.string.cancel))
            .build()

        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    override fun showBiometricError() {
        showMessage(getString(R.string.biometric_error))
    }

    override fun showNewBiometricEnrolledError() {
        showMessage(getString(R.string.biometric_enrolled_error_text))
    }

    override fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    override fun showLoginError(exception: Exception?) {
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

    override fun startRegisterActivity() {
        Intent(this, RegisterActivity::class.java).apply {
            startActivity(this)
        }
    }

    override fun showNoInternetError() {
        showMessage(resources.getString(R.string.no_internet_connection_warning))
    }

    private fun showMessage(message: String) {
        Snackbar.make(loginConstraintLayout, message, Snackbar.LENGTH_LONG).show()
    }

    override fun showInvalidEmail() {
        emailTextInput.error = resources.getString(R.string.email_error)
    }

    override fun showInvalidPassword() {
        passwordTextInput.error = resources.getString(R.string.password_error)
    }

    override fun setShowProgress(isLoading: Boolean) {
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