package com.sigma.vshcheglov.webshop.data.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmUserCredentials(
    var email: String = "",
    var encryptedPassword: String = "",
    @PrimaryKey
    var id: Int = 1
) : RealmObject()