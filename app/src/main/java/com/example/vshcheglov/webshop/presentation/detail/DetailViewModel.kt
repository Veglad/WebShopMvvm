package com.example.vshcheglov.webshop.presentation.detail

import com.example.vshcheglov.webshop.domain.Basket
import com.example.vshcheglov.webshop.domain.Product
import nucleus5.presenter.Presenter

class DetailViewModel : Presenter<DetailViewModel.DetailView>() {

    private lateinit var product: Product

    fun showProductInfo(product: Product?) {
        this.product = product ?: Product()
        view?.showProductInfo(this.product)
    }

    fun buyProduct() {
        Basket.addProduct(product)
        view?.startBasketActivity()
    }

    interface DetailView {
        fun startBasketActivity()

        fun showProductInfo(product: Product)
    }
}