package com.example.vshcheglov.webshop.domain.common

interface Mapper<in E, T> {
    fun map(from: E): T
}