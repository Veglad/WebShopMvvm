package com.example.vshcheglov.webshop.domain

data class User (var email: String?, var id: String) {
    
    data class UserCredentials(var email: String = "",
                               var encryptedPassword: String = "")
}