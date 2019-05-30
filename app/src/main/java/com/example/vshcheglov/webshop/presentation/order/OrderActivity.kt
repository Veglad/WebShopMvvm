package com.example.vshcheglov.webshop.presentation.order

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
import com.example.vshcheglov.webshop.presentation.helpers.Router
import com.shashank.sony.fancydialoglib.Animation
import com.shashank.sony.fancydialoglib.FancyAlertDialog
import com.shashank.sony.fancydialoglib.Icon
import kotlinx.android.synthetic.main.activity_order.*

class OrderActivity : AppCompatActivity() {

    private val viewModel: OrderViewModel by lazy {
        ViewModelProviders.of(this).get(OrderViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        viewModel.stateLiveData.observe(this, Observer { state -> updateUi(state) })
        viewModel.commandLiveData.observe(this, Observer { commandEvent ->
            commandEvent.getContentIfNotHandled()?.let { command -> performCommand(command) }
        })

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

    private fun performCommand(command: OrderCommand) {
        when (command) {
            is OrderCommand.ShowNoInternet -> showNoInternetError()
            is OrderCommand.ShowInvalidName -> nameTextInput.error = resources.getString(R.string.order_invalid_name)
            is OrderCommand.ShowInvalidSecondName -> {
                lastNameTextInput.error = getString(R.string.order_invalid_last_name)
            }
            is OrderCommand.ShowInvalidCardNumber -> {
                cardNumberTextInput.error = getString(R.string.order_invalid_card_number)
            }
            is OrderCommand.ShowInvalidCardMonth -> {
                cardMonthTextInput.error = getString(R.string.order_invalid_card_month)
            }
            is OrderCommand.ShowInvalidCardYear -> cardYearTextInput.error = getString(R.string.order_invalid_card_year)
            is OrderCommand.ShowInvalidCardCvv -> cardCvvTextInput.error = getString(R.string.order_invalid_cv)
            is OrderCommand.ShowOrderSaveError -> showOrderSaveError()
            is OrderCommand.NotifyOrderCompleted -> notifyOrderCompleted()
        }
    }

    private fun updateUi(state: OrderViewState) {
        setShowProgress(state.isLoading)
        orderTotalPrice.text = String.format(getString(R.string.price_format), state.orderPrice)
    }

    private fun clearErrors() {
        nameTextInput.error = ""
        lastNameTextInput.error = ""
        cardNumberTextInput.error = ""
        cardMonthTextInput.error = ""
        cardYearTextInput.error = ""
        cardCvvTextInput.error = ""
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
            .OnPositiveClicked { Router.showMain(this) }
            .OnNegativeClicked { Router.showMain(this) }
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
}
