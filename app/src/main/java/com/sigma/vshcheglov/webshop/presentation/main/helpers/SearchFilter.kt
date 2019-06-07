package com.sigma.vshcheglov.webshop.presentation.main.helpers

import android.widget.Filter
import android.widget.Filterable
import com.sigma.vshcheglov.webshop.domain.Product

class SearchFilter(val productListFull : List<Product>,
                   val onPublishResults : (List<Product>?) -> Unit) : Filterable {

    private val productsFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<Product> = ArrayList()

            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(productListFull)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim()

                for (product in productListFull) {
                    if (product.name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(product)
                    }
                }
            }

            return FilterResults().also { it.values = filteredList }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val filteredList = results?.values as? List<Product>
            onPublishResults(filteredList)
        }
    }

    override fun getFilter() = productsFilter
}