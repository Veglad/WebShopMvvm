package com.example.vshcheglov.webshop.presentation.di.components

import com.example.vshcheglov.webshop.data.DataProvider
import com.example.vshcheglov.webshop.data.entities.mappers.RealmOrderMapper
import com.example.vshcheglov.webshop.data.entities.mappers.RealmResponseOrderMapper
import com.example.vshcheglov.webshop.data.entities.mappers.ResponseOrderMapper
import com.example.vshcheglov.webshop.data.products.ProductNetworkDataSource
import com.example.vshcheglov.webshop.data.products.ProductRepository
import com.example.vshcheglov.webshop.data.products.ProductStorage
import com.example.vshcheglov.webshop.data.users.UserRepository
import com.example.vshcheglov.webshop.data.users.UserNetworkDataSource
import com.example.vshcheglov.webshop.data.users.UserStorage
import com.example.vshcheglov.webshop.presentation.basket.BasketPresenter
import com.example.vshcheglov.webshop.presentation.purchase.PurchasePresenter
import com.example.vshcheglov.webshop.presentation.di.modules.*
import com.example.vshcheglov.webshop.presentation.login.LoginPresenter
import com.example.vshcheglov.webshop.presentation.main.MainPresenter
import com.example.vshcheglov.webshop.presentation.order.OrderPresenter
import com.example.vshcheglov.webshop.presentation.registration.RegisterPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DataProviderModule::class,
        MappersModule::class,
        EncryptorModule::class
    ]
)
interface AppComponent {
    fun inject(mainPresenter: MainPresenter)
    fun inject(basketPresenter: BasketPresenter)
    fun inject(networkDataSource: ProductNetworkDataSource)
    fun inject(productRepository: ProductRepository)
    fun inject(loginPresenter: LoginPresenter)
    fun inject(registerPresenter: RegisterPresenter)
    fun inject(orderPresenter: OrderPresenter)
    fun inject(boughtPresenter: PurchasePresenter)
    fun inject(usersRepository: UserRepository)
    fun inject(userNetwork: UserNetworkDataSource)
    fun inject(dataProvider: DataProvider)
    fun inject(productStorage: ProductStorage)
    fun inject(userStorage: UserStorage)
    fun inject(realmOrderMapper: RealmOrderMapper)
    fun inject(responseOrderMapper: ResponseOrderMapper)
    fun inject(realmResponseOrderMapper: RealmResponseOrderMapper)
}