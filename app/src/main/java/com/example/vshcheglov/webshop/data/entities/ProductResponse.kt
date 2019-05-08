package com.example.vshcheglov.webshop.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductResponse(
    var deviceId: Int = -1,
    var name: String = "Noname",
    var price: Double = 0.0,
    var imageThumbnailUrl: String = "",
    var shortDescription: String = "no",
    var longDescription: String = "no",
    var imageUrl: String = "",
    var inStok: Int = 0,
    var isPopular: Boolean = true,
    var bought: Int = 0,
    var categoryId: Int = 0,
    var promotional: Int = 0
) : Parcelable