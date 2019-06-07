package com.sigma.vshcheglov.webshop.presentation.entites.mappers

import com.sigma.vshcheglov.webshop.domain.Basket
import com.sigma.vshcheglov.webshop.domain.Product
import com.sigma.vshcheglov.webshop.domain.common.Mapper
import com.sigma.vshcheglov.webshop.presentation.entites.ProductBasketCard

class ProductBasketCardMapper : Mapper<Basket, MutableList<ProductBasketCard>> {

    private fun mapFrom(product: Product, productCount: Int, totalProductPrice: Double,
                        totalProductPriceDiscount: Double) = ProductBasketCard().also {
        it.name = product.name
        it.description = product.shortDescription
        it.imageUrl = product.imageThumbnailUrl
        it.productsNumber = productCount
        it.totalPriceDiscount = totalProductPriceDiscount
        it.totalPrice = totalProductPrice
        it.productPriceDiscount = product.priceWithDiscount
        it.productPrice = product.price
        it.percentageDiscount = product.percentageDiscount.toDouble()
    }

    override fun map(basket: Basket) = mutableListOf<ProductBasketCard>().apply {
        for (productToCount in basket.productToCountList) {
            val product = productToCount.first
            val productCount = productToCount.second
            val totalProductPrice = basket.getSameProductPrice(product.id)
            val totalProductPriceDiscount = basket.getSameProductDiscountPrice(product.id)
            this.add(mapFrom(product, productCount, totalProductPrice, totalProductPriceDiscount))
        }
    }
}