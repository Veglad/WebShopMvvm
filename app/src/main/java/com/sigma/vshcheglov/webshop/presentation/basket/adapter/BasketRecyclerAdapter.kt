package com.sigma.vshcheglov.webshop.presentation.basket.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sigma.vshcheglov.webshop.R
import com.sigma.vshcheglov.webshop.presentation.entites.BasketCardPriceInfo
import com.sigma.vshcheglov.webshop.presentation.entites.ProductBasketCard
import com.sigma.vshcheglov.webshop.presentation.helpers.ImageLoaderManager
import kotlinx.android.synthetic.main.basket_recycler_item.view.*

class BasketRecyclerAdapter(private val cardProductList: MutableList<ProductBasketCard>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<BasketRecyclerAdapter.ViewHolder>() {

    var onProductNumberIncreasedListener: ((Int) -> Unit)? = null
    var onProductNumberDecreasedListener: ((Int) -> Unit)? = null
    private lateinit var removedProductBasketCard: ProductBasketCard

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.basket_recycler_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = cardProductList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = holder.view

        with(cardProductList[position]) {
            ImageLoaderManager.loadImage(view.productBasketImage, imageUrl)
            view.productBasketImage.contentDescription = String.format(
                view.context.getString(R.string.image_content_text_format),
                name
            )

            view.productBasketCountTextView.text = productsNumber.toString()
            view.productBasketTitle.text = name
            view.productBasketDescription.text = description

            view.addImageButton.setOnClickListener {
                onProductNumberIncreasedListener?.invoke(holder.adapterPosition)
            }
            view.removeImageButton.setOnClickListener {
                onProductNumberDecreasedListener?.invoke(holder.adapterPosition)
            }

            initSaleTitle(view, this)
            initProductPrice(view, this)
            initTotalProductsPrice(view, totalPrice, percentageDiscount, totalPriceDiscount)
        }
    }

    private fun initProductPrice(view: View, productBasketCard: ProductBasketCard) {
        with(productBasketCard) {
            view.productBasketPriceTextView.text =
                String.format(view.context.getString(R.string.price_format), productPriceDiscount)
            //Product price title
            if (percentageDiscount > 0) {
                view.productBasketPriceTitle.also {
                    it.text = String.format(view.context.getString(R.string.price_format), productPrice)
                    it.paintFlags = it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            }
        }
    }

    private fun initSaleTitle(view: View, productBasketCard: ProductBasketCard) {
        view.productBasketSaleTextView.text =
            String.format(view.context.getString(R.string.sale_format), productBasketCard.percentageDiscount)
        if (productBasketCard.percentageDiscount == 0.0) {
            view.productBasketSaleTextView.visibility = View.INVISIBLE
        }
    }

    private fun initTotalProductsPrice(
        view: View,
        totalPrice: Double,
        percentageDiscount: Double,
        totalDiscountPrice: Double
    ) {
        if (percentageDiscount > 0) {
            view.basketTotalPriceTitle.also {
                it.text = String.format(view.context.getString(R.string.price_format), totalPrice)
                it.paintFlags = it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        view.basketTotalPrice.text =
            String.format(view.context.getString(R.string.price_format), totalDiscountPrice)
    }

    fun removeItem(position: Int) {
        removedProductBasketCard = cardProductList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun restoreItem(position: Int) {
        cardProductList.add(position, removedProductBasketCard)
        notifyItemInserted(position)
    }

    fun updateBasketCardPriceInfo(basketCardPriceInfo: BasketCardPriceInfo, view: View) {
        with(basketCardPriceInfo) {
            cardProductList[position].totalPrice = totalPrice
            cardProductList[position].totalPriceDiscount = totalDiscountPrice
            cardProductList[position].productsNumber = productCount

            view.productBasketCountTextView.text = productCount.toString()
            initTotalProductsPrice(view, totalPrice, percentageDiscount, totalDiscountPrice)
        }
    }

    fun updateBasketCardList(basketCardList: MutableList<ProductBasketCard>) {
        cardProductList.clear()
        cardProductList.addAll(basketCardList)
    }

    class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
}