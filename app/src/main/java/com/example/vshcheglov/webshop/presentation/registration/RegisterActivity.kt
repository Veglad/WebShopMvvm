package com.example.vshcheglov.webshop.presentation.registration

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.text.InputType
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.extensions.isNetworkAvailable
import com.example.vshcheglov.webshop.presentation.helpers.Router
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    private val viewModel: RegisterViewModel by lazy {
        ViewModelProviders.of(this).get(RegisterViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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
            showPassword(event, registerPassword, registerShowPasswordButton)
            true
        }

        showConfirmPasswordButton.setOnTouchListener { _, event ->
            showPassword(event, registerConfirmPassword, showConfirmPasswordButton)
            true
        }
    }

    private fun performCommand(command: RegisterCommand) {
        when (command) {
            is StartMainScreen -> Router.showMain(this)
            is ShowEmailInvalid -> registerEmailTextInput.error = getString(R.string.email_error)
            is ShowPasswordsNotMatch -> {
                registerConfirmPasswordTextInput.error = getString(R.string.passwords_not_match)
            }
            is ShowPasswordInvalid -> {
                registerPasswordTextInput.error = resources.getString(R.string.password_error)
            }
            is ShowConfirmPasswordInvalid -> {
                registerConfirmPasswordTextInput.error = resources.getString(R.string.password_error)
            }
            is ShowNoInternet -> showNoInternetError()
            is ShowRegisterError -> showLoginError(command.exception)
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

    private fun showPassword(
        event: MotionEvent,
        editText: EditText,
        imageButton: ImageButton
    ) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                editText.inputType = InputType.TYPE_CLASS_TEXT
                val showPasswordDrawable =
                    ContextCompat.getDrawable(this, R.drawable.ic_hide_password_white_24dp)
                imageButton.setImageDrawable(showPasswordDrawable)
            }
            MotionEvent.ACTION_UP -> {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                val hidePasswordDrawable =
                    ContextCompat.getDrawable(this, R.drawable.ic_show_password_white_24dp)
                imageButton.setImageDrawable(hidePasswordDrawable)
            }
        }
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
