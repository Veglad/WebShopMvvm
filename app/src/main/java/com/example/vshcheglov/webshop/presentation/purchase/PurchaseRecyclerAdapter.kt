package com.example.vshcheglov.webshop.presentation.purchase

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.domain.OrderProduct
import com.example.vshcheglov.webshop.presentation.helpers.ImageLoaderManager
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.bought_product_recycler_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class PurchaseRecyclerAdapter(private val context: Context,
                              var productToTimeStampList: List<Pair<OrderProduct, Timestamp>> = mutableListOf()) :
    androidx.recyclerview.widget.RecyclerView.Adapter<PurchaseRecyclerAdapter.ViewHolder>() {

    private val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bought_product_recycler_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = productToTimeStampList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productToTimeStampList[position].first
        val purchaseDate = productToTimeStampList[position].second
        with(product) {
            ImageLoaderManager.loadImage(holder.view.boughtProductImage, imageUrl)
            holder.view.boughtProductTitle.text = name
            holder.view.boughtProductPrice.text = String.format(
                holder.view.context.getString(com.example.vshcheglov.webshop.R.string.price_format),
                price
            )
            holder.view.boughtProductAmount.text = count.toString()
            holder.view.productPurchaseDate.text = simpleDateFormat.format(purchaseDate.toDate())
        }
    }

    class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
}