package com.sigma.vshcheglov.webshop.presentation.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sigma.vshcheglov.webshop.R
import com.sigma.vshcheglov.webshop.domain.Product
import com.sigma.vshcheglov.webshop.presentation.helpers.ImageLoaderManager
import com.sigma.vshcheglov.webshop.presentation.helpers.Router
import com.sigma.vshcheglov.webshop.presentation.helpers.custom_drawable.CountDrawableHelper
import kotlinx.android.synthetic.main.activity_detail.*


class DetailActivity : AppCompatActivity() {

    companion object {
        const val PRODUCT_KEY = "product_key"
    }

    private val viewModel: DetailViewModel by lazy {
        ViewModelProviders.of(this).get(DetailViewModel::class.java)
    }

    private var menu: Menu? = null
    private var itemsNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

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
        viewModel.basketItems.observe(this, Observer { basketItems -> updateBasketItems(basketItems) })
        viewModel.commandLiveData.observe(this, Observer { commandEvent ->
            commandEvent.getContentIfNotHandled()?.let { command -> performCommand(command) }
        })
    }

    private fun updateUi(state: DetailViewState) {
        showProductInfo(state.product)
    }

    private fun updateBasketItems(basketItems: Int) {
        if (menu == null) {
            itemsNumber = basketItems
        } else {
            menu?.let {
                val basketMenuItem = it.findItem(R.id.actionDetailBasket)
                CountDrawableHelper.setCount(this, basketItems.toString(), basketMenuItem)
            }
        }
    }

    private fun showProductInfo(product: Product) {
        with(product) {
            ImageLoaderManager.loadImage(detailProductImageView, imageThumbnailUrl)
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
            is StartBasketScreen -> Router.showBasket(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.actionDetailBasket -> Router.showBasket(this)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.detail_menu, menu)
        updateBasketItems(itemsNumber)
        return true
    }

    override fun onResume() {
        super.onResume()
        viewModel?.showProductInfo(intent.extras?.getParcelable(DetailActivity.PRODUCT_KEY))
    }
}
