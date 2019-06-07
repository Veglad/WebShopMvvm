package com.sigma.vshcheglov.webshop.presentation.basket

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.sigma.vshcheglov.webshop.R
import com.sigma.vshcheglov.webshop.presentation.basket.adapter.BasketRecyclerAdapter
import com.sigma.vshcheglov.webshop.presentation.basket.adapter.BasketRecyclerItemTouchHelper
import com.sigma.vshcheglov.webshop.presentation.entites.BasketCardPriceInfo
import com.sigma.vshcheglov.webshop.presentation.helpers.Router
import kotlinx.android.synthetic.main.activity_basket.*
import kotlinx.android.synthetic.main.activity_basket_list_layout.*
import kotlinx.android.synthetic.main.message_with_action_layout.*

class BasketActivity : AppCompatActivity(), BasketRecyclerItemTouchHelper.BasketRecyclerItemTouchHelperListener {

    private lateinit var basketAdapter: BasketRecyclerAdapter
    private val viewModel: BasketViewModel by lazy {
        ViewModelProviders.of(this).get(BasketViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basket)

        viewModel.initProductListWithBasketInfo()

        viewModel.stateLiveData.observe(this, Observer { state -> updateUi(state) })
        viewModel.commandLiveData.observe(this, Observer { commandEvent ->
            commandEvent.getContentIfNotHandled()?.let { command ->
                performCommand(command)
            }
        })

        initRecyclerView()
        initActionBar()
        initEmptyBasketLayout()
        basketMakeOrderButton.setOnClickListener { viewModel?.makeOrder() }
    }

    private fun updateUi(state: BasketViewState) {
        setBasketIsEmptyWarning(state.isBasketEmpty)
        if (!state.isBasketEmpty) {
            setBasketAmount(state.basketAmount)
            setBasketItemsNumber(state.basketItemNumber)
        }
    }

    private fun setBasketItemsNumber(basketItemNumber: Int) {
        basketItemsTextView.text = basketItemNumber.toString()
    }

    private fun performCommand(command: BasketCommand) {
        when (command) {
            is StartOrderScreen -> Router.showOrder(this)
            is RemoveBasketCard -> removeBasketCard(command.position)
            is RestoreBasketCard -> restoreBasketCard(command.position)
            is UpdateBasketCardPriceInfo -> updateBasketCardPriceInfo(command.basketCardPriceInfo)
            is ShowBasketCardList -> basketAdapter.updateBasketCardList(command.basketCards)
        }
    }

    private fun updateBasketCardPriceInfo(basketCardPriceInfo: BasketCardPriceInfo) {
        val view = basketRecyclerView.layoutManager?.findViewByPosition(basketCardPriceInfo.position)
        view?.let {
            basketAdapter.updateBasketCardPriceInfo(basketCardPriceInfo, view)
        }
    }

    private fun initEmptyBasketLayout() {
        messageActionLayoutTitle.text = getString(R.string.basket_empty_title)
        messageActionLayoutText.text = getString(R.string.basket_empty_text)
        messageActionLayoutButton.text = getString(R.string.basket_empty_button_text)
        messageActionLayoutButton.setOnClickListener { Router.showMain(this) }
    }

    private fun initRecyclerView() {
        with(basketRecyclerView) {
            layoutManager = LinearLayoutManager(this@BasketActivity)
            basketAdapter = BasketRecyclerAdapter(mutableListOf()).also {
                it.onProductNumberIncreasedListener = { position -> viewModel?.productNumberIncreased(position) }
                it.onProductNumberDecreasedListener = { position -> viewModel?.productNumberDecreased(position) }
            }
            adapter = basketAdapter
            itemAnimator = DefaultItemAnimator()
            val itemTouchSimpleCallback =
                BasketRecyclerItemTouchHelper(
                    0,
                    ItemTouchHelper.LEFT,
                    this@BasketActivity
                )
            ItemTouchHelper(itemTouchSimpleCallback).attachToRecyclerView(this)
        }
    }

    private fun initActionBar() {
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
    }

    override fun onSwiped(
        viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        direction: Int,
        position: Int
    ) {
        val holder = viewHolder as? BasketRecyclerAdapter.ViewHolder
        holder?.let { viewModel?.removeProductFromBasket(position) }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private fun setBasketAmount(amount: Double) {
        val amountTitle = String.format(getString(R.string.price_format), amount)
        basketAmountTextView.text = amountTitle
    }

    private fun setBasketIsEmptyWarning(isEmpty: Boolean) {
        if (isEmpty) {
            basketListLayout.visibility = View.INVISIBLE
            basketMakeOrderButton.visibility = View.INVISIBLE
            basketEmptyLayout.visibility = View.VISIBLE
        } else {
            basketListLayout.visibility = View.VISIBLE
            basketMakeOrderButton.visibility = View.VISIBLE
            basketEmptyLayout.visibility = View.GONE
        }
        basketMakeOrderButton.isEnabled = !isEmpty
    }

    private fun removeBasketCard(position: Int) {
        basketAdapter.removeItem(position)
        val undoTitle = getString(R.string.removed_item_snackbar)
        val snackBar = Snackbar.make(basketCoordinatorLayout, undoTitle, Snackbar.LENGTH_SHORT)
        snackBar.setAction(getString(R.string.undo_uppercase)) { viewModel?.restoreProductCard() }
        snackBar.show()
    }

    private fun restoreBasketCard(deletedIndex: Int) {
        basketAdapter.restoreItem(deletedIndex)
    }
}
