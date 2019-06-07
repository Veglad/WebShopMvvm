package com.sigma.vshcheglov.webshop.presentation.helpers

import android.app.Activity
import android.content.Intent
import com.sigma.vshcheglov.webshop.presentation.basket.BasketActivity
import com.sigma.vshcheglov.webshop.presentation.login.LoginActivity
import com.sigma.vshcheglov.webshop.presentation.main.MainActivity
import com.sigma.vshcheglov.webshop.presentation.order.OrderActivity
import com.sigma.vshcheglov.webshop.presentation.purchase.PurchaseActivity
import com.sigma.vshcheglov.webshop.presentation.registration.RegisterActivity

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
        activity.startActivity(Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun showRegister(activity: Activity) {
        activity.startActivity(Intent(activity, RegisterActivity::class.java))
    }

    fun showOrder(activity: Activity) {
        activity.startActivity(Intent(activity, OrderActivity::class.java))
    }
}