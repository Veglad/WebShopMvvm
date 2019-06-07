package com.sigma.vshcheglov.webshop.data.entities

data class UserResponse (var email: String?, var id: String) {

    constructor() : this("", "") //TODO: Investigate serialization
}