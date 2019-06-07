package com.sigma.vshcheglov.webshop.presentation.di.modules

import com.sigma.vshcheglov.webshop.data.users.UserStorage
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [MappersModule::class])
class UserStorageModule {

    @Provides
    @Singleton
    fun providesUserStorage() = UserStorage()
}