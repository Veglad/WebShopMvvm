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
import com.example.vshcheglov.webshop.presentation.helpres.EventWithContent
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

    private val _stateLiveData = MutableLiveData<LoginViewState>().apply {
        value = LoginViewState()
    }
    val stateLiveData: LiveData<LoginViewState> = _stateLiveData

    private val _commandLiveData: MutableLiveData<EventWithContent<LoginCommand>> = MutableLiveData()
    val commandLiveData: LiveData<EventWithContent<LoginCommand>> = _commandLiveData

    private fun getState() = stateLiveData.value!!

    init {
        App.appComponent.inject(this)
    }

    fun logInUser(email: String, password: String, isNetworkAvailable: Boolean) {
        var isValid = true
        if (!email.isEmailValid()) {
            setCommand(LoginCommand.ShowEmailInvalid)
            isValid = false
        }
        if (!password.isPasswordValid()) {
            setCommand(LoginCommand.ShowPasswordInvalid)
            isValid = false
        }

        if (isNetworkAvailable) {
            if (!isValid) return
            performLogin(email, password)
        } else {
            setCommand(LoginCommand.ShowNoInternet)
        }
    }

    private fun performLogin(email: String, password: String) {
        uiCoroutineScope.launch {
            _stateLiveData.value = getState().copy(isLoading = true, userEmail = email)
            try {
                dataProvider.signInUser(email, password)

                if (!dataProvider.containsUserCredentials()) {
                    val encryptedPassword = encryptor.encode(password)
                    encryptedPassword?.let {
                        dataProvider.saveUserCredentials(UserCredentials(email, encryptedPassword))
                    }
                }

                setCommand(LoginCommand.StartMainScreen)
            } catch (ex: Exception) {
                Timber.e("user sign in error: $ex")
                setCommand(LoginCommand.ShowLoginError(ex))
            } finally {
                _stateLiveData.value = getState().copy(isLoading = false)
            }
        }
    }

    fun loadUserEmail() {
        if (dataProvider.containsUserCredentials()) {
            val credentials = dataProvider.getUserCredentials()
            credentials?.let {
                _stateLiveData.value = getState().copy(userEmail = credentials.email)
            }
        } else {
            setCommand(LoginCommand.HideBiometricPrompt)
        }
    }

    fun useBiometricPrompt() {
        if (dataProvider.containsUserCredentials()) {
            val cryptoObject = encryptor.cryptoObject
            if (cryptoObject != null) {
                setCommand(LoginCommand.ShowBiometricPrompt(cryptoObject))
            } else {
                dataProvider.deleteUserCredentials()
                setCommand(LoginCommand.ShowNewBiometricEnrolled)
            }
        } else {
            setCommand(LoginCommand.HideBiometricPrompt)
        }
    }

    fun authenticateUser(cipher: Cipher?, isNetworkAvailable: Boolean) {
        val userCredentials = dataProvider.getUserCredentials()
        if (cipher == null) {
            setCommand(LoginCommand.ShowBiometricError)
            Timber.e("Cipher is null")
        } else if (userCredentials == null) {
            Timber.e("Incorrect saved credentials")
            setCommand(LoginCommand.ShowBiometricError)
        } else {
            val password = encryptor.decode(userCredentials.encryptedPassword, cipher)
            if (password != null) {
                if (isNetworkAvailable) {
                    performLogin(userCredentials.email, password)
                } else {
                    setCommand(LoginCommand.ShowNoInternet)
                }
            } else {
                Timber.e("Password decryption error")
                setCommand(LoginCommand.ShowBiometricError)
            }
        }
    }

    private fun setCommand(command: LoginCommand) {
        _commandLiveData.value = EventWithContent(command)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
        _stateLiveData.value = getState().copy(isLoading = false)
    }
}

data class LoginViewState(
    var isLoading: Boolean = false,
    var userEmail: String = ""
)

sealed class LoginCommand {
    class ShowLoginError(val exception: Exception) : LoginCommand()
    class ShowBiometricPrompt(val cryptoObject: BiometricPrompt.CryptoObject) : LoginCommand()
    object StartMainScreen : LoginCommand()
    object ShowNoInternet : LoginCommand()
    object ShowPasswordInvalid : LoginCommand()
    object ShowEmailInvalid : LoginCommand()
    object HideBiometricPrompt : LoginCommand()
    object ShowNewBiometricEnrolled : LoginCommand()
    object ShowBiometricError : LoginCommand()

}