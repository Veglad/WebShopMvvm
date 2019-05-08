package com.example.vshcheglov.webshop.data.entities

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class RealmOrder(
    var orderProducts: RealmList<RealmOrderProduct>,
    var timestampDate: Date,
    var amount: Double,
    @PrimaryKey
    var id: String = ""
) : RealmObject() {
    constructor() : this(RealmList<RealmOrderProduct>(), Date(), 0.0)
}


open class RealmOrderProduct(
    @PrimaryKey
    var id: String = "",
    var productId: Int = 0,
    var name: String = "Noname",
    var price: Double = 0.0,
    var imageUrl: String = "",
    var count: Int = 0) : RealmObject()

