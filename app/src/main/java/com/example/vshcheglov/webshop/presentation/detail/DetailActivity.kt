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
import com.example.vshcheglov.webshop.presentation.helpers.Router
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
        viewModel.stateLiveData.observe(this, Observer { state -> updateUi(state) })
        viewModel.commandLiveData.observe(this, Observer { commandEvent ->
            commandEvent.getContentIfNotHandled()?.let { command -> performCommand(command) }
        })
    }

    private fun updateUi(state: DetailViewState) {
        showProductInfo(state.product)
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

    private fun performCommand(command: DetailCommand) {
        when (command) {
            is DetailCommand.StartBasketScreen -> Router.navigateToBasketActivity(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        viewModel?.showProductInfo(intent.extras?.getParcelable(DetailActivity.PRODUCT_KEY))
    }
}
