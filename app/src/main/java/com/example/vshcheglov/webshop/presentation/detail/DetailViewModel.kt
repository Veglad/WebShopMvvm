package com.example.vshcheglov.webshop.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vshcheglov.webshop.domain.Basket
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.presentation.helpres.Event

class DetailViewModel : ViewModel() {

    private lateinit var product: Product

    private val _liveDataStartBasketScreen = MutableLiveData<Event>()
    val liveDataStartBasketScreen: LiveData<Event> = _liveDataStartBasketScreen

    private val _liveDataProductInfo = MutableLiveData<Product>()
    val liveDataProductInfo: LiveData<Product> = _liveDataProductInfo

    fun showProductInfo(product: Product?) {
        this.product = product ?: Product()
        _liveDataProductInfo.value = this.product
    }

    fun buyProduct() {
        Basket.addProduct(product)
        _liveDataStartBasketScreen.value = Event()
    }
}