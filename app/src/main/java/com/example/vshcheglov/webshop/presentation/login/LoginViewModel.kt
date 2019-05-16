package com.example.vshcheglov.webshop.presentation.login

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.domain.User.UserCredentials
import com.example.vshcheglov.webshop.extensions.isEmailValid
import com.example.vshcheglov.webshop.extensions.isPasswordValid
import com.example.vshcheglov.webshop.presentation.helpres.Encryptor
import com.example.vshcheglov.webshop.presentation.helpres.Event
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Exception
import javax.crypto.Cipher
import javax.inject.Inject

class LoginViewModel : ViewModel() {

    @Inject
    lateinit var dataProvider: DataProvider
    @Inject
    lateinit var encryptor: Encryptor

    private val job: Job = Job()
    private val uiCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val _showEmailInvalid = MutableLiveData<Event>()
    val showEmailInvalid: LiveData<Event>
        get() = _showEmailInvalid

    private val _showPasswordIsInvalid = MutableLiveData<Event>()
    val showPasswordIsInvalid: LiveData<Event>
        get() = _showPasswordIsInvalid

    private val _showNoInternet = MutableLiveData<Event>()
    val showNoInternet: LiveData<Event>
        get() = _showNoInternet

    private val _startMainScreen = MutableLiveData<Event>()
    val startMainScreen: LiveData<Event>
        get() = _startMainScreen

    private val _showBiometricError = MutableLiveData<Event>()
    val showBiometricError: LiveData<Event>
        get() = _showBiometricError

    private val _hideBiometricPrompt = MutableLiveData<Event>()
    val hideBiometricPrompt: LiveData<Event>
        get() = _hideBiometricPrompt

    private val _showNewBiometricEnrolled = MutableLiveData<Event>()
    val showNewBiometricEnrolled: LiveData<Event>
        get() = _showNewBiometricEnrolled

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _loginError = MutableLiveData<Exception>()
    val loginError: LiveData<Exception>
        get() = _loginError

    private val _showBiometricPrompt = MutableLiveData<BiometricPrompt.CryptoObject>()
    val showBiometricPrompt: LiveData<BiometricPrompt.CryptoObject>
        get() = _showBiometricPrompt

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String>
        get() = _userEmail

    init {
        App.appComponent.inject(this)
        initEmailFromCredentials()
    }

    fun logInUser(email: String, password: String, isNetworkAvailable: Boolean) {
        var isValid = true
        if (!email.isEmailValid()) {
            _showEmailInvalid.value = Event()
            isValid = false
        }
        if (!password.isPasswordValid()) {
            _showPasswordIsInvalid.value = Event()
            isValid = false
        }

        if (isNetworkAvailable) {
            if (!isValid) return
            performLogin(email, password)
        } else {
            _showNoInternet.value = Event()
        }
    }

    private fun performLogin(email: String, password: String) {
        uiCoroutineScope.launch {
            _isLoading.value = true
            try {
                dataProvider.signInUser(email, password)

                if (!dataProvider.containsUserCredentials()) {
                    val encryptedPassword = encryptor.encode(password)
                    encryptedPassword?.let {
                        dataProvider.saveUserCredentials(UserCredentials(email, encryptedPassword))
                    }
                }

                _startMainScreen.value = Event()
            } catch (ex: Exception) {
                Timber.e("user sign in error: $ex")
                _loginError.value = ex
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun initEmailFromCredentials() {
        if (dataProvider.containsUserCredentials()) {
            val credentials = dataProvider.getUserCredentials()
            credentials?.let {
                _userEmail.value = credentials.email
            }
        } else {
            _hideBiometricPrompt.value = Event()
        }
    }

    fun useBiometricPrompt() {
        if (dataProvider.containsUserCredentials()) {
            val cryptoObject = encryptor.cryptoObject
            if (cryptoObject != null) {
                _showBiometricPrompt.value = cryptoObject
            } else {
                dataProvider.deleteUserCredentials()
                _showNewBiometricEnrolled.value = Event()
            }
        } else {
            _hideBiometricPrompt.value = Event()
        }
    }

    fun authenticateUser(cipher: Cipher?, isNetworkAvailable: Boolean) {
        val userCredentials = dataProvider.getUserCredentials()
        if (cipher == null) {
            _showBiometricError.value = Event()
            Timber.e("Cipher is null")
        } else if (userCredentials == null) {
            Timber.e("Incorrect saved credentials")
            _showBiometricError.value = Event()
        } else {
            val password = encryptor.decode(userCredentials.encryptedPassword, cipher)
            if (password != null) {
                if (isNetworkAvailable) {
                    performLogin(userCredentials.email, password)
                } else {
                    _showNoInternet.value = Event()
                }
            } else {
                Timber.e("Password decryption error")
                _showBiometricError.value = Event()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
        _isLoading.value = false
    }
}