package com.example.vshcheglov.webshop.data.entities

import com.google.firebase.Timestamp

data class OrderResponse(
    var orderProducts: MutableList<OrderResponseProduct>,
    var timestamp: Timestamp,
    var amount: Double,
    var id: String = ""
) {
    constructor() : this(mutableListOf(), Timestamp.now(), 0.0)
}


data class OrderResponseProduct(
    var id: String = "",
    var productId: Int = 0,
    var name: String = "Noname",
    var price: Double = 0.0,
    var imageUrl: String = "",
    var count: Int = 0)

