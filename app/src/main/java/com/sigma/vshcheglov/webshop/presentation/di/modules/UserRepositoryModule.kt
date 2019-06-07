package com.sigma.vshcheglov.webshop.presentation.di.modules

import com.sigma.vshcheglov.webshop.data.users.UserRepository
import com.sigma.vshcheglov.webshop.data.users.UserNetworkDataSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        FirebaseModule::class,
        MappersModule::class,
        UserStorageModule::class,
        UserCredentialsStorageModule::class
    ]
)
class UserRepositoryModule {
    @Provides
    @Singleton
    fun providesRepository(): UserRepository = UserRepository()

    @Provides
    @Singleton
    fun providesNetworkDataSource() = UserNetworkDataSource()
}