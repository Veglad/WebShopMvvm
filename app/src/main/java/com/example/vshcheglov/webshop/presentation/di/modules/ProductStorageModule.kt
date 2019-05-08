package com.example.vshcheglov.webshop.presentation.di.modules

import com.example.vshcheglov.webshop.data.products.ProductStorage
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [MappersModule::class])
class ProductStorageModule {

    @Provides
    @Singleton
    fun providesProductStorage() = ProductStorage()
}