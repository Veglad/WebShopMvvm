package com.example.vshcheglov.webshop.presentation.entites

data class OrderCard (
    var name: String = "",
    var lastName: String = "",
    var cardNumber: String = "",
    var cardMonth: Int? = 0,
    var cardYear: Int? = 0,
    var cardCvv: String = ""
)