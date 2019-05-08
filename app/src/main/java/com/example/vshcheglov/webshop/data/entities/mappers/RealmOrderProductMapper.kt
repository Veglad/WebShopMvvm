package com.example.vshcheglov.webshop.data.entities.mappers

import com.example.vshcheglov.webshop.data.entities.RealmOrderProduct
import com.example.vshcheglov.webshop.domain.OrderProduct
import com.example.vshcheglov.webshop.domain.common.Mapper

class RealmOrderProductMapper : Mapper<RealmOrderProduct, OrderProduct> {
    override fun map(from: RealmOrderProduct) = OrderProduct(
        from.id,
        from.productId,
        from.name,
        from.price,
        from.imageUrl,
        from.count
    )

    fun map(to: OrderProduct) = RealmOrderProduct(
        to.id,
        to.productId,
        to.name,
        to.price,
        to.imageUrl,
        to.count
    )
}