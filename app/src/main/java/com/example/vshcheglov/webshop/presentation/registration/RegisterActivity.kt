package com.example.vshcheglov.webshop.presentation.registration

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.text.InputType
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.extensions.isNetworkAvailable
import com.example.vshcheglov.webshop.presentation.main.MainActivity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.android.synthetic.main.activity_register.*
import nucleus5.factory.RequiresPresenter
import nucleus5.view.NucleusAppCompatActivity
import java.lang.Exception

@RequiresPresenter(RegisterPresenter::class)
class RegisterActivity : NucleusAppCompatActivity<RegisterPresenter>(), RegisterPresenter.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        buttonRegisterUser.setOnClickListener {
            registerEmailTextInput.error = ""
            registerPasswordTextInput.error = ""
            registerConfirmPasswordTextInput.error = ""
            presenter.registerUser(registerEmail.text.toString(),
                registerPassword.text.toString(), registerConfirmPassword.text.toString(), isNetworkAvailable())
        }

        setSupportActionBar(registerActionBar)
        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowTitleEnabled(false);
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

    private fun showPassword(event: MotionEvent, editText: EditText) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> editText.inputType = InputType.TYPE_CLASS_TEXT
            MotionEvent.ACTION_UP -> editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    override fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    override fun showLoginError(exception: Exception?) {
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

    override fun showNoInternetError() {
        Snackbar.make(
            registerLinearLayout,
            resources.getString(R.string.no_internet_connection_warning),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun showInvalidEmail() {
        registerEmailTextInput.error = resources.getString(R.string.email_error)
    }

    override fun showInvalidPassword() {
        registerPasswordTextInput.error = resources.getString(R.string.password_error)
    }

    override fun showInvalidConfirmPassword() {
        registerConfirmPasswordTextInput.error = resources.getString(R.string.password_error)
    }

    override fun showPasswordsNotMatchError() {
        registerConfirmPasswordTextInput.error = resources.getString(R.string.password_not_match)
    }

    override fun setShowProgress(isLoading: Boolean) {
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
