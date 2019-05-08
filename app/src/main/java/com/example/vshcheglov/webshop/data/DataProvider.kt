package com.example.vshcheglov.webshop.data

import android.graphics.Bitmap
import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.domain.Order
import com.example.vshcheglov.webshop.data.products.ProductRepository
import com.example.vshcheglov.webshop.data.users.UserRepository
import com.example.vshcheglov.webshop.domain.User.UserCredentials
import javax.inject.Inject

class DataProvider {
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var productRepository: ProductRepository

    val isSignedIn: Boolean
        get() = userRepository.isSignedIn

    init {
        App.appComponent.inject(this)
    }

    suspend fun getProducts() = productRepository.getProducts()

    suspend fun getPromotionalProducts() = productRepository.getPromotionalProducts()

    suspend fun registerUser(email: String, password: String) {
        userRepository.registerUser(email, password)
    }

    suspend fun signInUser(email: String, password: String) {
        userRepository.signInUser(email, password)
    }

    suspend fun getCurrentUser() = userRepository.getCurrentUser()

    suspend fun saveOrder(order: Order) {
        userRepository.saveOrder(order)
    }

    fun logOut() {
        userRepository.logOut()
    }

    suspend fun getUserOrders() = userRepository.getUserOrders()

    fun saveUserProfilePhoto(profilePhotoBitmap: Bitmap, name: String) {
        userRepository.saveUserProfilePhoto(profilePhotoBitmap, name)
    }

    suspend fun getUserAvatarByteArray() = userRepository.getUserAvatarByteArray()

    fun saveUserCredentials(userCredentials: UserCredentials) {
        userRepository.saveUserCredentials(userCredentials)
    }

    fun containsUserCredentials() = userRepository.containsUserCredentials()

    fun getUserCredentials() = userRepository.getUserCredentials()

    fun deleteUserCredentials() {
        userRepository.deleteUserCredentials()
    }
}