package com.example.vshcheglov.webshop.presentation.purchase

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.domain.OrderProduct
import com.example.vshcheglov.webshop.presentation.main.MainActivity
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.activity_purchase.*
import kotlinx.android.synthetic.main.message_with_action_layout.*
import kotlinx.android.synthetic.main.purchase_list_layout.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.vshcheglov.webshop.presentation.helpres.Event
import com.shashank.sony.fancydialoglib.Animation
import com.shashank.sony.fancydialoglib.FancyAlertDialog
import com.shashank.sony.fancydialoglib.Icon

class PurchaseActivity : AppCompatActivity() {

    companion object {
        const val COLUMNS_NUMBER = 2
    }

    private lateinit var boughtRecyclerAdapter: PurchaseRecyclerAdapter
    private lateinit var viewModel: PurchaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)

        viewModel = ViewModelProviders.of(this).get(PurchaseViewModel::class.java)
        initViewModelObservers()

        setSupportActionBar(boughtToolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.bought_products)
        }

        messageActionLayoutButton.setOnClickListener { startMainActivity() }
    }

    private fun initViewModelObservers() {
        viewModel.liveDataIsLoading.observe(this,
            Observer<Boolean> { isLoading ->
                purchaseProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            })
        viewModel.liveDataProducts.observe(this,
            Observer<List<Pair<OrderProduct, Timestamp>>> { products ->
                showProducts(products)
            })
        viewModel.liveDataShowProductsLoadingError.observe(this,
            Observer<Exception> { ex ->
                showProductsFetchingError(ex)
            })
        viewModel.liveDataShowNoProducts.observe(this,
            Observer<Event> { products ->
                showNoData()
            })

    }

    private fun showProducts(productToTimeStampList: List<Pair<OrderProduct, Timestamp>>) {
        boughtRecyclerAdapter = PurchaseRecyclerAdapter(this, productToTimeStampList)
        purchaseRecyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, COLUMNS_NUMBER)
        purchaseRecyclerView.adapter = boughtRecyclerAdapter
    }

    private fun showProductsFetchingError(exception: Exception) {
        FancyAlertDialog.Builder(this)
            .setTitle(getString(R.string.bought_error_title))
            .setBackgroundColor(ContextCompat.getColor(this, R.color.dialogNegativeColor))
            .setMessage(getString(R.string.order_error_message))
            .setNegativeBtnText(getString(R.string.cancel))
            .setPositiveBtnBackground(ContextCompat.getColor(this, R.color.dialogNegativeColor))
            .setPositiveBtnText(getString(R.string.ok))
            .setNegativeBtnBackground(ContextCompat.getColor(this, R.color.dialogNeutralColor))
            .setAnimation(Animation.POP)
            .isCancellable(true)
            .setIcon(R.drawable.ic_close_white_24dp, Icon.Visible)
            .OnPositiveClicked { startMainActivity() }
            .OnNegativeClicked { startMainActivity() }
            .build()
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun showNoData() {
        purchaseListLayout.visibility = View.GONE
        purchaseErrorLayout.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }

        return false
    }
}
