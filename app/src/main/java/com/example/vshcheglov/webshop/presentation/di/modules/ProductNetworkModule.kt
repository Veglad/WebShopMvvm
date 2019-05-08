package com.example.vshcheglov.webshop.presentation.di.modules

import android.content.Context
import com.example.vshcheglov.webshop.data.products.network.NetworkService
import com.example.vshcheglov.webshop.data.products.network.WebShopApi
import com.example.vshcheglov.webshop.data.products.ProductNetworkDataSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AppModule::class, MappersModule::class])
class ProductNetworkModule {
    @Singleton
    @Provides
    fun provideShopApi(context: Context): WebShopApi {
        val retrofit = NetworkService.createRetrofit(context)
        return retrofit.create(WebShopApi::class.java)
    }

    @Provides
    @Singleton
    fun providesNetworkDataSource() = ProductNetworkDataSource()
}