package com.example.vshcheglov.webshop.presentation.di.modules

import android.content.Context
import com.example.vshcheglov.webshop.presentation.helpers.ImagePickHelper
import com.example.vshcheglov.webshop.presentation.helpres.Encryptor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AppModule::class])
class ImagePickHelperModule {

    @Provides
    @Singleton
    fun providesImagePickHelper(context: Context) = ImagePickHelper(context)
}