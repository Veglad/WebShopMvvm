package com.example.vshcheglov.webshop.presentation.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.vshcheglov.webshop.presentation.basket.BasketActivity
import com.example.vshcheglov.webshop.presentation.login.LoginActivity
import com.example.vshcheglov.webshop.presentation.main.MainActivity
import com.example.vshcheglov.webshop.presentation.order.OrderActivity
import com.example.vshcheglov.webshop.presentation.purchase.PurchaseActivity
import com.example.vshcheglov.webshop.presentation.registration.RegisterActivity

object Router {
    fun showMain(activity: Activity) {
        activity.startActivity(Intent(activity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun showBasket(activity: Activity) {
        activity.startActivity(Intent(activity, BasketActivity::class.java))
    }

    fun showPurchase(activity: Activity) {
        activity.startActivity(Intent(activity, PurchaseActivity::class.java))
    }

    fun showLogin(activity: Activity) {
        activity.startActivity(Intent(activity, LoginActivity::class.java))
    }

    fun showRegister(activity: Activity) {
        activity.startActivity(Intent(activity, RegisterActivity::class.java))
    }

    fun showOrder(activity: Activity) {
        activity.startActivity(Intent(activity, OrderActivity::class.java))
    }
}