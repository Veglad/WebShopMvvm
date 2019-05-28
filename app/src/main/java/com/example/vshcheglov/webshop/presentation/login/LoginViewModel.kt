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

    private val _liveDataShowEmailInvalid = MutableLiveData<Event>()
    val liveDataShowEmailInvalid: LiveData<Event> = _liveDataShowEmailInvalid

    private val _liveDataShowPasswordIsInvalid = MutableLiveData<Event>()
    val liveDataShowPasswordIsInvalid: LiveData<Event> = _liveDataShowPasswordIsInvalid

    private val _liveDataShowNoInternet = MutableLiveData<Event>()
    val liveDataShowNoInternet: LiveData<Event> = _liveDataShowNoInternet

    private val _liveDataStartMainScreen = MutableLiveData<Event>()
    val liveDataStartMainScreen: LiveData<Event> = _liveDataStartMainScreen

    private val _liveDataShowBiometricError = MutableLiveData<Event>()
    val liveDataShowBiometricError: LiveData<Event> = _liveDataShowBiometricError

    private val _liveDataHideBiometricPrompt = MutableLiveData<Event>()
    val liveDataHideBiometricPrompt: LiveData<Event> = _liveDataHideBiometricPrompt

    private val _liveDataShowNewBiometricEnrolled = MutableLiveData<Event>()
    val liveDataShowNewBiometricEnrolled: LiveData<Event> = _liveDataShowNewBiometricEnrolled

    private val _liveDataIsLoading = MutableLiveData<Boolean>()
    val liveDataIsLoading: LiveData<Boolean> = _liveDataIsLoading

    private val _liveDataLoginError = MutableLiveData<Exception>()
    val liveDataLoginError: LiveData<Exception> = _liveDataLoginError

    private val _liveDataShowBiometricPrompt = MutableLiveData<BiometricPrompt.CryptoObject>()
    val liveDataShowBiometricPrompt: LiveData<BiometricPrompt.CryptoObject> = _liveDataShowBiometricPrompt

    private val _liveDataUserEmail = MutableLiveData<String>()
    val liveDataUserEmail: LiveData<String> = _liveDataUserEmail

    init {
        App.appComponent.inject(this)
        initEmailFromCredentials()
    }

    fun logInUser(email: String, password: String, isNetworkAvailable: Boolean) {
        var isValid = true
        if (!email.isEmailValid()) {
            _liveDataShowEmailInvalid.value = Event()
            isValid = false
        }
        if (!password.isPasswordValid()) {
            _liveDataShowPasswordIsInvalid.value = Event()
            isValid = false
        }

        if (isNetworkAvailable) {
            if (!isValid) return
            performLogin(email, password)
        } else {
            _liveDataShowNoInternet.value = Event()
        }
    }

    private fun performLogin(email: String, password: String) {
        uiCoroutineScope.launch {
            _liveDataIsLoading.value = true
            try {
                dataProvider.signInUser(email, password)

                if (!dataProvider.containsUserCredentials()) {
                    val encryptedPassword = encryptor.encode(password)
                    encryptedPassword?.let {
                        dataProvider.saveUserCredentials(UserCredentials(email, encryptedPassword))
                    }
                }

                _liveDataStartMainScreen.value = Event()
            } catch (ex: Exception) {
                Timber.e("user sign in error: $ex")
                _liveDataLoginError.value = ex
            } finally {
                _liveDataIsLoading.value = false
            }
        }
    }

    private fun initEmailFromCredentials() {
        if (dataProvider.containsUserCredentials()) {
            val credentials = dataProvider.getUserCredentials()
            credentials?.let {
                _liveDataUserEmail.value = credentials.email
            }
        } else {
            _liveDataHideBiometricPrompt.value = Event()
        }
    }

    fun useBiometricPrompt() {
        if (dataProvider.containsUserCredentials()) {
            val cryptoObject = encryptor.cryptoObject
            if (cryptoObject != null) {
                _liveDataShowBiometricPrompt.value = cryptoObject
            } else {
                dataProvider.deleteUserCredentials()
                _liveDataShowNewBiometricEnrolled.value = Event()
            }
        } else {
            _liveDataHideBiometricPrompt.value = Event()
        }
    }

    fun authenticateUser(cipher: Cipher?, isNetworkAvailable: Boolean) {
        val userCredentials = dataProvider.getUserCredentials()
        if (cipher == null) {
            _liveDataShowBiometricError.value = Event()
            Timber.e("Cipher is null")
        } else if (userCredentials == null) {
            Timber.e("Incorrect saved credentials")
            _liveDataShowBiometricError.value = Event()
        } else {
            val password = encryptor.decode(userCredentials.encryptedPassword, cipher)
            if (password != null) {
                if (isNetworkAvailable) {
                    performLogin(userCredentials.email, password)
                } else {
                    _liveDataShowNoInternet.value = Event()
                }
            } else {
                Timber.e("Password decryption error")
                _liveDataShowBiometricError.value = Event()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
        _liveDataIsLoading.value = false
    }
}