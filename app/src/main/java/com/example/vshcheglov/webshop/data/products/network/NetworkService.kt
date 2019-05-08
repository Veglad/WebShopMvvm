package com.example.vshcheglov.webshop.data.products.network

import android.content.Context
import com.example.vshcheglov.webshop.data.products.ProductNetworkDataSource
import com.example.vshcheglov.webshop.extensions.isNetworkAvailable
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

object NetworkService {
    private const val MAX_LIFE = 60 * 60 * 24 * 30

    fun createRetrofit(context: Context) : Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().also {
            it.level = HttpLoggingInterceptor.Level.BODY
        }

        val onlineInterceptor = Interceptor { chain ->
            chain.proceed(chain.request())
                .newBuilder()
                .removeHeader("Pragma")
                .build()
        }

        val offlineInterceptor = Interceptor { chain ->
            var request = chain.request()

            if (!context.isNetworkAvailable()) {
                request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + MAX_LIFE)
                    .removeHeader("Pragma")
                    .build()
            }

            chain.proceed(request)
        }

        val cache = Cache(File(context.cacheDir, "http-cache"), 10 * 1024 * 1024)

        val client = OkHttpClient().newBuilder()
            .addInterceptor(loggingInterceptor)
            //.addInterceptor(offlineInterceptor)
            //.addInterceptor(onlineInterceptor)
            .cache(cache)
            .build()

        return Retrofit.Builder()
            .baseUrl(ProductNetworkDataSource.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }
}