package com.sigma.vshcheglov.webshop.data.entities.mappers

import com.sigma.vshcheglov.webshop.App
import com.sigma.vshcheglov.webshop.data.entities.OrderResponse
import com.sigma.vshcheglov.webshop.data.entities.OrderResponseProduct
import com.sigma.vshcheglov.webshop.data.entities.RealmOrderProduct
import com.sigma.vshcheglov.webshop.data.entities.RealmOrder
import com.sigma.vshcheglov.webshop.domain.common.Mapper
import com.google.firebase.Timestamp
import io.realm.RealmList
import javax.inject.Inject

class RealmResponseOrderMapper : Mapper<RealmOrder, OrderResponse> {

    @Inject
    lateinit var productMapper: RealmResponseOrderProductMapper

    init {
        App.appComponent.inject(this)
    }

    override fun map(from: RealmOrder): OrderResponse {
        val networkProductList = mutableListOf<OrderResponseProduct>().apply {
            for (realmProduct in from.orderProducts) {
                add(productMapper.map(realmProduct))
            }
        }

        return OrderResponse(networkProductList, Timestamp(from.timestampDate), from.amount, from.id)
    }

    fun map(to: OrderResponse): RealmOrder {
        val realmProductList = RealmList<RealmOrderProduct>().apply {
            for (networkProduct in to.orderProducts) {
                add(productMapper.map(networkProduct))
            }
        }

        return RealmOrder(realmProductList, to.timestamp.toDate(), to.amount, to.id)
    }
}