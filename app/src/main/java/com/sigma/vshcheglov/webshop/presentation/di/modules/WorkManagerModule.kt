package com.sigma.vshcheglov.webshop.presentation.di.modules

import com.sigma.vshcheglov.webshop.data.DataProvider
import com.sigma.vshcheglov.webshop.presentation.main.helpers.AvatarWorkerFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [DataProviderModule::class])
class WorkManagerModule {

    @Provides
    @Singleton
    fun providesWorkerFactory(dataProvider: DataProvider) = AvatarWorkerFactory(dataProvider)
}