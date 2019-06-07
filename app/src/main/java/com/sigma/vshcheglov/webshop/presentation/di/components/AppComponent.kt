package com.sigma.vshcheglov.webshop.presentation.di.components

import com.sigma.vshcheglov.webshop.App
import com.sigma.vshcheglov.webshop.SplashActivity
import com.sigma.vshcheglov.webshop.data.DataProvider
import com.sigma.vshcheglov.webshop.data.entities.mappers.RealmOrderMapper
import com.sigma.vshcheglov.webshop.data.entities.mappers.RealmResponseOrderMapper
import com.sigma.vshcheglov.webshop.data.entities.mappers.ResponseOrderMapper
import com.sigma.vshcheglov.webshop.data.products.ProductNetworkDataSource
import com.sigma.vshcheglov.webshop.data.products.ProductRepository
import com.sigma.vshcheglov.webshop.data.products.ProductStorage
import com.sigma.vshcheglov.webshop.data.users.UserRepository
import com.sigma.vshcheglov.webshop.data.users.UserNetworkDataSource
import com.sigma.vshcheglov.webshop.data.users.UserStorage
import com.sigma.vshcheglov.webshop.presentation.basket.BasketViewModel
import com.sigma.vshcheglov.webshop.presentation.purchase.PurchaseViewModel
import com.sigma.vshcheglov.webshop.presentation.di.modules.*
import com.sigma.vshcheglov.webshop.presentation.login.LoginViewModel
import com.sigma.vshcheglov.webshop.presentation.main.MainViewModel
import com.sigma.vshcheglov.webshop.presentation.order.OrderViewModel
import com.sigma.vshcheglov.webshop.presentation.registration.RegisterViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DataProviderModule::class,
        MappersModule::class,
        EncryptorModule::class,
        WorkManagerModule::class,
        ImagePickHelperModule::class
    ]
)
interface AppComponent {
    fun inject(mainPresenter: MainViewModel)
    fun inject(app: App)
    fun inject(splashActivity: SplashActivity)
    fun inject(basketPresenter: BasketViewModel)
    fun inject(networkDataSource: ProductNetworkDataSource)
    fun inject(productRepository: ProductRepository)
    fun inject(loginPresenter: LoginViewModel)
    fun inject(registerPresenter: RegisterViewModel)
    fun inject(orderPresenter: OrderViewModel)
    fun inject(boughtPresenter: PurchaseViewModel)
    fun inject(usersRepository: UserRepository)
    fun inject(userNetwork: UserNetworkDataSource)
    fun inject(dataProvider: DataProvider)
    fun inject(productStorage: ProductStorage)
    fun inject(userStorage: UserStorage)
    fun inject(realmOrderMapper: RealmOrderMapper)
    fun inject(responseOrderMapper: ResponseOrderMapper)
    fun inject(realmResponseOrderMapper: RealmResponseOrderMapper)
}