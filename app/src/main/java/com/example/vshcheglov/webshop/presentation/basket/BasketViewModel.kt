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

    private val _basketAmount = MutableLiveData<Double>()
    val basketAmount: LiveData<Double> = _basketAmount

    private val _basketItemNumber = MutableLiveData<String>()
    val basketItemNumber: LiveData<String> = _basketItemNumber

    private val _basket = MutableLiveData<MutableList<ProductBasketCard>>()
    val basket: LiveData<MutableList<ProductBasketCard>> = _basket

    private val _basketIsEmpty = MutableLiveData<Boolean>()
    val basketIsEmpty: LiveData<Boolean> = _basketIsEmpty

    private val _sameProductNumber = MutableLiveData<Pair<Int, Int>>()
    val sameProductNumber: LiveData<Pair<Int, Int>> = _sameProductNumber

    private val _totalProductPrice = MutableLiveData<Pair<Int, Double>>()
    val totalProductPrice: LiveData<Pair<Int, Double>> = _totalProductPrice

    private val _totalProductPriceTitle = MutableLiveData<TotalProductPriceTitle>()
    val totalProductPriceTitle: LiveData<TotalProductPriceTitle> = _totalProductPriceTitle

    private val _startOrderScreen = MutableLiveData<Event>()
    val startOrderScreen: LiveData<Event> = _startOrderScreen

    private val _removeItem = MutableLiveData<EventWithContent<Int>>()
    val removeItem: LiveData<EventWithContent<Int>> = _removeItem

    private val _restoreItem = MutableLiveData<EventWithContent<Int>>()
    val restoreItem: LiveData<EventWithContent<Int>> = _restoreItem

    init {
        App.appComponent.inject(this)
    }

    fun makeOrder() {
        _startOrderScreen.value = Event()
    }

    fun initProductListWithBasketInfo() {
        updateBasketInfo()

        val isBasketEmpty = Basket.productsNumber == 0
        _basketIsEmpty.value = isBasketEmpty
        if (!isBasketEmpty) {
            _basket.value = productBasketCardMapper.map(Basket)
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

        _sameProductNumber.value = position to productCount
        _totalProductPrice.value = position to Basket.getSameProductDiscountPrice(product.id)
        if (product.percentageDiscount > 0) {
            _totalProductPriceTitle.value = TotalProductPriceTitle(
                position,
                Basket.getSameProductPrice(product.id),
                product.percentageDiscount.toDouble()
            )
        }
    }


    private fun updateBasketInfo() {
        _basketAmount.value = Basket.totalPriceWithDiscount
        _basketItemNumber.value = Basket.productsNumber.toString()
    }

    fun removeProductFromBasket(position: Int) {
        productToCount = Basket.productToCountList[position]
        deletedIndex = position

        Basket.removeSameProducts(position)

        _removeItem.value = EventWithContent(position)
        _basketIsEmpty.value = Basket.productsNumber == 0

        updateBasketInfo()
    }

    fun restoreProductCard() {
        Basket.addProductToCountEntry(productToCount, deletedIndex)
        _basketIsEmpty.value = Basket.productsNumber == 0
        _restoreItem.value = EventWithContent(deletedIndex)
        updateBasketInfo()
    }
}