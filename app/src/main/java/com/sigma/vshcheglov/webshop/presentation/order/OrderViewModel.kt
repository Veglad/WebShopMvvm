package com.sigma.vshcheglov.webshop.presentation.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sigma.vshcheglov.webshop.App
import com.sigma.vshcheglov.webshop.data.DataProvider
import com.sigma.vshcheglov.webshop.data.entities.mappers.BasketToOrderMapper
import com.sigma.vshcheglov.webshop.domain.Basket
import com.sigma.vshcheglov.webshop.extensions.isCardNumberValid
import com.sigma.vshcheglov.webshop.extensions.isCvvValid
import com.sigma.vshcheglov.webshop.presentation.entites.OrderCard
import com.sigma.vshcheglov.webshop.presentation.helpres.EventWithContent
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

    @Inject
    lateinit var dataProvider: DataProvider
    @Inject
    lateinit var basketToOrderMapper: BasketToOrderMapper

    private val job: Job = Job()
    private val uiCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val _stateLiveData: MutableLiveData<OrderViewState> = MutableLiveData<OrderViewState>().apply {
        value = OrderViewState()
    }
    val stateLiveData: LiveData<OrderViewState> = _stateLiveData

    private val _commandLiveData: MutableLiveData<EventWithContent<OrderCommand>> = MutableLiveData()
    val commandLiveData: LiveData<EventWithContent<OrderCommand>> = _commandLiveData

    private fun getState() = stateLiveData.value!!

    init {
        App.appComponent.inject(this)
        _stateLiveData.value = getState().copy(orderPrice = Basket.totalPriceWithDiscount)
    }

    fun makeOrder(card: OrderCard, isNetworkAvailable: Boolean) {
        uiCoroutineScope.launch {
            _stateLiveData.value = getState().copy(isLoading = true)
            val isValid = validateOrderCard(card)

            if (isNetworkAvailable) {
                if (isValid) {
                    saveOrder()
                }
            } else {
                setCommand(ShowNoInternet)
            }
            _stateLiveData.value = getState().copy(isLoading = false)
        }
    }

    private fun validateOrderCard(card: OrderCard): Boolean {
        var isValid = true
        if (card.name.length < MIN_NAME_LENGTH) {
            isValid = false
            setCommand(ShowInvalidName)
        }
        if (card.lastName.length < MIN_NAME_LENGTH) {
            isValid = false
            setCommand(ShowInvalidSecondName)
        }
        card.cardNumber = card.cardNumber.replace(" ", "")
        if (!card.cardNumber.isCardNumberValid()) {
            isValid = false
            setCommand(ShowInvalidCardNumber)
        }
        val cardMonth = card.cardMonth
        if (cardMonth == null || cardMonth !in MIN_CARD_MONTH_NUMBER..MAX_CARD_MONTH_NUMBER) {
            isValid = false
            setCommand(ShowInvalidCardMonth)
        }
        val cardYear = card.cardYear
        if (cardYear == null ||
            cardYear !in Calendar.getInstance().get(Calendar.YEAR) % 100..MAX_CARD_YEAR_NUMBER
        ) {
            isValid = false
            setCommand(ShowInvalidCardYear)
        }
        if (!card.cardCvv.isCvvValid()) {
            isValid = false
            setCommand(ShowInvalidCardCvv)
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
            setCommand(NotifyOrderCompleted)
        } catch (ex: Exception) {
            setCommand(ShowOrderSaveError(ex))
        }
    }

    private fun setCommand(command: OrderCommand) {
        _commandLiveData.value = EventWithContent(command)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}

data class OrderViewState(
    var isLoading: Boolean = false,
    var orderPrice: Double = 0.0
)

sealed class OrderCommand
class ShowOrderSaveError(val exception: Exception) : OrderCommand()
object ShowInvalidName : OrderCommand()
object ShowInvalidSecondName : OrderCommand()
object ShowInvalidCardNumber : OrderCommand()
object ShowInvalidCardMonth : OrderCommand()
object ShowInvalidCardYear : OrderCommand()
object ShowInvalidCardCvv : OrderCommand()
object ShowNoInternet : OrderCommand()
object NotifyOrderCompleted : OrderCommand()