package com.example.vshcheglov.webshop.data.entities.mappers

import com.example.vshcheglov.webshop.data.entities.RealmProduct
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.domain.common.Mapper

class RealmProductMapper : Mapper<RealmProduct, Product> {

    override fun map(from: RealmProduct) = Product(
            from.id,
            from.name,
            from.price,
            from.imageThumbnailUrl,
            from.shortDescription,
            from.longDescription,
            from.imageUrl,
            from.inStockNumber,
            from.purchasesNumber,
            from.percentageDiscount)

    fun map(to: Product) = RealmProduct(
            to.id,
            to.name,
            to.price,
            to.imageThumbnailUrl,
            to.shortDescription,
            to.longDescription,
            to.imageUrl,
            to.inStockNumber,
            to.purchasesNumber,
            to.percentageDiscount)
}