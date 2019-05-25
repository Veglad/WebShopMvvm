package com.example.vshcheglov.webshop.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vshcheglov.webshop.domain.Basket
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.presentation.helpres.Event

class DetailViewModel : ViewModel() {

    private lateinit var product: Product

    private val _startBasketScreen = MutableLiveData<Event>()
    val startBasketScreen: LiveData<Event> = _startBasketScreen

    private val _productInfo = MutableLiveData<Product>()
    val productInfo: LiveData<Product> = _productInfo

    fun showProductInfo(product: Product?) {
        this.product = product ?: Product()
        _productInfo.value = this.product
    }

    fun buyProduct() {
        Basket.addProduct(product)
        _startBasketScreen.value = Event()
    }
}