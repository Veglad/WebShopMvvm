package com.example.vshcheglov.webshop.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    var id: Int = -1,
    var name: String = "Noname",
    var price: Double = 0.0,
    var imageThumbnailUrl: String = "",
    var shortDescription: String = "no",
    var longDescription : String = "no",
    var imageUrl: String = "",
    var inStockNumber: Int = 0,
    var purchasesNumber: Int = 0,
    var percentageDiscount: Int = 0
) : Parcelable {
    val priceWithDiscount
        get() = price * (1 - percentageDiscount.toDouble() / 100)
}