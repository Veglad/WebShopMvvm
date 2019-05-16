package com.example.vshcheglov.webshop.presentation.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.domain.User
import com.example.vshcheglov.webshop.extensions.isEmailValid
import com.example.vshcheglov.webshop.extensions.isPasswordValid
import com.example.vshcheglov.webshop.presentation.helpres.Encryptor
import com.example.vshcheglov.webshop.presentation.helpres.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RegisterViewModel : ViewModel() {
    @Inject
    lateinit var dataProvider: DataProvider
    @Inject
    lateinit var encryptor: Encryptor

    private val job: Job = Job()
    private val uiCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val _showEmailInvalid = MutableLiveData<Event>()
    val showEmailInvalid: LiveData<Event>
        get() = _showEmailInvalid

    private val _showPasswordsNotMatch = MutableLiveData<Event>()
    val showPasswordsNotMatch: LiveData<Event>
        get() = _showPasswordsNotMatch

    private val _showPasswordIsInvalid = MutableLiveData<Event>()
    val showPasswordIsInvalid: LiveData<Event>
        get() = _showPasswordIsInvalid

    private val _showConfirmPasswordIsInvalid = MutableLiveData<Event>()
    val showConfirmPasswordIsInvalid: LiveData<Event>
        get() = _showConfirmPasswordIsInvalid

    private val _showNoInternet = MutableLiveData<Event>()
    val showNoInternet: LiveData<Event>
        get() = _showNoInternet

    private val _startMainScreen = MutableLiveData<Event>()
    val startMainScreen: LiveData<Event>
        get() = _startMainScreen

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _registrationError = MutableLiveData<Exception>()
    val registrationError: LiveData<Exception>
        get() = _registrationError

    init {
        App.appComponent.inject(this)
    }

    fun registerUser(email: String, password: String, confirmPassword: String, isNetworkAvailable: Boolean) {
        var isValid = true

        if (!email.isEmailValid()) {
            _showEmailInvalid.value = Event()
            isValid = false
        }
        if (password != confirmPassword) {
            _showPasswordsNotMatch.value = Event()
            isValid = false
        }
        if (!password.isPasswordValid()) {
            _showPasswordIsInvalid.value = Event()
            isValid = false
        }
        if (!confirmPassword.isPasswordValid()) {
            _showConfirmPasswordIsInvalid.value = Event()
            isValid = false
        }

        if (isNetworkAvailable) {
            if (!isValid) return
            registerUserWithEmailAndPassword(email, password)
        } else {
            _showNoInternet.value = Event()
        }
    }

    private fun registerUserWithEmailAndPassword(email: String, password: String) {
        uiCoroutineScope.launch {
            _isLoading.value = true
            try {
                dataProvider.registerUser(email, password)

                if (!dataProvider.containsUserCredentials()) {
                    val encryptedPassword = encryptor.encode(password)
                    encryptedPassword?.let {
                        dataProvider.saveUserCredentials(User.UserCredentials(email, encryptedPassword))
                    }
                }

                _startMainScreen.value = Event()
            } catch (ex: Exception) {
                Timber.e("user registration error: $ex")
                _registrationError.value = ex
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _isLoading.value = false
        job.cancel()
    }
}