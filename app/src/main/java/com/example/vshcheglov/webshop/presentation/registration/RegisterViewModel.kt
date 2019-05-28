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

    private val _liveDataShowEmailInvalid = MutableLiveData<Event>()
    val liveDataShowEmailInvalid: LiveData<Event> = _liveDataShowEmailInvalid

    private val _liveDataShowPasswordsNotMatch = MutableLiveData<Event>()
    val liveDataShowPasswordsNotMatch: LiveData<Event> = _liveDataShowPasswordsNotMatch

    private val _liveDataShowPasswordIsInvalid = MutableLiveData<Event>()
    val liveDataShowPasswordIsInvalid: LiveData<Event> = _liveDataShowPasswordIsInvalid

    private val _liveDataShowConfirmPasswordInvalid = MutableLiveData<Event>()
    val showConfirmPasswordInvalid: LiveData<Event> = _liveDataShowConfirmPasswordInvalid

    private val _liveDataShowNoInternet = MutableLiveData<Event>()
    val liveDataShowNoInternet: LiveData<Event> = _liveDataShowNoInternet

    private val _liveDataStartMainScreen = MutableLiveData<Event>()
    val liveDataStartMainScreen: LiveData<Event> = _liveDataStartMainScreen

    private val _liveDataIsLoading = MutableLiveData<Boolean>()
    val liveDataIsLoading: LiveData<Boolean> = _liveDataIsLoading

    private val _liveDataRegistrationError = MutableLiveData<Exception>()
    val liveDataRegistrationError: LiveData<Exception> = _liveDataRegistrationError

    init {
        App.appComponent.inject(this)
    }

    fun registerUser(email: String, password: String, confirmPassword: String, isNetworkAvailable: Boolean) {
        var isValid = true

        if (!email.isEmailValid()) {
            _liveDataShowEmailInvalid.value = Event()
            isValid = false
        }
        if (password != confirmPassword) {
            _liveDataShowPasswordsNotMatch.value = Event()
            isValid = false
        }
        if (!password.isPasswordValid()) {
            _liveDataShowPasswordIsInvalid.value = Event()
            isValid = false
        }
        if (!confirmPassword.isPasswordValid()) {
            _liveDataShowConfirmPasswordInvalid.value = Event()
            isValid = false
        }

        if (isNetworkAvailable) {
            if (!isValid) return
            registerUserWithEmailAndPassword(email, password)
        } else {
            _liveDataShowNoInternet.value = Event()
        }
    }

    private fun registerUserWithEmailAndPassword(email: String, password: String) {
        uiCoroutineScope.launch {
            _liveDataIsLoading.value = true
            try {
                dataProvider.registerUser(email, password)

                if (!dataProvider.containsUserCredentials()) {
                    val encryptedPassword = encryptor.encode(password)
                    encryptedPassword?.let {
                        dataProvider.saveUserCredentials(User.UserCredentials(email, encryptedPassword))
                    }
                }

                _liveDataStartMainScreen.value = Event()
            } catch (ex: Exception) {
                Timber.e("user registration error: $ex")
                _liveDataRegistrationError.value = ex
            } finally {
                _liveDataIsLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _liveDataIsLoading.value = false
        job.cancel()
    }
}