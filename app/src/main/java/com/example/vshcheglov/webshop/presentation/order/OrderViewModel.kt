package com.example.vshcheglov.webshop.presentation.order

import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.data.entities.mappers.BasketToOrderMapper
import com.example.vshcheglov.webshop.domain.Basket
import com.example.vshcheglov.webshop.extensions.isCardNumberValid
import com.example.vshcheglov.webshop.extensions.isCvvValid
import com.example.vshcheglov.webshop.presentation.entites.OrderCard
import kotlinx.coroutines.*
import nucleus5.presenter.Presenter
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class OrderViewModel : Presenter<OrderViewModel.OrderView>() {

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

    private lateinit var job: Job
    private lateinit var uiCoroutineScope: CoroutineScope

    init {
        App.appComponent.inject(this)
    }

    fun initOrderPrice() {
        val orderPrice = Basket.totalPriceWithDiscount
        view?.setOrderPrice(orderPrice)
    }

    fun makeOrder(card: OrderCard, isNetworkAvailable: Boolean) {
        uiCoroutineScope.launch {
            view?.let {
                it.setShowProgress(true)
                val isValid = validateOrderCard(card, it)

                if (isNetworkAvailable) {
                    if (isValid) {
                        saveOrder()
                    }
                } else {
                    it.showNoInternetError()
                }
                it.setShowProgress(false)
            }
        }
    }

    private fun validateOrderCard(card: OrderCard, view: OrderView): Boolean {
        var isValid = true
        if (card.name.length < MIN_NAME_LENGTH) {
            isValid = false
            view.showInvalidName()
        }
        if (card.lastName.length < MIN_NAME_LENGTH) {
            isValid = false
            view.showInvalidSecondName()
        }
        card.cardNumber = card.cardNumber.replace(" ", "")
        if (!card.cardNumber.isCardNumberValid()) {
            isValid = false
            view.showInvalidCardNumber()
        }
        val cardMonth = card.cardMonth
        if (cardMonth == null || cardMonth !in MIN_CARD_MONTH_NUMBER..MAX_CARD_MONTH_NUMBER) {
            isValid = false
            view.showInvalidCardMonth()
        }
        val cardYear = card.cardYear
        if (cardYear == null ||
            cardYear !in Calendar.getInstance().get(Calendar.YEAR) % 100..MAX_CARD_YEAR_NUMBER
        ) {
            isValid = false
            view.showInvalidCardYear()
        }
        if (!card.cardCvv.isCvvValid()) {
            isValid = false
            view.showInvalidCardCvv()
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
            view?.notifyOrderCompleted()
        } catch (ex: Exception) {
            view?.showOrderSaveError()
        }
    }

    private fun initCoroutineJob() {
        job = Job()
        uiCoroutineScope = CoroutineScope(Dispatchers.Main + job)
    }

    override fun onTakeView(view: OrderView?) {
        super.onTakeView(view)
        initCoroutineJob()
    }

    override fun onDropView() {
        super.onDropView()
        job.cancel()
    }

    interface OrderView {
        fun setOrderPrice(orderPrice: Double)

        fun setShowProgress(isVisible: Boolean)

        fun showInvalidName()

        fun showInvalidSecondName()

        fun showInvalidCardNumber()

        fun showInvalidCardMonth()

        fun showInvalidCardYear()

        fun showInvalidCardCvv()

        fun showNoInternetError()

        fun notifyOrderCompleted()

        fun showOrderSaveError()
    }
}