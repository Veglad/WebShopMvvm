package com.sigma.vshcheglov.webshop.presentation.di.modules

import com.sigma.vshcheglov.webshop.presentation.helpres.Encryptor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class EncryptorModule() {

    @Provides
    @Singleton
    fun providesEncryptor() = Encryptor()
}