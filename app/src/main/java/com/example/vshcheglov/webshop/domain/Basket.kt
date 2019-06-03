package com.example.vshcheglov.webshop.domain

import androidx.lifecycle.MutableLiveData

object Basket {

    var productToCountList = mutableListOf<Pair<Product, Int>>()
        private set

    var size = 0
        get() = productToCountList.size
        private set

    var productsNumber: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }

    var totalPrice = 0.0

    var totalPriceWithDiscount = 0.0

    fun addProduct(product: Product, position: Int = size) {
        val productIndex = productToCountList.indexOfFirst { it.first.id == product.id }
        if (productIndex == -1) {
            productToCountList.add(position, Pair(product, 1))

            updateBasketWithNewProduct(product)
        } else {
            incrementProductCount(productIndex)
        }
    }

    fun incrementProductCount(position: Int) {
        val productToCount = productToCountList[position]
        val updatedProductToCount = Pair(productToCount.first, productToCount.second + 1)
        productToCountList[position] = updatedProductToCount

        updateBasketWithNewProduct(productToCount.first)
    }

    private fun updateBasketWithNewProduct(product: Product) {
        productsNumber.value?.let {
            productsNumber.value = it + 1
        }
        totalPrice += product.price
        totalPriceWithDiscount += product.priceWithDiscount
    }

    fun removeProductIfAble(product: Product): Boolean {
        val productIndex = productToCountList.indexOfFirst { it.first.id == product.id }
        return decrementProductCountIfAble(productIndex)
    }

    fun decrementProductCountIfAble(position: Int): Boolean {
        val productToCount = productToCountList[position]
        if (productToCount.second > 1) {
            val updatedProductToCount = Pair(productToCount.first, productToCount.second - 1)
            productToCountList[position] = updatedProductToCount

            updateBasketWithRemovedProduct(productToCount)
            return true
        }

        return false
    }

    private fun updateBasketWithRemovedProduct(productToCount: Pair<Product, Int>) {
        productsNumber.value?.let { productsNumber.value = it - 1 }
        totalPrice -= productToCount.first.price
        totalPriceWithDiscount -= productToCount.first.priceWithDiscount
    }

    fun getSameProductPrice(productId: Int): Double {
        val productIndex = productToCountList.indexOfFirst { it.first.id == productId }
        return productToCountList[productIndex].first.price * productToCountList[productIndex].second
    }


    fun getSameProductDiscountPrice(productId: Int): Double {
        val productIndex = productToCountList.indexOfFirst { it.first.id == productId }
        return productToCountList[productIndex].first.priceWithDiscount * productToCountList[productIndex].second
    }


    fun removeSameProducts(index: Int) {
        val productToCount = productToCountList.removeAt(index)

        updateBasketWithRemovedEntry(productToCount)
    }

    private fun updateBasketWithRemovedEntry(productToCount: Pair<Product, Int>) {
        productsNumber.value?.let { productsNumber.value = it - productToCount.second }
        totalPrice -= productToCount.first.price * productToCount.second
        totalPriceWithDiscount -= productToCount.first.priceWithDiscount * productToCount.second
    }

    fun addProductToCountEntry(productToCount: Pair<Product, Int>, position: Int) {
        productToCountList.add(position, productToCount)

        updateBasketWithNewEntry(productToCount)
    }

    private fun updateBasketWithNewEntry(productToCount: Pair<Product, Int>) {
        productsNumber.value?.let { productsNumber.value = it + productToCount.second }
        totalPrice += productToCount.first.price * productToCount.second
        totalPriceWithDiscount += productToCount.first.priceWithDiscount * productToCount.second
    }

    fun clear() {
        size = 0
        productToCountList.clear()
        productsNumber.value = 0
        totalPrice = 0.0
        totalPriceWithDiscount = 0.0
    }
}