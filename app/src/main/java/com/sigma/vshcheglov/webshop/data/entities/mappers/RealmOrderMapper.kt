package com.sigma.vshcheglov.webshop.data.entities.mappers

import com.sigma.vshcheglov.webshop.App
import com.sigma.vshcheglov.webshop.data.entities.RealmOrderProduct
import com.sigma.vshcheglov.webshop.data.entities.RealmOrder
import com.sigma.vshcheglov.webshop.domain.Order
import com.sigma.vshcheglov.webshop.domain.OrderProduct
import com.sigma.vshcheglov.webshop.domain.common.Mapper
import com.google.firebase.Timestamp
import io.realm.RealmList
import javax.inject.Inject

class RealmOrderMapper : Mapper<RealmOrder, Order> {

    @Inject
    lateinit var productMapper: RealmOrderProductMapper

    init {
        App.appComponent.inject(this)
    }

    override fun map(from: RealmOrder): Order {
        val productList = mutableListOf<OrderProduct>().apply {
            for (realmProduct in from.orderProducts) {
                add(productMapper.map(realmProduct))
            }
        }

        return Order(productList, Timestamp(from.timestampDate), from.amount, from.id)
    }

    fun map(to: Order): RealmOrder {
        val realmProductList = RealmList<RealmOrderProduct>().apply {
            for (product in to.orderProducts) {
                add(productMapper.map(product))
            }
        }

        return RealmOrder(realmProductList, to.timestamp.toDate(), to.amount, to.id)
    }
}