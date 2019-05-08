package com.example.vshcheglov.webshop.data.entities.mappers

import com.example.vshcheglov.webshop.data.entities.RealmUserCredentials
import com.example.vshcheglov.webshop.domain.User.UserCredentials
import com.example.vshcheglov.webshop.domain.common.Mapper

class RealmUserCredentialsMapper : Mapper<RealmUserCredentials, UserCredentials> {

    override fun map(from: RealmUserCredentials) = UserCredentials(from.email, from.encryptedPassword)

    fun map(from: UserCredentials) = RealmUserCredentials(from.email, from.encryptedPassword)
}