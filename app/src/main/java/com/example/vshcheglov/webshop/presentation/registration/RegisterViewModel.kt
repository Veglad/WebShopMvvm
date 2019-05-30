package com.example.vshcheglov.webshop.presentation.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.domain.User
import com.example.vshcheglov.webshop.extensions.isEmailValid
import com.example.vshcheglov.webshop.extensions.isPasswordValid
import com.example.vshcheglov.webshop.presentation.basket.BasketCommand
import com.example.vshcheglov.webshop.presentation.helpres.Encryptor
import com.example.vshcheglov.webshop.presentation.helpres.Event
import com.example.vshcheglov.webshop.presentation.helpres.EventWithContent
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

    private val _stateLiveData: MutableLiveData<RegisterViewState> = MutableLiveData<RegisterViewState>().apply {
        value = RegisterViewState()
    }
    val stateLiveData: LiveData<RegisterViewState> = _stateLiveData

    private val _commandLiveData: MutableLiveData<EventWithContent<RegisterCommand>> = MutableLiveData()
    val commandLiveData: LiveData<EventWithContent<RegisterCommand>> = _commandLiveData

    private fun getState() = stateLiveData.value!!

    init {
        App.appComponent.inject(this)
    }

    fun registerUser(email: String, password: String, confirmPassword: String, isNetworkAvailable: Boolean) {
        var isValid = true

        if (!email.isEmailValid()) {
            setCommand(ShowEmailInvalid)
            isValid = false
        }
        if (password != confirmPassword) {
            setCommand(ShowPasswordsNotMatch)
            isValid = false
        }
        if (!password.isPasswordValid()) {
            setCommand(ShowPasswordInvalid)
            isValid = false
        }
        if (!confirmPassword.isPasswordValid()) {
            setCommand(ShowConfirmPasswordInvalid)
            isValid = false
        }

        if (isNetworkAvailable) {
            if (!isValid) return
            registerUserWithEmailAndPassword(email, password)
        } else {
            setCommand(ShowNoInternet)
        }
    }

    private fun registerUserWithEmailAndPassword(email: String, password: String) {
        uiCoroutineScope.launch {
            _stateLiveData.value = getState().copy(isLoading = true)
            try {
                dataProvider.registerUser(email, password)

                if (!dataProvider.containsUserCredentials()) {
                    val encryptedPassword = encryptor.encode(password)
                    encryptedPassword?.let {
                        dataProvider.saveUserCredentials(User.UserCredentials(email, encryptedPassword))
                    }
                }

                setCommand(StartMainScreen)
            } catch (ex: Exception) {
                Timber.e("user registration error: $ex")
                setCommand(ShowRegisterError(ex))
            } finally {
                _stateLiveData.value = getState().copy(isLoading = false)
            }
        }
    }

    private fun setCommand(command: RegisterCommand) {
        _commandLiveData.value = EventWithContent(command)
    }

    override fun onCleared() {
        super.onCleared()
        _stateLiveData.value = getState().copy(isLoading = false)
        job.cancel()
    }
}

data class RegisterViewState(
    var isLoading: Boolean = false
)

sealed class RegisterCommand
class ShowRegisterError(val exception: Exception) : RegisterCommand()
object StartMainScreen : RegisterCommand()
object ShowNoInternet : RegisterCommand()
object ShowConfirmPasswordInvalid : RegisterCommand()
object ShowPasswordInvalid : RegisterCommand()
object ShowPasswordsNotMatch : RegisterCommand()
object ShowEmailInvalid : RegisterCommand()