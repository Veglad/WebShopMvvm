package com.example.vshcheglov.webshop.presentation.login

import androidx.biometric.BiometricPrompt
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.domain.User.UserCredentials
import com.example.vshcheglov.webshop.extensions.isEmailValid
import com.example.vshcheglov.webshop.extensions.isPasswordValid
import com.example.vshcheglov.webshop.presentation.helpres.Encryptor
import kotlinx.coroutines.*
import nucleus5.presenter.Presenter
import timber.log.Timber
import java.lang.Exception
import javax.crypto.Cipher
import javax.inject.Inject

class LoginViewModel : Presenter<LoginViewModel.View>() {
    
    @Inject
    lateinit var dataProvider: DataProvider
    @Inject
    lateinit var encryptor: Encryptor

    private lateinit var job: Job
    private lateinit var uiCoroutineScope: CoroutineScope

    init {
        App.appComponent.inject(this)
    }

    fun logInUser(email: String, password: String, isNetworkAvailable: Boolean) {
        var isValid = true
        if (!email.isEmailValid()) {
            view?.showInvalidEmail()
            isValid = false
        }
        if (!password.isPasswordValid()) {
            view?.showInvalidPassword()
            isValid = false
        }

        if (isNetworkAvailable) {
            if (!isValid) return
            performLogin(email, password)
        } else {
            view?.showNoInternetError()
        }
    }

    private fun performLogin(email: String, password: String) {
        uiCoroutineScope.launch {
            view?.setShowProgress(true)
            try {
                dataProvider.signInUser(email, password)

                if (!dataProvider.containsUserCredentials()) {
                    val encryptedPassword = encryptor.encode(password)
                    encryptedPassword?.let {
                        dataProvider.saveUserCredentials(UserCredentials(email, encryptedPassword))
                    }
                }

                view?.startMainActivity()
            } catch (ex: Exception) {
                Timber.e("user sign in error: $ex")
                view?.showLoginError(ex)
            } finally {
                view?.setShowProgress(false)
            }
        }
    }

    fun registerUser() {
        view?.startRegisterActivity()
    }

    override fun onDropView() {
        super.onDropView()
        job.cancel()
        view?.setShowProgress(false)
    }

    override fun onTakeView(view: View?) {
        super.onTakeView(view)
        initCoroutineJob()
        showEmailFromCredentials()
    }

    private fun showEmailFromCredentials() {
        if (dataProvider.containsUserCredentials()) {
            val credentials = dataProvider.getUserCredentials()
            credentials?.let {
                view?.showUserEmail(credentials.email)
            }
        } else {
            view?.hideBiometricPromptFeature()
        }
    }

    private fun initCoroutineJob() {
        job = Job()
        uiCoroutineScope = CoroutineScope(Dispatchers.Main + job)
    }

    fun useBiometricPrompt() {
        if (dataProvider.containsUserCredentials()) {
            val cryptoObject = encryptor.cryptoObject
            if (cryptoObject != null) {
                view?.showBiometricPrompt(cryptoObject)
            } else {
                dataProvider.deleteUserCredentials()
                view?.showNewBiometricEnrolledError()
            }
        } else {
            view?.hideBiometricPromptFeature()
        }
    }

    fun authenticateUser(cipher: Cipher?, isNetworkAvailable: Boolean) {
        val userCredentials = dataProvider.getUserCredentials()
        if (cipher == null) {
            view?.showBiometricError()
            Timber.e("Cipher is null")
        } else if (userCredentials == null) {
            Timber.e("Incorrect saved credentials")
            view?.showBiometricError()
        } else {
            val password = encryptor.decode(userCredentials.encryptedPassword, cipher)
            if (password != null) {
                if (isNetworkAvailable) {
                    performLogin(userCredentials.email, password)
                } else {
                    view?.showNoInternetError()
                }
            } else {
                Timber.e("Password decryption error")
                view?.showBiometricError()
            }
        }
    }

    interface View {
        fun startMainActivity()

        fun showLoginError(exception: Exception?)

        fun startRegisterActivity()

        fun showNoInternetError()

        fun showInvalidEmail()

        fun showInvalidPassword()

        fun setShowProgress(isLoading: Boolean)

        fun showBiometricPrompt(cryptoObject: BiometricPrompt.CryptoObject)

        fun showBiometricError()

        fun showNewBiometricEnrolledError()

        fun hideBiometricPromptFeature()

        fun showUserEmail(email: String)
    }
}