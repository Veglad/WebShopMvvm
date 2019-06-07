package com.sigma.vshcheglov.webshop.domain

import com.google.firebase.Timestamp

data class Order(
    var orderProducts: MutableList<OrderProduct>,
    var timestamp: Timestamp,
    var amount: Double,
    var id: String = "") {
    constructor() : this(mutableListOf(), Timestamp.now(), 0.0, "")
}

data class OrderProduct(
    var id: String = "",
    var productId: Int = 0,
    var name: String = "Noname",
    var price: Double = 0.0,
    var imageUrl: String = "",
    var count: Int = 0)