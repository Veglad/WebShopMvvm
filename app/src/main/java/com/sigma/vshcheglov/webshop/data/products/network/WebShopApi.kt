package com.sigma.vshcheglov.webshop.data.products.network

import com.sigma.vshcheglov.webshop.data.entities.ProductResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path

interface WebShopApi {

    @GET("/products")
    fun getProductsAsync(): Deferred<List<ProductResponse>>

    @Deprecated("Does not work")
    @GET("/api/DeviceData/{deviceId}")
    fun getDeviceAsync(@Path("deviceId") id: Long): Deferred<ProductResponse>

    @GET("/products")
    fun getPromotionalProductsAsync(): Deferred<List<ProductResponse>>

}