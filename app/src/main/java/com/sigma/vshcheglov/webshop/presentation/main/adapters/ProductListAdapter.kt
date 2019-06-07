package com.sigma.vshcheglov.webshop.presentation.main.adapters

import androidx.recyclerview.widget.RecyclerView
import com.sigma.vshcheglov.webshop.domain.Product

abstract class ProductListAdapter<ViewHolder : RecyclerView.ViewHolder?> : RecyclerView.Adapter<ViewHolder>() {
    var onBuyClickListener: ((product: Product) -> Unit)? = null
}