package com.sigma.vshcheglov.webshop.presentation.di.modules

import android.content.Context
import com.sigma.vshcheglov.webshop.presentation.helpers.ImagePickHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AppModule::class])
class ImagePickHelperModule {

    @Provides
    @Singleton
    fun providesImagePickHelper(context: Context) = ImagePickHelper(context)
}