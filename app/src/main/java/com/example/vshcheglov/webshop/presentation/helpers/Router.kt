package com.example.vshcheglov.webshop.presentation.helpers

import android.content.Context
import android.content.Intent
import com.example.vshcheglov.webshop.presentation.basket.BasketActivity
import com.example.vshcheglov.webshop.presentation.login.LoginActivity
import com.example.vshcheglov.webshop.presentation.main.MainActivity
import com.example.vshcheglov.webshop.presentation.order.OrderActivity
import com.example.vshcheglov.webshop.presentation.purchase.PurchaseActivity
import com.example.vshcheglov.webshop.presentation.registration.RegisterActivity

object Router {
    fun navigateToMainActivity(context: Context) {
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun navigateToBasketActivity(context: Context) {
        context.startActivity(Intent(context, BasketActivity::class.java))
    }

    fun navigateToPurchaseActivity(context: Context) {
        context.startActivity(Intent(context, PurchaseActivity::class.java))
    }



    fun navigateToLoginActivity(context: Context) {
        context.startActivity(Intent(context, LoginActivity::class.java))
    }

    fun navigateToRegisterActivity(context: Context) {
        context.startActivity(Intent(context, RegisterActivity::class.java))
    }

    fun navigateToOrderActivity(context: Context) {
        context.startActivity(Intent(context, OrderActivity::class.java))
    }
}