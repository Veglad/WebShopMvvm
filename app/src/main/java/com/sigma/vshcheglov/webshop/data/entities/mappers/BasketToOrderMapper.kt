package com.sigma.vshcheglov.webshop.data.entities.mappers

import com.sigma.vshcheglov.webshop.domain.Order
import com.sigma.vshcheglov.webshop.domain.OrderProduct
import com.sigma.vshcheglov.webshop.domain.Basket
import com.sigma.vshcheglov.webshop.domain.Product
import com.sigma.vshcheglov.webshop.domain.common.Mapper
import com.google.firebase.Timestamp

class BasketToOrderMapper: Mapper<Basket, Order> {
    override fun map(from: Basket) = Order(
        map(from.productToCountList),
        Timestamp.now(),
        Basket.totalPriceWithDiscount
    )

    fun map(from: MutableList<Pair<Product, Int>>) = mutableListOf<OrderProduct>().also {
        for (productToCount in from) {
            val orderProductToCount = map(productToCount.first)
            orderProductToCount.count = productToCount.second
            it.add(orderProductToCount)
        }
    }

    fun map(from: Product) = OrderProduct(
        "",
        from.id,
        from.name,
        from.priceWithDiscount,
        from.imageThumbnailUrl
    )
}