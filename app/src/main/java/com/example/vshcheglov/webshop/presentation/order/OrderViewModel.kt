package com.example.vshcheglov.webshop.presentation.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.data.entities.mappers.BasketToOrderMapper
import com.example.vshcheglov.webshop.domain.Basket
import com.example.vshcheglov.webshop.extensions.isCardNumberValid
import com.example.vshcheglov.webshop.extensions.isCvvValid
import com.example.vshcheglov.webshop.presentation.entites.OrderCard
import com.example.vshcheglov.webshop.presentation.helpres.Event
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class OrderViewModel : ViewModel() {

    companion object {
        const val MIN_NAME_LENGTH = 2
        const val MIN_CARD_MONTH_NUMBER = 1
        const val MAX_CARD_MONTH_NUMBER = 12
        const val MAX_CARD_YEAR_NUMBER = 30
    }

    private val _orderPrice = MutableLiveData<Double>()
    val orderPrice: LiveData<Double> = _orderPrice

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _invalidName = MutableLiveData<Event>()
    val invalidName: LiveData<Event> = _invalidName

    private val _invalidSecondName = MutableLiveData<Event>()
    val invalidSecondName: LiveData<Event> = _invalidSecondName

    private val _invalidCardNumber = MutableLiveData<Event>()
    val invalidCardNumber: LiveData<Event> = _invalidCardNumber

    private val _invalidCardMonth = MutableLiveData<Event>()
    val invalidCardMonth: LiveData<Event> = _invalidCardMonth

    private val _invalidCardYear = MutableLiveData<Event>()
    val invalidCardYear: LiveData<Event> = _invalidCardYear

    private val _invalidCardCvv = MutableLiveData<Event>()
    val invalidCardCvv: LiveData<Event> = _invalidCardCvv

    private val _noInternet = MutableLiveData<Event>()
    val noInternet: LiveData<Event> = _noInternet

    private val _orderSaveError = MutableLiveData<Event>()
    val orderSaveError: LiveData<Event> = _orderSaveError

    private val _orderCompleted = MutableLiveData<Event>()
    val orderCompleted: LiveData<Event> = _orderCompleted

    @Inject
    lateinit var dataProvider: DataProvider
    @Inject
    lateinit var basketToOrderMapper: BasketToOrderMapper

    private val job: Job = Job()
    private val uiCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    init {
        App.appComponent.inject(this)
    }

    fun initOrderPrice() {
        _orderPrice.value = Basket.totalPriceWithDiscount
    }

    fun makeOrder(card: OrderCard, isNetworkAvailable: Boolean) {
        uiCoroutineScope.launch {
            _isLoading.value = true
            val isValid = validateOrderCard(card)

            if (isNetworkAvailable) {
                if (isValid) {
                    saveOrder()
                }
            } else {
                _noInternet.value = Event()
            }
            _isLoading.value = false
        }
    }

    private fun validateOrderCard(card: OrderCard): Boolean {
        var isValid = true
        if (card.name.length < MIN_NAME_LENGTH) {
            isValid = false
            _invalidName.value = Event()
        }
        if (card.lastName.length < MIN_NAME_LENGTH) {
            isValid = false
            _invalidSecondName.value = Event()
        }
        card.cardNumber = card.cardNumber.replace(" ", "")
        if (!card.cardNumber.isCardNumberValid()) {
            isValid = false
            _invalidCardNumber.value = Event()
        }
        val cardMonth = card.cardMonth
        if (cardMonth == null || cardMonth !in MIN_CARD_MONTH_NUMBER..MAX_CARD_MONTH_NUMBER) {
            isValid = false
            _invalidCardMonth.value = Event()
        }
        val cardYear = card.cardYear
        if (cardYear == null ||
            cardYear !in Calendar.getInstance().get(Calendar.YEAR) % 100..MAX_CARD_YEAR_NUMBER
        ) {
            isValid = false
            _invalidCardYear.value = Event()
        }
        if (!card.cardCvv.isCvvValid()) {
            isValid = false
            _invalidCardCvv.value = Event()
        }
        return isValid
    }

    private suspend fun saveOrder() {
        val order = basketToOrderMapper.map(Basket)
        try {
            withContext(Dispatchers.IO) {
                dataProvider.saveOrder(order)
            }
            Basket.clear()
            _orderCompleted.value = Event()
        } catch (ex: Exception) {
            _orderSaveError.value = Event()
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}