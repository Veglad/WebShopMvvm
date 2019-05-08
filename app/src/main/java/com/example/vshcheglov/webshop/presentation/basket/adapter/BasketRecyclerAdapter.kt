package com.example.vshcheglov.webshop.presentation.basket.adapter

import android.content.Context
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.presentation.entites.ProductBasketCard
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
            Glide.with(view.context)
                .load(imageUrl)
                .error(R.drawable.no_image)
                .into(view.productBasketImage)
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
            initTotalProductsPrice(view, totalPriceDiscount)
            initTotalProductsPriceTitle(view, totalPrice, percentageDiscount)
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

    fun initTotalProductsPriceTitle(view: View, totalPrice: Double, percentageDiscount: Double) {
        if (percentageDiscount > 0) {
            view.basketTotalPriceTitle.also {
                it.text = String.format(view.context.getString(R.string.price_format), totalPrice)
                it.paintFlags = it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }
    }

    fun initTotalProductsPrice(view: View, totalDiscountPrice: Double) {
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

    fun setProductsNumberByPosition(view: View, productsNumber: Int, position: Int) {
        cardProductList[position].productsNumber = productsNumber
        view.productBasketCountTextView.text = productsNumber.toString()
    }

    fun updateCardTotalPrice(position: Int, totalPriceDiscount: Double, view: View) {
        cardProductList[position].totalPriceDiscount = totalPriceDiscount
        initTotalProductsPrice(view, totalPriceDiscount)
    }

    fun updateCardTotalPriceTitle(position: Int, totalPrice: Double, view: View, percentageDiscount: Double) {
        cardProductList[position].totalPrice = totalPrice
        initTotalProductsPriceTitle(view, totalPrice, percentageDiscount)
    }

    class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
}