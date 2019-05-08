package com.example.vshcheglov.webshop.data.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmProduct(
    @PrimaryKey
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
) : RealmObject()