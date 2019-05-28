package com.example.vshcheglov.webshop.presentation.basket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.domain.Basket
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.presentation.entites.ProductBasketCard
import com.example.vshcheglov.webshop.presentation.entites.TotalProductPriceTitle
import com.example.vshcheglov.webshop.presentation.entites.mappers.ProductBasketCardMapper
import com.example.vshcheglov.webshop.presentation.helpres.Event
import com.example.vshcheglov.webshop.presentation.helpres.EventWithContent
import javax.inject.Inject
import kotlin.properties.Delegates

class BasketViewModel : ViewModel() {

    @Inject
    lateinit var productBasketCardMapper: ProductBasketCardMapper
    private lateinit var productToCount: Pair<Product, Int>
    private var deletedIndex by Delegates.notNull<Int>()

    interface BasketView {
        fun setTotalProductPriceTitle(position: Int, totalPrice: Double, percentageDiscount: Double)
    }

    private val _liveDataBasketAmount = MutableLiveData<Double>()
    val liveDataBasketAmount: LiveData<Double> = _liveDataBasketAmount

    private val _liveDataBasketItemNumber = MutableLiveData<String>()
    val liveDataBasketItemNumber: LiveData<String> = _liveDataBasketItemNumber

    private val _liveDataBasket = MutableLiveData<MutableList<ProductBasketCard>>()
    val liveDataBasket: LiveData<MutableList<ProductBasketCard>> = _liveDataBasket

    private val _liveDataBasketIsEmpty = MutableLiveData<Boolean>()
    val liveDataBasketIsEmpty: LiveData<Boolean> = _liveDataBasketIsEmpty

    private val _liveDataSameProductNumber = MutableLiveData<Pair<Int, Int>>()
    val liveDataSameProductNumber: LiveData<Pair<Int, Int>> = _liveDataSameProductNumber

    private val _liveDataTotalProductPrice = MutableLiveData<Pair<Int, Double>>()
    val liveDataTotalProductPrice: LiveData<Pair<Int, Double>> = _liveDataTotalProductPrice

    private val _liveDataTotalProductPriceTitle = MutableLiveData<TotalProductPriceTitle>()
    val liveDataTotalProductPriceTitle: LiveData<TotalProductPriceTitle> = _liveDataTotalProductPriceTitle

    private val _liveDataStartOrderScreen = MutableLiveData<Event>()
    val liveDataStartOrderScreen: LiveData<Event> = _liveDataStartOrderScreen

    private val _liveDataRemoveItem = MutableLiveData<EventWithContent<Int>>()
    val liveDataRemoveItem: LiveData<EventWithContent<Int>> = _liveDataRemoveItem

    private val _liveDataRestoreItem = MutableLiveData<EventWithContent<Int>>()
    val liveDataRestoreItem: LiveData<EventWithContent<Int>> = _liveDataRestoreItem

    init {
        App.appComponent.inject(this)
    }

    fun makeOrder() {
        _liveDataStartOrderScreen.value = Event()
    }

    fun initProductListWithBasketInfo() {
        updateBasketInfo()

        val isBasketEmpty = Basket.productsNumber == 0
        _liveDataBasketIsEmpty.value = isBasketEmpty
        if (!isBasketEmpty) {
            _liveDataBasket.value = productBasketCardMapper.map(Basket)
        }
    }

    fun productNumberIncreased(position: Int) {
        Basket.incrementProductCount(position)
        cardAndBasketUpdate(position)
    }

    fun productNumberDecreased(position: Int) {
        if (Basket.decrementProductCountIfAble(position)) {
            cardAndBasketUpdate(position)
        }
    }

    private fun cardAndBasketUpdate(position: Int) {
        val updatedProductToCount = Basket.productToCountList[position]
        val productCount = updatedProductToCount.second
        val product = updatedProductToCount.first

        updateBasketInfo()

        _liveDataSameProductNumber.value = position to productCount
        _liveDataTotalProductPrice.value = position to Basket.getSameProductDiscountPrice(product.id)
        if (product.percentageDiscount > 0) {
            _liveDataTotalProductPriceTitle.value = TotalProductPriceTitle(
                position,
                Basket.getSameProductPrice(product.id),
                product.percentageDiscount.toDouble()
            )
        }
    }


    private fun updateBasketInfo() {
        _liveDataBasketAmount.value = Basket.totalPriceWithDiscount
        _liveDataBasketItemNumber.value = Basket.productsNumber.toString()
    }

    fun removeProductFromBasket(position: Int) {
        productToCount = Basket.productToCountList[position]
        deletedIndex = position

        Basket.removeSameProducts(position)

        _liveDataRemoveItem.value = EventWithContent(position)
        _liveDataBasketIsEmpty.value = Basket.productsNumber == 0

        updateBasketInfo()
    }

    fun restoreProductCard() {
        Basket.addProductToCountEntry(productToCount, deletedIndex)
        _liveDataBasketIsEmpty.value = Basket.productsNumber == 0
        _liveDataRestoreItem.value = EventWithContent(deletedIndex)
        updateBasketInfo()
    }
}