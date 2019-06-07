package com.sigma.vshcheglov.webshop.presentation.di.modules

import com.sigma.vshcheglov.webshop.data.entities.mappers.*
import com.sigma.vshcheglov.webshop.data.entities.mappers.ResponseUserMapper
import com.sigma.vshcheglov.webshop.presentation.entites.mappers.ProductBasketCardMapper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MappersModule {

    @Singleton
    @Provides
    fun povideProductBasketCardMapper() = ProductBasketCardMapper()

    @Singleton
    @Provides
    fun provideNetworkProductMapper() = ResponseProductMapper()

    @Singleton
    @Provides
    fun provideBasketToOrderMapper() = BasketToOrderMapper()

    @Singleton
    @Provides
    fun provideResponseUserMapper() = ResponseUserMapper()

    @Singleton
    @Provides
    fun provideRealmProductMapper() = RealmProductMapper()


    @Singleton
    @Provides
    fun provideResponseOrderMapper() = ResponseOrderMapper()

    @Singleton
    @Provides
    fun provideRealmResponseOrderMapper() = RealmResponseOrderMapper()

    @Singleton
    @Provides
    fun provideRealmOrderMapper() = RealmOrderMapper()

    @Singleton
    @Provides
    fun provideRealmOrderProductMapper() = RealmOrderProductMapper()

    @Singleton
    @Provides
    fun provideResponseOrderProductMapper() = ResponseOrderProductMapper()

    @Singleton
    @Provides
    fun provideRealmResponseOrderProductMapper() = RealmResponseOrderProductMapper()

    @Singleton
    @Provides
    fun provideRealmUserCredentialsMapper() = RealmUserCredentialsMapper()
}
