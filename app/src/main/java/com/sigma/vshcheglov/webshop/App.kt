package com.sigma.vshcheglov.webshop

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.sigma.vshcheglov.webshop.presentation.di.components.AppComponent
import com.sigma.vshcheglov.webshop.presentation.di.components.DaggerAppComponent
import com.sigma.vshcheglov.webshop.presentation.di.modules.*
import com.sigma.vshcheglov.webshop.presentation.main.helpers.AvatarWorkerFactory
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber
import javax.inject.Inject


class App : Application() {

    companion object {
        lateinit var appComponent: AppComponent
    }

    @Inject
    lateinit var avatarWorkerFactory: AvatarWorkerFactory

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .productNetworkModule(ProductNetworkModule())
            .productStorageModule(ProductStorageModule())
            .userStorageModule(UserStorageModule())
            .userCredentialsStorageModule(UserCredentialsStorageModule())
            .appModule(AppModule(this))
            .imagePickHelperModule(ImagePickHelperModule())
            .mappersModule(MappersModule())
            .dataProviderModule(DataProviderModule())
            .workManagerModule(WorkManagerModule())
            .encryptorModule(EncryptorModule())
            .build()

        appComponent.inject(this)

        initTimber()
        initRealmConfiguration()
        initWorkerFactory()
    }

    private fun initWorkerFactory() {
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(avatarWorkerFactory)
                .build()
        )
    }

    private fun initRealmConfiguration() {
        Realm.init(this)
        val config = RealmConfiguration.Builder().build()
        Realm.setDefaultConfiguration(config)
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}