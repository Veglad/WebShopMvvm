package com.sigma.vshcheglov.webshop.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sigma.vshcheglov.webshop.domain.Basket
import com.sigma.vshcheglov.webshop.domain.Product
import com.sigma.vshcheglov.webshop.presentation.helpres.EventWithContent

class DetailViewModel : ViewModel() {

    private val _stateLiveData: MutableLiveData<DetailViewState> = MutableLiveData<DetailViewState>().apply {
        value = DetailViewState()
    }
    val stateLiveData: LiveData<DetailViewState> = _stateLiveData

    private val _commandLiveData: MutableLiveData<EventWithContent<DetailCommand>> = MutableLiveData()
    val commandLiveData: LiveData<EventWithContent<DetailCommand>> = _commandLiveData

    val basketItems = Basket.productsNumber

    private fun getState() = stateLiveData.value!!

    fun showProductInfo(product: Product?) {
        _stateLiveData.value = getState().copy(product = product ?: Product())
    }


    fun buyProduct() {
        Basket.addProduct(getState().product)
    }
}

data class DetailViewState(
    var product: Product = Product()
)

sealed class DetailCommand
object StartBasketScreen : DetailCommand()