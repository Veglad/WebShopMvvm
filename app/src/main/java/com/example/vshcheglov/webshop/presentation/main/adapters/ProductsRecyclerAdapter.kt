package com.example.vshcheglov.webshop.presentation.main.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import com.bumptech.glide.Glide
import com.example.vshcheglov.webshop.presentation.detail.DetailActivity
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.presentation.helpres.ItemSnapHelper
import kotlinx.android.synthetic.main.product_recycler_item.view.*
import kotlinx.android.synthetic.main.products_recycler_title.view.*
import kotlinx.android.synthetic.main.promotional_recycler_view.view.*

class ProductsRecyclerAdapter(
    private val context: Context,
    private var productList: MutableList<Product> = mutableListOf(),
    private var promotionalProductList: List<Product> = mutableListOf()
) : ProductListAdapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        private const val PROMOTIONAL_POSITION = 1
        private const val PROMOTIONAL_TITLE_POSITION = 0
        private const val PRODUCTS_TITLE_POSITION = 2

        private const val TITLE_TYPE = 0
        private const val PROMOTIONAL_DEVICES_TYPE = 1
        private const val DEVICES_TYPE = 2

        private const val NOT_PRODUCTS_IN_LIST_COUNT = 3
    }

    private val promotionalRecyclerAdapter by lazy {
        PromotionalRecyclerAdapter(context, promotionalProductList).also {
            it.onBuyClickListener = { product ->  onBuyClickListener?.invoke(product) }
        }
    }

    fun setProductList(productList: MutableList<Product>) {
        this.productList.apply {
            clear()
            addAll(productList)
        }
    }

    private val viewPool = androidx.recyclerview.widget.RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TITLE_TYPE -> {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.products_recycler_title, parent, false)
            TitleViewHolder(view)
        }
        PROMOTIONAL_DEVICES_TYPE -> {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.promotional_recycler_view, parent, false)
            PromotionalViewHolder(view)
        }
        else -> {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.product_recycler_item, parent, false)
            ProductsViewHolder(view)
        }
    }

    override fun getItemCount() = productList.size + NOT_PRODUCTS_IN_LIST_COUNT

    override fun getItemViewType(position: Int) = when (position) {
        PROMOTIONAL_TITLE_POSITION, PRODUCTS_TITLE_POSITION -> TITLE_TYPE
        PROMOTIONAL_POSITION -> PROMOTIONAL_DEVICES_TYPE
        else -> DEVICES_TYPE
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            TITLE_TYPE -> {
                bindTitles(holder, position)
            }
            PROMOTIONAL_DEVICES_TYPE -> {
                bindPromotionalList(holder)
            }
            else -> {
                bindProductsList(holder, position)
            }
        }
    }

    private fun bindTitles(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        (holder as TitleViewHolder).apply {
            view.productsRecyclerTitleTextView.text = if (position == 0) {
                view.context.getString(R.string.promotional_title)
            } else {
                view.context.getString(R.string.devices_title)
            }
        }
    }

    private fun bindPromotionalList(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        val promotionalViewHolder = (holder as PromotionalViewHolder)
        val horizontalManager = androidx.recyclerview.widget.LinearLayoutManager(
            context,
            androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
            false
        )
        with(promotionalViewHolder.view.promotionalRecyclerView) {
            setRecycledViewPool(viewPool)
            layoutManager = horizontalManager
            adapter = promotionalRecyclerAdapter
            onFlingListener = null
            ItemSnapHelper().attachToRecyclerView(this)
        }
    }

    private fun bindProductsList(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val view = (holder as ProductsViewHolder).view
        val product = getProductByPosition(holder.adapterPosition)
        with(product) {
            Glide.with(view.context)
                .load(imageThumbnailUrl)
                .error(R.drawable.no_image)
                .into(view.productImage)
            view.productImage.contentDescription = String.format(
                view.context.getString(R.string.image_content_text_format),
                name
            )
            view.productTitle.text = name
            view.productDescription.text = shortDescription
            view.productPrice.text = String.format(
                view.context.getString(R.string.price_format),
                price
            )
            view.buyButton.setOnClickListener { onBuyClickListener?.invoke(product) }
        }

        holder.view.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putParcelable(DetailActivity.PRODUCT_KEY, product)

                })
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val imagePair = Pair.create(
                    holder.view.productImage as View,
                    context.getString(R.string.shared_image_transition_name)
                )
                val titlePair = Pair.create(
                    holder.view.productTitle as View,
                    context.getString(R.string.shared_title_transition_name)
                )
                val pricePair = Pair.create(
                    holder.view.productPrice as View,
                    context.getString(R.string.shared_price_transition_name)
                )
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    context as Activity,
                    imagePair, titlePair, pricePair
                )
                context.startActivity(intent, options.toBundle())
            } else {
                context.startActivity(intent)
            }
        }
    }

    private fun getProductByPosition(position: Int): Product = productList[position - NOT_PRODUCTS_IN_LIST_COUNT]

    fun updatePromotionalList(promotionalProductList: List<Product>) {
        this.promotionalProductList = promotionalProductList
        promotionalRecyclerAdapter.productList = promotionalProductList
        promotionalRecyclerAdapter.notifyDataSetChanged()
        notifyItemChanged(PROMOTIONAL_POSITION)
    }

    class ProductsViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

    class PromotionalViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

    class TitleViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
}