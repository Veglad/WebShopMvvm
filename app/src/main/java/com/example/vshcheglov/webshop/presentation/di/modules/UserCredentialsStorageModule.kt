package com.example.vshcheglov.webshop.presentation.di.modules

import com.example.vshcheglov.webshop.data.users.UserCredentialsStorage
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UserCredentialsStorageModule {

    @Provides
    @Singleton
    fun providesUserCredentialsStorage() = UserCredentialsStorage()
}