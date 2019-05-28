package com.example.vshcheglov.webshop.presentation.order

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.extensions.isNetworkAvailable
import com.example.vshcheglov.webshop.presentation.entites.OrderCard
import com.example.vshcheglov.webshop.presentation.main.MainActivity
import com.shashank.sony.fancydialoglib.Animation
import com.shashank.sony.fancydialoglib.FancyAlertDialog
import com.shashank.sony.fancydialoglib.Icon
import kotlinx.android.synthetic.main.activity_order.*

class OrderActivity : AppCompatActivity() {

    private lateinit var viewModel: OrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        viewModel = ViewModelProviders.of(this).get(OrderViewModel::class.java)
        initViewModelObservers()

        orderButton.setOnClickListener {
            val name = orderName.text.toString()
            val lastName = orderLastName.text.toString()
            val cardNumber = orderCardNumber.text.toString()
            val cardMonth = orderCardMonth.text.toString().toIntOrNull()
            val cardYear = orderCardYear.text.toString().toIntOrNull()
            val cardCvv = orderCardCvv.text.toString()
            val orderCard = OrderCard(name, lastName, cardNumber, cardMonth, cardYear, cardCvv)

            clearErrors()
            viewModel.makeOrder(orderCard, isNetworkAvailable())
        }

        initActionBar()
    }

    private fun initViewModelObservers() {
        viewModel.liveDataOrderPrice.observe(this, Observer { price ->
            orderTotalPrice.text = String.format(getString(R.string.price_format), price)
        })

        viewModel.liveDataIsLoading.observe(this, Observer { isLoading ->
            setShowProgress(isLoading)
        })
        viewModel.liveDataInvalidName.observe(this, Observer { event ->
            event.performEventIfNotHandled {
                nameTextInput.error = resources.getString(R.string.order_invalid_name)
            }
        })
        viewModel.liveDataInvalidSecondName.observe(this, Observer { event ->
            event.performEventIfNotHandled {
                lastNameTextInput.error = resources.getString(R.string.order_invalid_last_name)
            }
        })
        viewModel.liveDataInvalidCardNumber.observe(this, Observer { event ->
            event.performEventIfNotHandled {
                cardNumberTextInput.error = resources.getString(R.string.order_invalid_card_number)
            }
        })
        viewModel.liveDataInvalidCardMonth.observe(this, Observer { event ->
            event.performEventIfNotHandled {
                cardMonthTextInput.error = resources.getString(R.string.order_invalid_card_month)
            }
        })
        viewModel.liveDataInvalidCardYear.observe(this, Observer { event ->
            event.performEventIfNotHandled {
                cardYearTextInput.error = resources.getString(R.string.order_invalid_card_year)
            }
        })
        viewModel.liveDataInvalidCardCvv.observe(this, Observer { event ->
            event.performEventIfNotHandled {
                cardCvvTextInput.error = resources.getString(R.string.order_invalid_cv)
            }
        })
        viewModel.liveDataNoInternet.observe(this, Observer { event ->
            event.performEventIfNotHandled { showNoInternetError() }
        })
        viewModel.liveDataOrderSaveError.observe(this, Observer { event ->
            event.performEventIfNotHandled { showOrderSaveError() }
        })
        viewModel.liveDataOrderCompleted.observe(this, Observer { event ->
            event.performEventIfNotHandled { notifyOrderCompleted() }
        })
    }

    private fun clearErrors() {
        nameTextInput.error = ""
        lastNameTextInput.error = ""
        cardNumberTextInput.error = ""
        cardMonthTextInput.error = ""
        cardYearTextInput.error = ""
        cardCvvTextInput.error = ""
    }

    override fun onResume() {
        super.onResume()
        viewModel?.initOrderPrice()
    }

    private fun initActionBar() {
        setSupportActionBar(orderActionBar)
        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowTitleEnabled(false)
        }
        orderActionBar.navigationIcon?.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }

        return false
    }

    private fun setShowProgress(isVisible: Boolean) {
        if (isVisible) {
            orderButton.startAnimation()
        } else {
            orderButton.revertAnimation()
        }
    }

    private fun showNoInternetError() {
        Snackbar.make(
            orderLinearLayout,
            resources.getString(R.string.no_internet_connection_warning),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun notifyOrderCompleted() {
        FancyAlertDialog.Builder(this)
            .setTitle(getString(R.string.order_completed_title))
            .setBackgroundColor(ContextCompat.getColor(this, R.color.dialogPositiveColor))
            .setMessage(getString(R.string.order_completed_message))
            .setNegativeBtnText(getString(R.string.cancel))
            .setPositiveBtnBackground(ContextCompat.getColor(this, R.color.dialogPositiveColor))
            .setPositiveBtnText(getString(R.string.ok))
            .setNegativeBtnBackground(ContextCompat.getColor(this, R.color.dialogNeutralColor))
            .setAnimation(Animation.POP)
            .isCancellable(true)
            .setIcon(R.drawable.ic_done_white_24dp, Icon.Visible)
            .OnPositiveClicked { startMainScreen() }
            .OnNegativeClicked { startMainScreen() }
            .build()
    }

    private fun showOrderSaveError() {
        FancyAlertDialog.Builder(this)
            .setTitle(getString(R.string.order_error_title))
            .setBackgroundColor(ContextCompat.getColor(this, R.color.dialogNegativeColor))
            .setMessage(getString(R.string.order_error_message))
            .setNegativeBtnText(getString(R.string.cancel))
            .setPositiveBtnBackground(ContextCompat.getColor(this, R.color.dialogNegativeColor))
            .setPositiveBtnText(getString(R.string.ok))
            .setNegativeBtnBackground(ContextCompat.getColor(this, R.color.dialogNeutralColor))
            .setAnimation(Animation.POP)
            .isCancellable(true)
            .setIcon(R.drawable.ic_close_white_24dp, Icon.Visible)
            .build()
    }

    private fun startMainScreen() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
