package com.example.vshcheglov.webshop.presentation.di.modules

import com.example.vshcheglov.webshop.data.products.ProductRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ProductNetworkModule::class, ProductStorageModule::class])
class ProductRepositoryModule {
    @Provides
    @Singleton
    fun providesRepository() = ProductRepository()
}