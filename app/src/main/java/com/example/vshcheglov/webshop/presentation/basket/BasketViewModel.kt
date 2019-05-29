package com.example.vshcheglov.webshop.presentation.basket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.domain.Basket
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.presentation.entites.ProductBasketCard
import com.example.vshcheglov.webshop.presentation.entites.BasketCardPriceInfo
import com.example.vshcheglov.webshop.presentation.entites.mappers.ProductBasketCardMapper
import com.example.vshcheglov.webshop.presentation.helpres.EventWithContent
import javax.inject.Inject
import kotlin.properties.Delegates

class BasketViewModel : ViewModel() {

    @Inject
    lateinit var productBasketCardMapper: ProductBasketCardMapper
    private lateinit var productToCount: Pair<Product, Int>
    private var deletedIndex by Delegates.notNull<Int>()

    private val _stateLiveData: MutableLiveData<BasketViewState> = MutableLiveData<BasketViewState>().apply {
        value = BasketViewState()
    }
    val stateLiveData: LiveData<BasketViewState> = _stateLiveData

    private val _commandLiveData: MutableLiveData<EventWithContent<BasketCommand>> = MutableLiveData()
    val commandLiveData: LiveData<EventWithContent<BasketCommand>> = _commandLiveData

    private fun getState() = stateLiveData.value!!

    init {
        App.appComponent.inject(this)
        initProductListWithBasketInfo()
    }

    fun makeOrder() {
        setCommand(BasketCommand.StartOrderScreen)
    }

    private fun initProductListWithBasketInfo() {
        val state = getUpdatedStateWithBasketInfo()
        state.isBasketEmpty = Basket.productsNumber == 0

        if (!state.isBasketEmpty) {
            val basketCardList = productBasketCardMapper.map(Basket)
            setCommand(BasketCommand.ShowBasketCardList(basketCardList))
        }

        _stateLiveData.value = state
    }

    fun productNumberIncreased(position: Int) {
        Basket.incrementProductCount(position)
        _stateLiveData.value = getUpdatedStateWithBasketInfo()
        updateBasketCardPriceInfo(position, Basket.productToCountList[position])
    }

    fun productNumberDecreased(position: Int) {
        if (Basket.decrementProductCountIfAble(position)) {
            _stateLiveData.value = getUpdatedStateWithBasketInfo()
            updateBasketCardPriceInfo(position, Basket.productToCountList[position])
        }
    }

    // TODO: Refactor
    private fun updateBasketCardPriceInfo(position: Int, updatedProductToCount: Pair<Product, Int>) {
        val productCount = updatedProductToCount.second
        val product = updatedProductToCount.first

        val basketCardPriceInfo = BasketCardPriceInfo(
            position,
            Basket.getSameProductPrice(product.id),
            product.percentageDiscount.toDouble(),
            productCount,
            Basket.getSameProductDiscountPrice(product.id)
        )

        setCommand(BasketCommand.UpdateBasketCardPriceInfo(basketCardPriceInfo))
    }


    private fun getUpdatedStateWithBasketInfo() = getState().copy(
        basketAmount = Basket.totalPriceWithDiscount,
        basketItemNumber = Basket.productsNumber
    )

    // Pass deleted card item as a parameter, restore it as event with Pair<position, data>
    fun removeProductFromBasket(position: Int) {
        productToCount = Basket.productToCountList[position] // TODO: Refactor
        deletedIndex = position // TODO: Refactor

        Basket.removeSameProducts(position)

        setCommand(BasketCommand.RemoveBasketCard(position))

        _stateLiveData.value = getUpdatedStateWithBasketInfo().also { state ->
            state.isBasketEmpty = Basket.productsNumber == 0
        }
    }

    fun restoreProductCard() {
        Basket.addProductToCountEntry(productToCount, deletedIndex)

        _stateLiveData.value = getUpdatedStateWithBasketInfo().also { state ->
            state.isBasketEmpty = Basket.productsNumber == 0
        }

        setCommand(BasketCommand.RestoreBasketCard(deletedIndex))
    }

    private fun setCommand(command: BasketCommand) {
        _commandLiveData.value = EventWithContent(command)
    }
}

data class BasketViewState(
    var basketAmount: Double = 0.0,
    var basketItemNumber: Int = 0,
    var isBasketEmpty: Boolean = true
)

sealed class BasketCommand {
    class RemoveBasketCard(val position: Int) : BasketCommand()
    class RestoreBasketCard(val position: Int) : BasketCommand()
    class UpdateBasketCardPriceInfo(val basketCardPriceInfo: BasketCardPriceInfo) : BasketCommand()
    class ShowBasketCardList(val basketCards: MutableList<ProductBasketCard>) : BasketCommand()
    object StartOrderScreen : BasketCommand()
}