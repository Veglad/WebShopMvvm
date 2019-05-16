package com.example.vshcheglov.webshop.presentation.purchase

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.domain.OrderProduct
import com.example.vshcheglov.webshop.presentation.main.MainActivity
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.activity_purchase.*
import kotlinx.android.synthetic.main.message_with_action_layout.*
import kotlinx.android.synthetic.main.purchase_list_layout.*
import nucleus5.factory.RequiresPresenter
import nucleus5.view.NucleusAppCompatActivity
import androidx.core.content.ContextCompat
import com.shashank.sony.fancydialoglib.Animation
import com.shashank.sony.fancydialoglib.FancyAlertDialog
import com.shashank.sony.fancydialoglib.Icon


@RequiresPresenter(PurchaseViewModel::class)
class PurchaseActivity : NucleusAppCompatActivity<PurchaseViewModel>(), PurchaseViewModel.View {

    companion object {
        const val COLUMNS_NUMBER = 2
    }

    private lateinit var boughtRecyclerAdapter: PurchaseRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)

        setSupportActionBar(boughtToolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.bought_products)
        }

        messageActionLayoutButton.setOnClickListener { startMainActivity() }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }

        return false
    }

    override fun showProducts(productToTimeStampList: List<Pair<OrderProduct, Timestamp>>) {
        boughtRecyclerAdapter = PurchaseRecyclerAdapter(this, productToTimeStampList)
        purchaseRecyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, COLUMNS_NUMBER)
        purchaseRecyclerView.adapter = boughtRecyclerAdapter
    }

    override fun showProductsFetchingError(exception: Exception) {
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

    override fun showNoData() {
        purchaseListLayout.visibility = View.GONE
        purchaseErrorLayout.visibility = View.VISIBLE
    }

    override fun setShowLoading(isLoading: Boolean) {
        purchaseProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
