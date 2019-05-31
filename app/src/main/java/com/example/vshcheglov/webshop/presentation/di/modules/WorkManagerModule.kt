package com.example.vshcheglov.webshop.presentation.di.modules

import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.presentation.helpres.Encryptor
import com.example.vshcheglov.webshop.presentation.main.helpers.AvatarWorkerFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [DataProviderModule::class])
class WorkManagerModule {

    @Provides
    @Singleton
    fun providesWorkerFactory(dataProvider: DataProvider) = AvatarWorkerFactory(dataProvider)
}