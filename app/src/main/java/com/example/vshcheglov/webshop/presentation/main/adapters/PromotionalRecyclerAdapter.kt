package com.example.vshcheglov.webshop.presentation.main.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import com.bumptech.glide.Glide
import com.example.vshcheglov.webshop.presentation.detail.DetailActivity
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.domain.Product
import kotlinx.android.synthetic.main.promotional_recycler_item.view.*

class PromotionalRecyclerAdapter(private val context: Context, var productList: List<Product>) :
    ProductListAdapter<PromotionalRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.promotional_recycler_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(productList[holder.adapterPosition]) {
            Glide.with(holder.view.context)
                .load(imageThumbnailUrl)
                .error(R.drawable.no_image)
                .into(holder.view.productImage)
            holder.view.productImage.contentDescription = String.format(
                holder.view.context.getString(com.example.vshcheglov.webshop.R.string.image_content_text_format),
                name
            )
            holder.view.saleTextView.text = String.format(
                holder.view.context.getString(com.example.vshcheglov.webshop.R.string.sale_format),
                percentageDiscount
            )
            holder.view.productTitle.text = name
            holder.view.productPrice.text = String.format(
                holder.view.context.getString(com.example.vshcheglov.webshop.R.string.price_format),
                price
            )
            holder.view.buyButton.setOnClickListener {
                val product = productList[holder.adapterPosition]
                onBuyClickListener?.invoke(this)
            }
        }

        holder.view.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putParcelable(DetailActivity.PRODUCT_KEY, productList[position])
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
                val salePair = Pair.create(
                    holder.view.saleTextView as View,
                    context.getString(R.string.shared_sale_transition_name)
                )
                val pricePair = Pair.create(
                    holder.view.productPrice as View,
                    context.getString(R.string.shared_price_transition_name)
                )
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    context as Activity,
                    imagePair, titlePair, salePair, pricePair
                )
                context.startActivity(intent, options.toBundle())
            } else {
                context.startActivity(intent)
            }
        }
    }

    class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
}