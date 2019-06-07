package com.sigma.vshcheglov.webshop.presentation.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val applicationContext: Context) {

    @Provides
    @Singleton
    fun providesApplicationContext() = applicationContext
}