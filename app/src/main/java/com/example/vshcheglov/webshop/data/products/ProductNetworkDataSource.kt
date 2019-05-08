package com.example.vshcheglov.webshop.data.products

import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.entities.mappers.ResponseProductMapper
import com.example.vshcheglov.webshop.data.products.network.WebShopApi
import javax.inject.Inject

class ProductNetworkDataSource {

    @Inject lateinit var productEntityDataMapper: ResponseProductMapper
    @Inject lateinit var webShopApi: WebShopApi

    companion object {
        const val BASE_URL = "https://us-central1-webshop-58013.cloudfunctions.net"
    }

    init {
        App.appComponent.inject(this)
    }

    suspend fun getProducts() =
        productEntityDataMapper.map(webShopApi.getProductsAsync().await())

    suspend fun getPromotionalProducts() =
        productEntityDataMapper.map(webShopApi.getPromotionalProductsAsync().await())
}