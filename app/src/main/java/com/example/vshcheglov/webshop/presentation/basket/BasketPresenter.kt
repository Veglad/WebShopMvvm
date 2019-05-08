package com.example.vshcheglov.webshop.presentation.basket

import com.example.vshcheglov.webshop.App
import com.example.vshcheglov.webshop.domain.Basket
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.presentation.entites.ProductBasketCard
import com.example.vshcheglov.webshop.presentation.entites.mappers.ProductBasketCardMapper
import nucleus5.presenter.Presenter
import javax.inject.Inject
import kotlin.properties.Delegates

class BasketPresenter : Presenter<BasketPresenter.BasketView>() {

    @Inject
    lateinit var productBasketCardMapper: ProductBasketCardMapper
    private lateinit var productToCount: Pair<Product, Int>
    private var deletedIndex by Delegates.notNull<Int>()

    init {
        App.appComponent.inject(this)
    }

    fun makeOrder() {
        view?.startOrderActivity()
    }

    fun initProductListWithBasketInfo() {
        updateBasketInfo()

        val isBasketEmpty = Basket.productsNumber == 0
        view?.setBasketIsEmptyWarning(isBasketEmpty)
        if (!isBasketEmpty) {
            view?.showBasket(productBasketCardMapper.map(Basket))
        }
    }

    fun productNumberIncreased(position: Int) {
        Basket.incrementProductCount(position)
        cardAndBasketUpdate(position)
    }

    fun productNumberDecreased(position: Int) {
        if (Basket.decrementProductCountIfAble(position)) {
            cardAndBasketUpdate(position)
        }
    }

    private fun cardAndBasketUpdate(position: Int) {
        val updatedProductToCount = Basket.productToCountList[position]
        val productCount = updatedProductToCount.second
        val product = updatedProductToCount.first

        updateBasketInfo()

        view?.let {
            it.setSameProductsNumber(position, productCount)
            it.setTotalProductPrice(position, Basket.getSameProductDiscountPrice(product.id))
            if (product.percentageDiscount > 0) {
                it.setTotalProductPriceTitle(
                    position,
                    Basket.getSameProductPrice(product.id),
                    product.percentageDiscount.toDouble()
                )
            }
        }
    }


    private fun updateBasketInfo() {
        view?.let {
            it.setBasketAmount(Basket.totalPriceWithDiscount)
            it.setBasketItemsNumber(Basket.productsNumber.toString())
        }
    }

    fun removeProductFromBasket(position: Int) {
        productToCount = Basket.productToCountList[position]
        deletedIndex = position

        Basket.removeSameProducts(position)

        view?.let {
            it.removeProductCard(position)
            it.setBasketIsEmptyWarning(Basket.productsNumber == 0)
        }

        updateBasketInfo()
    }

    fun restoreProductCard() {
        Basket.addProductToCountEntry(productToCount, deletedIndex)
        view?.let {
            it.setBasketIsEmptyWarning(Basket.productsNumber == 0)
            it.restoreSameProductsCard(deletedIndex)
        }
        updateBasketInfo()
    }

    interface BasketView {
        fun startOrderActivity()

        fun setBasketAmount(amount: Double)

        fun setBasketItemsNumber(itemsNumber: String)

        fun showBasket(productBaseketCardList: MutableList<ProductBasketCard>)

        fun setBasketIsEmptyWarning(isEmpty: Boolean)

        fun removeProductCard(position: Int)

        fun restoreSameProductsCard(deletedIndex: Int)

        fun setSameProductsNumber(position: Int, number: Int)

        fun setTotalProductPrice(position: Int, totalDiscountPrice: Double)

        fun setTotalProductPriceTitle(position: Int, totalPrice: Double, percentageDiscount: Double)
    }
}