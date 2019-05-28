package com.example.vshcheglov.webshop.presentation.detail

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.domain.Product
import com.example.vshcheglov.webshop.presentation.basket.BasketActivity
import kotlinx.android.synthetic.main.activity_detail.*


class DetailActivity : AppCompatActivity() {

    companion object {
        const val PRODUCT_KEY = "product_key"
    }

    private lateinit var viewModel: DetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        viewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)
        initViewModelObservers()

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        detailBuyFloatActionButton.setOnClickListener {
            viewModel?.buyProduct()
        }
    }

    private fun initViewModelObservers() {
        viewModel.liveDataProductInfo.observe(this, Observer { productInfo -> showProductInfo(productInfo) })
        viewModel.liveDataStartBasketScreen.observe(this, Observer { event ->
            event.performEventIfNotHandled { startBasketActivity() }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel?.showProductInfo(intent.extras?.getParcelable(DetailActivity.PRODUCT_KEY))
    }

    private fun showProductInfo(product: Product) {
        with(product) {
            Glide.with(this@DetailActivity)
                .load(imageThumbnailUrl)
                .error(R.drawable.no_image)
                .into(detailProductImageView)
            detailProductTitle.text = name
            detailPriceTextView.text = String.format(getString(R.string.price_format), price)
            purchasesNumberTextView.text =
                String.format(getString(R.string.number_of_purchases_format), purchasesNumber.toString())
            detailDescriptionTextView.text = longDescription
            if (percentageDiscount > 0) {
                detailSaleTextView.visibility = View.VISIBLE
                detailSaleTextView.text = String.format(getString(R.string.sale_format), percentageDiscount.toString())
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    private fun startBasketActivity() {
        val intent = Intent(this, BasketActivity::class.java)
        startActivity(intent)
    }
}
