package com.example.vshcheglov.webshop.presentation.di.modules

import com.example.vshcheglov.webshop.data.users.UserStorage
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [MappersModule::class])
class UserStorageModule {

    @Provides
    @Singleton
    fun providesUserStorage() = UserStorage()
}