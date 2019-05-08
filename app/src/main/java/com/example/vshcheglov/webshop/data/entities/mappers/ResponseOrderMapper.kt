package com.example.vshcheglov.webshop.data.entities.mappers

import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.data.entities.OrderResponse
import com.example.vshcheglov.webshop.data.entities.OrderResponseProduct
import com.example.vshcheglov.webshop.domain.Order
import com.example.vshcheglov.webshop.domain.OrderProduct
import com.example.vshcheglov.webshop.domain.common.Mapper
import javax.inject.Inject

class ResponseOrderMapper : Mapper<OrderResponse, Order> {

    @Inject
    lateinit var productMapper: ResponseOrderProductMapper

    init {
        App.appComponent.inject(this)
    }

    override fun map(from: OrderResponse): Order {
        val orderList = mutableListOf<OrderProduct>().apply {
            for (networkProduct in from.orderProducts) {
                add(productMapper.map(networkProduct))
            }
        }

        return Order(orderList, from.timestamp, from.amount, from.id)
    }

    fun map(to: Order): OrderResponse {
        val orderNetworkList = mutableListOf<OrderResponseProduct>().apply {
            for (product in to.orderProducts) {
                add(productMapper.map(product))
            }
        }

        return OrderResponse(orderNetworkList, to.timestamp, to.amount, to.id)
    }
}