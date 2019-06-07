package com.sigma.vshcheglov.webshop.data.users

import android.graphics.Bitmap
import com.sigma.vshcheglov.webshop.App
import com.sigma.vshcheglov.webshop.data.entities.RealmOrder
import com.sigma.vshcheglov.webshop.data.entities.mappers.ResponseOrderMapper
import com.sigma.vshcheglov.webshop.data.entities.mappers.RealmResponseOrderMapper
import com.sigma.vshcheglov.webshop.data.entities.mappers.RealmUserCredentialsMapper
import com.sigma.vshcheglov.webshop.domain.Order
import com.sigma.vshcheglov.webshop.data.entities.mappers.ResponseUserMapper
import com.sigma.vshcheglov.webshop.domain.User.UserCredentials
import javax.inject.Inject


class UserRepository {

    @Inject
    lateinit var userNetwork: UserNetworkDataSource
    @Inject
    lateinit var mapper: ResponseUserMapper
    @Inject
    lateinit var userStorage: UserStorage
    @Inject
    lateinit var userCredentialsStorage: UserCredentialsStorage
    @Inject
    lateinit var responseOrderMapper: ResponseOrderMapper
    @Inject
    lateinit var realmResponseOrderMapper: RealmResponseOrderMapper
    @Inject
    lateinit var realmUserCredentialsMapper: RealmUserCredentialsMapper

    val isSignedIn: Boolean
        get() = userNetwork.isSignedIn

    init {
        App.appComponent.inject(this)
    }

    suspend fun registerUser(email: String, password: String) {
        userStorage.clear()
        userNetwork.registerUser(email, password)
    }

    suspend fun signInUser(email: String, password: String) {
        userNetwork.signInUser(email, password)
    }

    suspend fun getCurrentUser() = mapper.map(userNetwork.getCurrentUser())

    suspend fun saveOrder(order: Order) {
        val orderNetwork = responseOrderMapper.map(order)
        userNetwork.saveOrder(orderNetwork)
    }

    fun logOut() {
        userNetwork.logOut()
        userStorage.clear()
    }

    suspend fun getUserOrders(): MutableList<Order> {
        var orderList = mutableListOf<Order>()
        try {
            val networkOrders = userNetwork.getUserOrders()
            orderList.apply {
                for (orderNetwork in networkOrders) {
                    add(responseOrderMapper.map(orderNetwork))
                }
            }

            val realmOrderList = mutableListOf<RealmOrder>().apply {
                for (orderNetwork in networkOrders) {
                    add(realmResponseOrderMapper.map(orderNetwork))
                }
            }

            userStorage.saveOrders(realmOrderList)
        } catch (ex: Exception) {
            orderList = userStorage.getUserOrders()
        }

        return orderList
    }

    fun saveUserProfilePhoto(profilePhotoBitmap: Bitmap, name: String) {
        userNetwork.saveUserProfilePhoto(profilePhotoBitmap, name)
    }

    suspend fun getUserAvatarByteArray() = userNetwork.getUserAvatarByteArray()

    fun saveUserCredentials(userCredentials: UserCredentials) {
        userStorage.clear()
        userCredentialsStorage.saveUserCredentials(realmUserCredentialsMapper.map(userCredentials))
    }

    fun getUserCredentials(): UserCredentials? {
        val realmCredentials = userCredentialsStorage.getUserCredentials()
        return realmCredentials?.let { realmUserCredentialsMapper.map(it) }
    }

    fun containsUserCredentials() = userCredentialsStorage.containsUserCredentials()

    fun deleteUserCredentials() {
        userCredentialsStorage.deleteUserCredentials()
    }
}
