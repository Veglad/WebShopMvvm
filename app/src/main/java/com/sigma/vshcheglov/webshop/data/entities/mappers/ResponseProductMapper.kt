package com.sigma.vshcheglov.webshop.data.entities.mappers

import com.sigma.vshcheglov.webshop.data.entities.ProductResponse
import com.sigma.vshcheglov.webshop.domain.Product
import com.sigma.vshcheglov.webshop.domain.common.Mapper

class ResponseProductMapper : Mapper<ProductResponse, Product> {

    override fun map(from: ProductResponse) = Product().apply {
        id = from.deviceId
        name = from.name
        price = from.price
        imageThumbnailUrl = from.imageThumbnailUrl
        shortDescription = from.shortDescription
        longDescription = from.longDescription
        imageUrl = from.imageUrl
        inStockNumber = from.inStok
        purchasesNumber = from.bought
        percentageDiscount = from.promotional
    }

    fun map(from: Collection<ProductResponse>) = mutableListOf<Product>().apply {
        for (productEntity in from) {
            this.add(map(productEntity))
        }
    }
}