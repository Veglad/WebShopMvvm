package com.example.vshcheglov.webshop.data.products

import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.domain.Product
import javax.inject.Inject

class ProductRepository {

    @Inject
    lateinit var networkDataSource: ProductNetworkDataSource

    @Inject
    lateinit var storage: ProductStorage

    init {
        App.appComponent.inject(this)
    }

    suspend fun getProducts(): MutableList<Product> {
        var productList: MutableList<Product>
        try {
            productList = networkDataSource.getProducts()
            storage.saveProductsToDb(productList)
        } catch (e: Exception) {
            productList = storage.getProductsFromDb()
        }

        if (productList.isEmpty()) {
            productList = storage.getProductsFromDb()
        }

        return productList
    }

    suspend fun getPromotionalProducts() : MutableList<Product> {
        var productList: MutableList<Product>
        try {
            productList = networkDataSource.getPromotionalProducts()
            productList = productList.filter { it.percentageDiscount > 0 }.toMutableList()
            storage.saveProductsToDb(productList)
        } catch (e: Exception) {
            productList = storage.getProductsFromDb(true)
        }

        return productList
    }
}