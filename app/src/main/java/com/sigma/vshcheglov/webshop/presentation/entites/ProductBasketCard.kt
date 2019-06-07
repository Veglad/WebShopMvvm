package com.sigma.vshcheglov.webshop.presentation.entites

data class ProductBasketCard (
    var name: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var productsNumber: Int = 0,
    var totalPriceDiscount: Double = 0.0,
    var totalPrice: Double = 0.0,
    var productPriceDiscount: Double = 0.0,
    var productPrice: Double = 0.0,
    var percentageDiscount: Double = 0.0
)