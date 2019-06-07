package com.sigma.vshcheglov.webshop.presentation.helpers.custom_drawable

import android.content.Context
import android.graphics.drawable.LayerDrawable
import android.view.MenuItem
import com.sigma.vshcheglov.webshop.R


object CountDrawableHelper {
    fun setCount(context: Context, count: String, basketMenuItem: MenuItem) {
        val icon = basketMenuItem.icon as LayerDrawable

        // Reuse drawable if possible
        val reuse = icon.findDrawableByLayerId(R.id.ic_group_count)
        val badge = if (reuse != null && reuse is CountDrawable) {
            reuse
        } else {
            CountDrawable(context)
        }

        badge.setCount(count)
        icon.mutate()
        icon.setDrawableByLayerId(R.id.ic_group_count, badge)
    }
}