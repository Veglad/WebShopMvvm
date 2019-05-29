package com.example.vshcheglov.webshop.presentation.entites

data class BasketCardPriceInfo(
    var position: Int = 0,
    var totalPrice: Double = 0.0,
    var percentageDiscount: Double = 0.0,
    var productCount: Int = 0,
    var totalDiscountPrice: Double = 0.0
)