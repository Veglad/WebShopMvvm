package com.example.vshcheglov.webshop.data.entities.mappers

import com.example.vshcheglov.webshop.data.entities.UserResponse
import com.example.vshcheglov.webshop.domain.User
import com.example.vshcheglov.webshop.domain.common.Mapper

class ResponseUserMapper: Mapper<UserResponse, User> {

    override fun map(from: UserResponse) = User(from.email, from.id)

    fun map(from: User) = UserResponse(from.email, from.id)
}