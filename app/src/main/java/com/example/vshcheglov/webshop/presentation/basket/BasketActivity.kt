package com.example.vshcheglov.webshop.presentation.basket

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.vshcheglov.webshop.R
import com.example.vshcheglov.webshop.presentation.basket.adapter.BasketRecyclerAdapter
import com.example.vshcheglov.webshop.presentation.basket.adapter.BasketRecyclerItemTouchHelper
import com.example.vshcheglov.webshop.presentation.entites.ProductBasketCard
import com.example.vshcheglov.webshop.presentation.entites.TotalProductPriceTitle
import com.example.vshcheglov.webshop.presentation.main.MainActivity
import com.example.vshcheglov.webshop.presentation.order.OrderActivity
import kotlinx.android.synthetic.main.activity_basket.*
import kotlinx.android.synthetic.main.activity_basket_list_layout.*
import kotlinx.android.synthetic.main.message_with_action_layout.*

class BasketActivity : AppCompatActivity(), BasketRecyclerItemTouchHelper.BasketRecyclerItemTouchHelperListener {

    private lateinit var basketAdapter: BasketRecyclerAdapter
    private lateinit var viewModel: BasketViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basket)

        viewModel = ViewModelProviders.of(this).get(BasketViewModel::class.java)
        initViewModelObservers()

        initActionBar()
        initEmptyBasketLayout()
        basketMakeOrderButton.setOnClickListener { viewModel?.makeOrder() }
    }

    private fun initViewModelObservers() {
        viewModel.liveDataBasketAmount.observe(this, Observer { basketAmount -> setBasketAmount(basketAmount) })
        viewModel.liveDataBasketItemNumber.observe(
            this,
            Observer { basketItemNumber -> basketItemsTextView.text = basketItemNumber })
        viewModel.liveDataBasket.observe(this, Observer { basket -> showBasket(basket) })
        viewModel.liveDataBasketIsEmpty.observe(this, Observer { isEmpty -> setBasketIsEmptyWarning(isEmpty) })
        viewModel.liveDataSameProductNumber.observe(this, Observer { sameProductNumber ->
            setSameProductsNumber(sameProductNumber.first, sameProductNumber.second)
        })
        viewModel.liveDataTotalProductPrice.observe(this, Observer { totalProductPrice ->
            setTotalProductPrice(totalProductPrice.first, totalProductPrice.second)
        })
        viewModel.liveDataTotalProductPriceTitle.observe(this, Observer { totalProductPriceTitle ->
            setTotalProductPriceTitle(totalProductPriceTitle)
        })
        viewModel.liveDataStartOrderScreen.observe(this, Observer { startOrderScreenEvent ->
            startOrderScreenEvent.performEventIfNotHandled { startOrderActivity() }
        })
        viewModel.liveDataRemoveItem.observe(this, Observer { removeItem ->
            removeItem.getContentIfNotHandled()?.let { position -> removeProductCard(position) }
        })
        viewModel.liveDataRestoreItem.observe(this, Observer { restoreItem ->
            restoreItem.getContentIfNotHandled()?.let { position -> restoreSameProductsCard(position) }
        })
    }

    private fun initEmptyBasketLayout() {
        messageActionLayoutTitle.text = getString(R.string.basket_empty_title)
        messageActionLayoutText.text = getString(R.string.basket_empty_text)
        messageActionLayoutButton.text = getString(R.string.basket_empty_button_text)
        messageActionLayoutButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).also {
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    private fun showBasket(productBaseketCardList: MutableList<ProductBasketCard>) {
        with(basketRecyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@BasketActivity)
            basketAdapter = BasketRecyclerAdapter(productBaseketCardList).also {
                it.onProductNumberIncreasedListener = { position -> viewModel?.productNumberIncreased(position) }
                it.onProductNumberDecreasedListener = { position -> viewModel?.productNumberDecreased(position) }
            }
            adapter = basketAdapter
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
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

    private fun startOrderActivity() {
        val intent = Intent(this, OrderActivity::class.java)
        startActivity(intent)
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

    private fun removeProductCard(position: Int) {
        basketAdapter.removeItem(position)
        val undoTitle = getString(R.string.removed_item_snackbar)
        val snackBar = Snackbar.make(basketCoordinatorLayout, undoTitle, Snackbar.LENGTH_SHORT)
        snackBar.setAction(getString(R.string.undo_uppercase)) { viewModel?.restoreProductCard() }
        snackBar.show()
    }

    private fun restoreSameProductsCard(deletedIndex: Int) {
        basketAdapter.restoreItem(deletedIndex)
    }

    private fun setSameProductsNumber(position: Int, number: Int) {
        val view = basketRecyclerView.layoutManager?.findViewByPosition(position)
        view?.let { basketAdapter.setProductsNumberByPosition(it, number, position) }
    }

    private fun setTotalProductPrice(position: Int, totalDiscountPrice: Double) {
        val view = basketRecyclerView.layoutManager?.findViewByPosition(position)
        view?.let { basketAdapter.updateCardTotalPrice(position, totalDiscountPrice, view) }
    }

    private fun setTotalProductPriceTitle(totalProductPriceTitle: TotalProductPriceTitle) {
        val view = basketRecyclerView.layoutManager?.findViewByPosition(totalProductPriceTitle.position)
        view?.let {
            basketAdapter.updateCardTotalPriceTitle(
                totalProductPriceTitle.position,
                totalProductPriceTitle.totalPrice, view, totalProductPriceTitle.percentageDiscount
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.initProductListWithBasketInfo()
    }
}
