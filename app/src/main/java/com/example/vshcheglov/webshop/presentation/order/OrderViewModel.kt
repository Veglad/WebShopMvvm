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

    private val _liveDataOrderPrice = MutableLiveData<Double>()
    val liveDataOrderPrice: LiveData<Double> = _liveDataOrderPrice

    private val _liveDataIsLoading = MutableLiveData<Boolean>()
    val liveDataIsLoading: LiveData<Boolean> = _liveDataIsLoading

    private val _liveDataInvalidName = MutableLiveData<Event>()
    val liveDataInvalidName: LiveData<Event> = _liveDataInvalidName

    private val _liveDataInvalidSecondName = MutableLiveData<Event>()
    val liveDataInvalidSecondName: LiveData<Event> = _liveDataInvalidSecondName

    private val _liveDataInvalidCardNumber = MutableLiveData<Event>()
    val liveDataInvalidCardNumber: LiveData<Event> = _liveDataInvalidCardNumber

    private val _liveDataInvalidCardMonth = MutableLiveData<Event>()
    val liveDataInvalidCardMonth: LiveData<Event> = _liveDataInvalidCardMonth

    private val _liveDataInvalidCardYear = MutableLiveData<Event>()
    val liveDataInvalidCardYear: LiveData<Event> = _liveDataInvalidCardYear

    private val _liveDataInvalidCardCvv = MutableLiveData<Event>()
    val liveDataInvalidCardCvv: LiveData<Event> = _liveDataInvalidCardCvv

    private val _liveDataNoInternet = MutableLiveData<Event>()
    val liveDataNoInternet: LiveData<Event> = _liveDataNoInternet

    private val _liveDataOrderSaveError = MutableLiveData<Event>()
    val liveDataOrderSaveError: LiveData<Event> = _liveDataOrderSaveError

    private val _liveDataOrderCompleted = MutableLiveData<Event>()
    val liveDataOrderCompleted: LiveData<Event> = _liveDataOrderCompleted

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
        _liveDataOrderPrice.value = Basket.totalPriceWithDiscount
    }

    fun makeOrder(card: OrderCard, isNetworkAvailable: Boolean) {
        uiCoroutineScope.launch {
            _liveDataIsLoading.value = true
            val isValid = validateOrderCard(card)

            if (isNetworkAvailable) {
                if (isValid) {
                    saveOrder()
                }
            } else {
                _liveDataNoInternet.value = Event()
            }
            _liveDataIsLoading.value = false
        }
    }

    private fun validateOrderCard(card: OrderCard): Boolean {
        var isValid = true
        if (card.name.length < MIN_NAME_LENGTH) {
            isValid = false
            _liveDataInvalidName.value = Event()
        }
        if (card.lastName.length < MIN_NAME_LENGTH) {
            isValid = false
            _liveDataInvalidSecondName.value = Event()
        }
        card.cardNumber = card.cardNumber.replace(" ", "")
        if (!card.cardNumber.isCardNumberValid()) {
            isValid = false
            _liveDataInvalidCardNumber.value = Event()
        }
        val cardMonth = card.cardMonth
        if (cardMonth == null || cardMonth !in MIN_CARD_MONTH_NUMBER..MAX_CARD_MONTH_NUMBER) {
            isValid = false
            _liveDataInvalidCardMonth.value = Event()
        }
        val cardYear = card.cardYear
        if (cardYear == null ||
            cardYear !in Calendar.getInstance().get(Calendar.YEAR) % 100..MAX_CARD_YEAR_NUMBER
        ) {
            isValid = false
            _liveDataInvalidCardYear.value = Event()
        }
        if (!card.cardCvv.isCvvValid()) {
            isValid = false
            _liveDataInvalidCardCvv.value = Event()
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
            _liveDataOrderCompleted.value = Event()
        } catch (ex: Exception) {
            _liveDataOrderSaveError.value = Event()
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}