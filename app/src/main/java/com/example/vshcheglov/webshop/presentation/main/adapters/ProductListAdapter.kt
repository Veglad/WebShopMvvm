package com.example.vshcheglov.webshop.presentation.main.adapters

import androidx.recyclerview.widget.RecyclerView
import com.example.vshcheglov.webshop.domain.Product

abstract class ProductListAdapter<ViewHolder : RecyclerView.ViewHolder?> : RecyclerView.Adapter<ViewHolder>() {
    var onBuyClickListener: ((product: Product) -> Unit)? = null
}